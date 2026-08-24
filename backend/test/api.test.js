const { test, describe, before, after } = require('node:test');
const assert = require('node:assert');
const http = require('node:http');
const { spawn } = require('node:child_process');
const path = require('node:path');

let serverProc = null;
const PORT = process.env.PORT || 3000;

function request(method, path, body = null) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : null;
    const req = http.request(
      {
        hostname: '127.0.0.1',
        port: PORT,
        path,
        method,
        headers: {
          'Content-Type': 'application/json',
          ...(data ? { 'Content-Length': Buffer.byteLength(data) } : {})
        }
      },
      (res) => {
        let responseBody = '';
        res.on('data', (chunk) => (responseBody += chunk));
        res.on('end', () => {
          try {
            const parsed = responseBody ? JSON.parse(responseBody) : {};
            resolve({ status: res.statusCode, headers: res.headers, body: parsed });
          } catch (e) {
            resolve({ status: res.statusCode, headers: res.headers, body: responseBody });
          }
        });
      }
    );

    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

function checkServerReady() {
  return new Promise((resolve) => {
    const req = http.get(`http://127.0.0.1:${PORT}/health`, (res) => {
      resolve(res.statusCode === 200);
    });
    req.on('error', () => resolve(false));
  });
}

describe('Tech Store Stripe Backend Test Suite', () => {
  before(async () => {
    const isReady = await checkServerReady();
    if (!isReady) {
      serverProc = spawn('node', ['server.js'], {
        cwd: path.join(__dirname, '..'),
        stdio: 'ignore'
      });

      for (let i = 0; i < 15; i++) {
        await new Promise((r) => setTimeout(r, 400));
        if (await checkServerReady()) {
          break;
        }
      }
    }
  });

  after(() => {
    if (serverProc) {
      serverProc.kill();
    }
  });

  test('GET / should return service metadata', async () => {
    const res = await request('GET', '/');
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.body.ok, true);
    assert.strictEqual(res.body.service, 'tech-store-stripe-backend');
  });

  test('GET /health should return 200 OK', async () => {
    const res = await request('GET', '/health');
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.body.ok, true);
  });

  describe('POST /api/payment-methods/create-card Validation', () => {
    test('Should return 400 when userId is missing', async () => {
      const res = await request('POST', '/api/payment-methods/create-card', {
        cardToken: 'tok_visa'
      });
      assert.strictEqual(res.status, 400);
      assert.match(res.body.message, /Missing required card token/i);
    });

    test('Should return 400 when cardToken is missing', async () => {
      const res = await request('POST', '/api/payment-methods/create-card', {
        userId: 'user_test_123'
      });
      assert.strictEqual(res.status, 400);
      assert.match(res.body.message, /Missing required card token/i);
    });
  });

  describe('POST /api/payments/create-payment-intent Validation', () => {
    test('Should return 400 when totalAmount is non-positive or invalid', async () => {
      const res = await request('POST', '/api/payments/create-payment-intent', {
        totalAmount: 0,
        paymentMethod: 'pm_card_visa'
      });
      assert.strictEqual(res.status, 400);
      assert.match(res.body.message, /totalAmount must be a positive number/i);
    });

    test('Should return 400 when paymentMethod is missing', async () => {
      const res = await request('POST', '/api/payments/create-payment-intent', {
        totalAmount: 150.0
      });
      assert.strictEqual(res.status, 400);
      assert.match(res.body.message, /paymentMethod is required/i);
    });

    test('Should return 400 when paymentMethod format is invalid (not starting with pm_)', async () => {
      const res = await request('POST', '/api/payments/create-payment-intent', {
        totalAmount: 150.0,
        paymentMethod: 'invalid_card_id'
      });
      assert.strictEqual(res.status, 400);
      assert.match(res.body.message, /must be a saved Stripe payment method \(pm_\)/i);
    });
  });
});
