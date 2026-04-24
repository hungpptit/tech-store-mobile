const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const Stripe = require('stripe');
const path = require('path');

const envResult = dotenv.config({ path: path.join(__dirname, '.env') });
if (envResult.error) {
  console.warn('Failed to load .env from backend folder:', envResult.error.message);
}

const app = express();
const port = process.env.PORT || 3000;
const stripeSecretKey = process.env.STRIPE_SECRET_KEY;
const stripe = stripeSecretKey ? new Stripe(stripeSecretKey) : null;

console.log('Backend env loaded:', {
  port,
  hasStripeSecretKey: Boolean(stripeSecretKey),
  stripeSecretKeyLength: stripeSecretKey ? stripeSecretKey.length : 0
});

app.use(cors());
app.use(express.json());
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

app.get('/', (req, res) => {
  res.json({ ok: true, service: 'tech-store-stripe-backend', port });
});

app.get('/health', (req, res) => {
  res.json({ ok: true, service: 'tech-store-stripe-backend' });
});

app.post('/api/payments/create-payment-intent', async (req, res) => {
  try {
    if (!stripe) {
      return res.status(500).json({ message: 'STRIPE_SECRET_KEY is not configured on the backend.' });
    }

    const { totalAmount, currency, userId, orderId, paymentMethod } = req.body || {};
    const normalizedAmount = Number(totalAmount);

    if (!Number.isFinite(normalizedAmount) || normalizedAmount <= 0) {
      return res.status(400).json({ message: 'totalAmount must be a positive number.' });
    }

    const paymentIntent = await stripe.paymentIntents.create({
      amount: Math.round(normalizedAmount * 100),
      currency: (currency || 'usd').toString().toLowerCase(),
      payment_method_types: ['card'],
      description: `Tech Store order ${orderId || ''}`.trim(),
      metadata: {
        userId: userId || '',
        orderId: orderId || '',
        paymentMethod: paymentMethod || 'card'
      }
    });

    return res.json({
      id: paymentIntent.id,
      object: paymentIntent.object,
      amount: paymentIntent.amount,
      currency: paymentIntent.currency,
      status: paymentIntent.status,
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id,
      payment_method_types: paymentIntent.payment_method_types,
      message: 'PaymentIntent created successfully'
    });
  } catch (error) {
    console.error('Create PaymentIntent error:', error);
    return res.status(500).json({ message: error?.message || 'Failed to create PaymentIntent.' });
  }
});

app.post('/api/payment-methods/create-card', async (req, res) => {
  try {
    if (!stripe) {
      return res.status(500).json({ message: 'STRIPE_SECRET_KEY is not configured on the backend.' });
    }

    const { userId, cardNumber, expMonth, expYear, cvc, cardHolderName } = req.body || {};
    const normalizedCardNumber = String(cardNumber || '').replace(/\D+/g, '');
    const normalizedExpMonth = Number(expMonth);
    const normalizedExpYear = Number(expYear);
    const normalizedCvc = String(cvc || '').trim();

    if (!userId || !normalizedCardNumber || !Number.isFinite(normalizedExpMonth) || !Number.isFinite(normalizedExpYear) || !normalizedCvc) {
      return res.status(400).json({ message: 'Missing required card fields.' });
    }

    const paymentMethod = await stripe.paymentMethods.create({
      type: 'card',
      card: {
        number: normalizedCardNumber,
        exp_month: normalizedExpMonth,
        exp_year: normalizedExpYear,
        cvc: normalizedCvc,
      },
      billing_details: {
        name: cardHolderName || ''
      },
      metadata: {
        userId: userId || ''
      }
    });

    const card = paymentMethod.card || {};

    return res.json({
      id: paymentMethod.id,
      brand: card.brand || '',
      last4: card.last4 || '',
      expMonth: card.exp_month || null,
      expYear: card.exp_year || null,
      cardHolderName: cardHolderName || '',
      message: 'PaymentMethod created successfully'
    });
  } catch (error) {
    console.error('Create PaymentMethod error:', error);
    return res.status(500).json({ message: error?.message || 'Failed to create PaymentMethod.' });
  }
});

app.listen(port, '0.0.0.0', () => {
  console.log(`Stripe backend listening on http://localhost:${port}`);
});

process.on('uncaughtException', (error) => {
  console.error('Uncaught exception:', error);
});

process.on('unhandledRejection', (reason) => {
  console.error('Unhandled rejection:', reason);
});



