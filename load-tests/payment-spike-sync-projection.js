import http from 'k6/http'
import { check, sleep } from 'k6'
import { Trend } from 'k6/metrics'

export const options = {
  scenarios: {
    payment_spike: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS ?? 50),
      duration: __ENV.DURATION ?? '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    payment_latency: ['p(95)<1500'],
  },
}

const adminBaseUrl = __ENV.ADMIN_API_BASE_URL ?? 'http://127.0.0.1:8082'
const shopBaseUrl = __ENV.SHOP_API_BASE_URL ?? 'http://127.0.0.1:8081'
const paymentLatency = new Trend('payment_latency')

export function setup() {
  const suffix = `${Date.now()}`
  const adminAuth = postJson(`${adminBaseUrl}/api/admin/auth/login`, {
    username: __ENV.ADMIN_USERNAME ?? 'admin',
    password: __ENV.ADMIN_PASSWORD ?? 'password1',
  })
  check(adminAuth, { 'admin logged in': (response) => response.status === 200 })
  const adminHeaders = authHeaders(adminAuth.json('payload.accessToken'))
  const productCount = Number(__ENV.PRODUCT_COUNT ?? 1)
  const productIds = []
  for (let index = 0; index < productCount; index += 1) {
    const product = postJson(`${adminBaseUrl}/api/admin/products`, {
      name: `Spike Americano ${suffix}-${index}`,
      price: 12000,
    }, adminHeaders)
    const productId = product.json('payload.id')
    postJson(`${adminBaseUrl}/api/admin/products/${productId}/inventory`, {
      quantity: Number(__ENV.STOCK ?? 100000),
    }, adminHeaders)
    productIds.push(productId)
  }
  return { productIds }
}

export default function (data) {
  const unique = `${__VU}-${__ITER}-${Date.now()}`
  const productId = data.productIds[(__VU + __ITER) % data.productIds.length]
  const signup = postJson(`${shopBaseUrl}/api/shop/auth/signup`, {
    username: `spike-${unique}`,
    password: 'password1',
    name: `Spike ${unique}`,
    email: `spike-${unique}@example.com`,
  })
  check(signup, { 'member signed up': (response) => response.status === 201 })
  const shopHeaders = authHeaders(signup.json('payload.accessToken'))
  const memberId = signup.json('payload.member.id')

  const order = postJson(`${shopBaseUrl}/api/shop/orders`, {
    memberId,
    lines: [{ productId, quantity: 1 }],
  }, shopHeaders)
  check(order, { 'order created': (response) => response.status === 201 })
  const orderId = order.json('payload.id')

  const startedAt = Date.now()
  const payment = postJson(`${shopBaseUrl}/api/shop/orders/${orderId}/pay`, {
    idempotencyKey: `spike-pay-${unique}`,
  }, shopHeaders)
  paymentLatency.add(Date.now() - startedAt)
  check(payment, {
    'payment completed': (response) => response.status === 200,
    'coupon issued': (response) => Number(response.json('payload.issuedCouponCount')) > 0,
  })

  sleep(Number(__ENV.SLEEP_SECONDS ?? 0))
}

function postJson(url, body, headers = {}) {
  return http.post(url, JSON.stringify(body), {
    headers: { 'Content-Type': 'application/json', ...headers },
  })
}

function authHeaders(accessToken) {
  return { Authorization: `Bearer ${accessToken}` }
}
