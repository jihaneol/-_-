import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { AdminApp } from '../../apps/admin/AdminApp'
import { ShopApp } from '../../apps/shop/ShopApp'

let members = [{ id: 1, name: 'Kim', email: 'kim@example.com' }]
let products = [{ id: 1, name: 'Americano', price: 12000, saleStatus: 'ON_SALE' }]
let orders: any[] = []
let coupons: any[] = []
let histories: any[] = []

const server = setupServer(
  http.get('/api/admin/dashboard/summary', () => ok({
    memberCount: members.length,
    productCount: products.length,
    orderCount: orders.length,
    paidOrderCount: orders.filter((order) => order.status === 'PAID').length,
    refundedOrderCount: orders.filter((order) => order.status === 'REFUNDED').length,
    issuedCouponCount: coupons.filter((coupon) => coupon.status === 'ISSUED').length,
  })),
  http.get('/api/admin/members', () => ok(members)),
  http.post('/api/admin/members', async ({ request }) => {
    const body = await request.json() as any
    const member = { id: 2, name: body.name, email: body.email }
    members = [...members, member]
    return ok(member)
  }),
  http.get('/api/admin/products', () => ok(products)),
  http.post('/api/admin/products', async ({ request }) => {
    const body = await request.json() as any
    const product = { id: 2, name: body.name, price: body.price, saleStatus: 'ON_SALE' }
    products = [...products, product]
    return ok(product)
  }),
  http.post('/api/admin/products/:productId/inventory', ({ params }) =>
    ok({ id: 1, productId: Number(params.productId), quantity: 10 }),
  ),
  http.get('/api/admin/products/:productId/inventory', ({ params }) =>
    ok({ id: 1, productId: Number(params.productId), quantity: 10 }),
  ),
  http.post('/api/admin/products/:productId/inventory/increase', ({ params }) =>
    ok({ id: 1, productId: Number(params.productId), quantity: 11 }),
  ),
  http.get('/api/admin/orders', () => ok(orders)),
  http.post('/api/admin/orders', async ({ request }) => {
    const body = await request.json() as any
    const order = buildOrder(body.memberId, body.lines[0].productId)
    orders = [order]
    return ok(order)
  }),
  http.post('/api/admin/orders/:orderId/pay', ({ params }) => ok(payOrder(Number(params.orderId)))),
  http.post('/api/admin/orders/:orderId/cancel', ({ params }) => {
    orders = orders.map((order) => order.id === Number(params.orderId) ? { ...order, status: 'CANCELLED' } : order)
    return ok(orders[0])
  }),
  http.post('/api/admin/orders/:orderId/refund', ({ params }) =>
    ok({ orderId: Number(params.orderId), paymentId: 1, orderStatus: 'REFUNDED', paymentStatus: 'REFUNDED', voidedCouponCount: 2 }),
  ),
  http.get('/api/admin/members/:memberId/coupons', () => ok(coupons)),
  http.get('/api/admin/members/:memberId/coupon-histories', () => ok(histories)),
  http.get('/api/admin/coupon-consistency', () => ok(buildCouponConsistency())),
  http.post('/api/shop/members', async ({ request }) => {
    const body = await request.json() as any
    const member = { id: 3, name: body.name, email: body.email }
    members = [...members, member]
    return ok(member)
  }),
  http.get('/api/shop/products', () => ok(products)),
  http.post('/api/shop/orders', async ({ request }) => {
    const body = await request.json() as any
    const order = buildOrder(body.memberId, body.lines[0].productId)
    orders = [order]
    return ok(order)
  }),
  http.post('/api/shop/orders/:orderId/pay', ({ params }) => ok(payOrder(Number(params.orderId)))),
  http.get('/api/shop/members/:memberId/coupons', () => ok(coupons)),
  http.get('/api/shop/members/:memberId/coupon-histories', () => ok(histories)),
)

describe('split apps', () => {
  beforeAll(() => server.listen())
  afterEach(() => {
    cleanup()
    server.resetHandlers()
    window.history.pushState(null, '', '/')
    members = [{ id: 1, name: 'Kim', email: 'kim@example.com' }]
    orders = []
    coupons = []
    histories = []
  })
  afterAll(() => server.close())

  it('renders the admin app with operator navigation', async () => {
    renderPage(<AdminApp />)

    await screen.findByRole('heading', { name: '운영 대시보드' })
    expect(screen.getByRole('button', { name: /^회원 관리$/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^상품 관리$/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^주문 관리$/ })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /^회원 관리$/ }))
    await screen.findByRole('heading', { name: '회원 관리' })
    await screen.findByRole('heading', { name: '쿠폰 정합성 리포트' })
    expect(screen.getByText('발급 이력 0')).toBeInTheDocument()
  })

  it('renders the shop app without admin navigation and buys a product with coupon count', async () => {
    renderPage(<ShopApp />)

    await screen.findByRole('heading', { name: '쇼핑몰' })
    expect(screen.queryByRole('button', { name: /주문\/결제/ })).not.toBeInTheDocument()

    await userEvent.type(screen.getByLabelText('이름'), 'Lee')
    await userEvent.type(screen.getByLabelText('이메일'), 'lee@example.com')
    await userEvent.click(screen.getByRole('button', { name: /가입/ }))
    await screen.findByText('회원 #3 가입')

    await screen.findByText('Americano')
    await userEvent.click(screen.getByRole('button', { name: /Americano 구매/ }))
    await screen.findByText('결제 완료: 쿠폰 2장 적립')

    await waitFor(() => expect(screen.getByText('보유 쿠폰 2장')).toBeInTheDocument())
  })
})

