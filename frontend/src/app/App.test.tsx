import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { AdminApp } from '../../apps/admin/AdminApp'
import { ShopApp } from '../../apps/shop/ShopApp'

let members = [{ id: 1, name: 'Kim', email: 'kim@example.com' }]
let products = [buildProduct(1, 'Americano', 12000)]
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
    const product = buildProduct(2, body.name, body.price)
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
  http.get('/api/shop/members/:memberId/coupon-wallet', ({ params }) => ok(buildCouponWallet(Number(params.memberId)))),
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
    products = [buildProduct(1, 'Americano', 12000)]
    orders = []
    coupons = []
    histories = []
  })
  afterAll(() => server.close())

  it('renders the admin app with operator navigation', async () => {
    coupons = [
      ...Array.from({ length: 10 }).map((_, index) => ({ id: index + 1, memberId: 1, orderId: 10, paymentId: 20, status: 'ISSUED' })),
      { id: 11, memberId: 1, orderId: 11, paymentId: 21, status: 'EXCHANGED' },
      { id: 12, memberId: 1, orderId: 12, paymentId: 22, status: 'VOIDED' },
    ]
    histories = [
      ...Array.from({ length: 10 }).map((_, index) => ({ id: index + 1, couponId: index + 1, memberId: 1, orderId: 10, paymentId: 20, type: 'ISSUED' })),
      { id: 11, couponId: 11, memberId: 1, orderId: 11, paymentId: 21, type: 'EXCHANGED' },
      { id: 12, couponId: 12, memberId: 1, orderId: 12, paymentId: 22, type: 'VOIDED' },
    ]

    renderPage(<AdminApp />)

    await screen.findByRole('heading', { name: '운영 대시보드' })
    expect(screen.getByRole('button', { name: /^회원 관리$/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^상품 관리$/ })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^주문 관리$/ })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /^회원 관리$/ }))
    await screen.findByRole('heading', { name: '회원 관리' })
    await screen.findByRole('heading', { name: '쿠폰 정합성 리포트' })
    expect(screen.getByText('회수 이력 1')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: '조회' }))
    expect(screen.getByLabelText('쿠폰 상태 기준')).toBeInTheDocument()
    expect(screen.getByText((_, element) => element?.textContent?.replace(/\s+/g, ' ').trim() === '교환 가능 1세트')).toBeInTheDocument()
    expect(screen.getAllByText('적립 중').length).toBeGreaterThan(0)
    expect(screen.getAllByText('교환 완료').length).toBeGreaterThan(0)
    expect(screen.getAllByText('회수').length).toBeGreaterThan(0)
  })

  it('renders the shop app without admin navigation and buys a product with coupon count', async () => {
    renderPage(<ShopApp />)

    await screen.findByRole('heading', { name: '커피 주문을 키오스크처럼 빠르게 끝내세요' })
    expect(screen.getByText('매장 주문 모드')).toBeInTheDocument()
    expect(screen.getAllByText('메뉴 선택').length).toBeGreaterThan(0)
    expect(screen.queryByRole('button', { name: /주문\/결제/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '05 프로그램' })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: '혜택' }))
    await screen.findByRole('heading', { name: '쿠폰 적립 안내' })
    await userEvent.click(screen.getByRole('button', { name: '상품' }))
    await screen.findByRole('heading', { name: '상품 목록' })
    expect(screen.getByRole('button', { name: '전체 메뉴' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: '홈' }))
    await screen.findByRole('heading', { name: '커피 주문을 키오스크처럼 빠르게 끝내세요' })
    expect(screen.queryByRole('heading', { name: '회원 시작' })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /^로그인$/ }))
    await screen.findByRole('heading', { name: '로그인' })
    expect(screen.getByRole('heading', { name: '회원 시작' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: '가입하기' }))
    await screen.findByRole('heading', { name: '회원 가입' })
    await userEvent.type(screen.getByLabelText('이름'), 'Lee')
    await userEvent.type(screen.getByLabelText('이메일'), 'lee@example.com')
    await userEvent.click(screen.getByRole('button', { name: /^가입$/ }))
    await screen.findByText('회원 #3 가입')

    await screen.findByText('Americano')
    await userEvent.click(screen.getByRole('button', { name: /Americano 담기/ }))
    await screen.findByRole('heading', { name: '장바구니' })
    expect(screen.getByText('온도')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /ICE/ })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /결제로 이동/ }))
    await screen.findByRole('heading', { name: '픽업 결제' })
    expect(screen.getByDisplayValue('매장 픽업')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /결제하기/ }))
    await screen.findByText('결제 완료: 쿠폰 2장 적립')

    await screen.findByRole('heading', { name: '마이페이지' })
    await waitFor(() => expect(screen.getByText('2 / 10장')).toBeInTheDocument())
    expect(screen.getByText('다음 교환까지 8장 남았습니다.')).toBeInTheDocument()
    expect(screen.getByText('교환 가능 0세트')).toBeInTheDocument()
    expect(screen.getByText('교환 완료 0')).toBeInTheDocument()
    expect(screen.getByText('회수 0')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: '쿠폰 정합성 리포트' })).not.toBeInTheDocument()
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

function buildProduct(id: number, name: string, price: number) {
  return {
    id,
    name,
    price,
    saleStatus: 'ON_SALE',
    couponAccrualCount: Math.floor(price / 5_000),
    exchangeEligible: price === 5_000,
  } as const
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

function buildCouponWallet(memberId: number) {
  const memberCoupons = coupons.filter((coupon) => coupon.memberId === memberId)
  const issuedCouponCount = memberCoupons.filter((coupon) => coupon.status === 'ISSUED').length
  return {
    memberId,
    issuedCouponCount,
    exchangedCouponCount: memberCoupons.filter((coupon) => coupon.status === 'EXCHANGED').length,
    voidedCouponCount: memberCoupons.filter((coupon) => coupon.status === 'VOIDED').length,
    totalCouponCount: memberCoupons.length,
    exchangeableSetCount: Math.floor(issuedCouponCount / 10),
    remainingToNextExchange: (10 - issuedCouponCount % 10) % 10,
    recentHistories: histories.filter((history) => history.memberId === memberId).slice().reverse(),
  }
}

function ok(data: unknown) {
  return HttpResponse.json({ code: 'SUCCESS', message: '요청이 성공했습니다.', data })
}
