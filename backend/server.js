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

async function findStripeCustomerByUserId(userId) {
  let startingAfter = undefined;

  while (true) {
    const page = await stripe.customers.list({
      limit: 100,
      ...(startingAfter ? { starting_after: startingAfter } : {})
    });

    const matchedCustomer = page.data.find(customer => customer?.metadata?.userId === userId);
    if (matchedCustomer) {
      return matchedCustomer;
    }

    if (!page.has_more || page.data.length === 0) {
      return null;
    }

    startingAfter = page.data[page.data.length - 1].id;
  }
}

async function getOrCreateStripeCustomer(userId, cardHolderName) {
  const existingCustomer = await findStripeCustomerByUserId(userId);
  if (existingCustomer) {
    return existingCustomer;
  }

  return stripe.customers.create({
    name: cardHolderName || undefined,
    metadata: {
      userId
    }
  });
}

app.post('/api/payments/create-payment-intent', async (req, res) => {
  try {
    if (!stripe) {
      return res.status(500).json({ message: 'STRIPE_SECRET_KEY is not configured on the backend.' });
    }

    const { totalAmount, currency, userId, orderId, paymentMethod } = req.body || {};
    const normalizedAmount = Number(totalAmount);
    const paymentMethodId = String(paymentMethod || '').trim();

    if (!Number.isFinite(normalizedAmount) || normalizedAmount <= 0) {
      return res.status(400).json({ message: 'totalAmount must be a positive number.' });
    }

    if (!paymentMethodId) {
      return res.status(400).json({ message: 'paymentMethod is required for saved-card checkout.' });
    }

    if (!paymentMethodId.startsWith('pm_')) {
      return res.status(400).json({ message: 'paymentMethod must be a saved Stripe payment method (pm_).' });
    }

    const savedPaymentMethod = await stripe.paymentMethods.retrieve(paymentMethodId);
    if (!savedPaymentMethod) {
      return res.status(404).json({ message: 'PaymentMethod not found.' });
    }

    let customerId = typeof savedPaymentMethod.customer === 'string' ? savedPaymentMethod.customer : savedPaymentMethod.customer?.id;
    if (!customerId) {
      return res.status(400).json({ message: 'PaymentMethod is not attached to a Customer yet. Please add the card again.' });
    }

    const customer = await stripe.customers.retrieve(customerId);
    if (customer?.deleted) {
      return res.status(400).json({ message: 'Customer was deleted. Please add the card again.' });
    }

    if (userId && customer?.metadata?.userId && customer.metadata.userId !== userId) {
      return res.status(403).json({ message: 'PaymentMethod does not belong to this user.' });
    }

    const paymentIntentParams = {
      amount: Math.round(normalizedAmount * 100),
      currency: (currency || 'usd').toString().toLowerCase(),
      customer: customerId,
      payment_method: paymentMethodId,
      description: `Tech Store order ${orderId || ''}`.trim(),
      metadata: {
        userId: userId || '',
        orderId: orderId || '',
        paymentMethod: paymentMethodId,
        customerId
      },
      confirm: true,
      payment_method_types: ['card']
    };

    const paymentIntent = await stripe.paymentIntents.create(paymentIntentParams);

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

    const { userId, cardToken, cardHolderName } = req.body || {};
    const normalizedCardToken = String(cardToken || '').trim();

    if (!userId || !normalizedCardToken) {
      return res.status(400).json({ message: 'Missing required card token.' });
    }

    const customer = await getOrCreateStripeCustomer(userId, cardHolderName);

    const paymentMethod = await stripe.paymentMethods.create({
      type: 'card',
      card: {
        token: normalizedCardToken,
      },
      billing_details: {
        name: cardHolderName || ''
      },
      metadata: {
        userId: userId || ''
      }
    });

    await stripe.paymentMethods.attach(paymentMethod.id, { customer: customer.id });

    const card = paymentMethod.card || {};

    return res.json({
      id: paymentMethod.id,
      customerId: customer.id,
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