function renderPage(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  render(
    <QueryClientProvider client={queryClient}>
      {ui}
    </QueryClientProvider>,
  )
}

function buildOrder(memberId: number, productId: number) {
  return {
    id: 1,
    memberId,
    status: 'CREATED',
    totalAmount: 12000,
    currency: 'KRW',
    lines: [{ productId, productName: 'Americano', unitPrice: 12000, quantity: 1, lineAmount: 12000 }],
  }
}

function payOrder(orderId: number) {
  orders = orders.map((order) => order.id === orderId ? { ...order, status: 'PAID', paymentId: 1 } : order)
  coupons = [
    { id: 1, memberId: orders[0].memberId, orderId, paymentId: 1, status: 'ISSUED' },
    { id: 2, memberId: orders[0].memberId, orderId, paymentId: 1, status: 'ISSUED' },
  ]
  histories = [
    { id: 1, couponId: 1, memberId: orders[0].memberId, orderId, paymentId: 1, type: 'ISSUED' },
    { id: 2, couponId: 2, memberId: orders[0].memberId, orderId, paymentId: 1, type: 'ISSUED' },
  ]
  return { orderId, paymentId: 1, orderStatus: 'PAID', paymentStatus: 'AUTHORIZED', paidAmount: 12000, issuedCouponCount: 2 }
}

function buildCouponConsistency() {
  const memberIds = Array.from(new Set([...coupons.map((coupon) => coupon.memberId), ...histories.map((history) => history.memberId)]))
  const orderIds = Array.from(new Set([...coupons.map((coupon) => coupon.orderId), ...histories.map((history) => history.orderId)]))
  return {
    consistent: true,
    totalCouponCount: coupons.length,
    totalIssueHistoryCount: histories.filter((history) => history.type === 'ISSUED').length,
    totalVoidHistoryCount: histories.filter((history) => history.type === 'VOIDED').length,
    totalExchangeHistoryCount: histories.filter((history) => history.type === 'EXCHANGED').length,
    memberRows: memberIds.map((memberId) => {
      const memberCoupons = coupons.filter((coupon) => coupon.memberId === memberId)
      const memberHistories = histories.filter((history) => history.memberId === memberId)
      const issuedCouponCount = memberCoupons.filter((coupon) => coupon.status === 'ISSUED').length
      return {
        memberId,
        issuedCouponCount,
        voidedCouponCount: memberCoupons.filter((coupon) => coupon.status === 'VOIDED').length,
        exchangedCouponCount: memberCoupons.filter((coupon) => coupon.status === 'EXCHANGED').length,
        issueHistoryCount: memberHistories.filter((history) => history.type === 'ISSUED').length,
        voidHistoryCount: memberHistories.filter((history) => history.type === 'VOIDED').length,
        exchangeHistoryCount: memberHistories.filter((history) => history.type === 'EXCHANGED').length,
        exchangeableSetCount: Math.floor(issuedCouponCount / 10),
        remainingToNextExchange: (10 - issuedCouponCount % 10) % 10,
        consistent: true,
      }
    }),
    orderRows: orderIds.map((orderId) => {
      const orderCoupons = coupons.filter((coupon) => coupon.orderId === orderId)
      const orderHistories = histories.filter((history) => history.orderId === orderId)
      return {
        orderId,
        memberId: orderCoupons[0]?.memberId ?? orderHistories[0]?.memberId ?? 0,
        issuedCouponCount: orderCoupons.filter((coupon) => coupon.status === 'ISSUED').length,
        voidedCouponCount: orderCoupons.filter((coupon) => coupon.status === 'VOIDED').length,
        exchangedCouponCount: orderCoupons.filter((coupon) => coupon.status === 'EXCHANGED').length,
        issueHistoryCount: orderHistories.filter((history) => history.type === 'ISSUED').length,
        voidHistoryCount: orderHistories.filter((history) => history.type === 'VOIDED').length,
        exchangeHistoryCount: orderHistories.filter((history) => history.type === 'EXCHANGED').length,
        consistent: true,
      }
    }),
  }
}

function ok(data: unknown) {
  return HttpResponse.json({ code: 'SUCCESS', message: '요청이 성공했습니다.', data })
}
