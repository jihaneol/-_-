import { request } from '../../shared/api/client'
import type { ApproveCouponExchangeResult, Coupon, CouponConsistencyReport, CouponExchangeResult, CouponHistory, CouponWallet, DashboardSummary, Inventory, Member, Order, PayOrderResult, Product, RefundOrderResult } from './types'

export const adminCommerceKeys = {
  summary: ['admin', 'commerce', 'dashboard', 'summary'] as const,
  members: ['admin', 'commerce', 'members'] as const,
  products: ['admin', 'commerce', 'products'] as const,
  orders: ['admin', 'commerce', 'orders'] as const,
  couponConsistency: ['admin', 'commerce', 'coupon-consistency'] as const,
  couponsIdle: ['admin', 'commerce', 'coupons', 'idle'] as const,
  historiesIdle: ['admin', 'commerce', 'coupon-histories', 'idle'] as const,
  coupons: (memberId: number) => ['admin', 'commerce', 'coupons', memberId] as const,
  histories: (memberId: number) => ['admin', 'commerce', 'coupon-histories', memberId] as const,
  inventory: (productId: number) => ['admin', 'commerce', 'inventory', productId] as const,
}

export const shopCommerceKeys = {
  products: ['shop', 'commerce', 'products'] as const,
  walletIdle: ['shop', 'commerce', 'coupon-wallet', 'idle'] as const,
  couponsIdle: ['shop', 'commerce', 'coupons', 'idle'] as const,
  historiesIdle: ['shop', 'commerce', 'coupon-histories', 'idle'] as const,
  wallet: (memberId: number) => ['shop', 'commerce', 'coupon-wallet', memberId] as const,
  coupons: (memberId: number) => ['shop', 'commerce', 'coupons', memberId] as const,
  histories: (memberId: number) => ['shop', 'commerce', 'coupon-histories', memberId] as const,
}

export const adminCommerceApi = {
  getDashboardSummary: () => request<DashboardSummary>('/api/admin/dashboard/summary'),
  listMembers: () => request<Member[]>('/api/admin/members'),
  createMember: (body: { name: string; email: string }) =>
    request<Member>('/api/admin/members', { method: 'POST', body: JSON.stringify(body) }),
  listProducts: () => request<Product[]>('/api/admin/products'),
  createProduct: (body: { name: string; price: number }) =>
    request<Product>('/api/admin/products', { method: 'POST', body: JSON.stringify(body) }),
  createInventory: (productId: number, body: { quantity: number }) =>
    request<Inventory>(`/api/admin/products/${productId}/inventory`, { method: 'POST', body: JSON.stringify(body) }),
  increaseInventory: (productId: number, body: { quantity: number }) =>
    request<Inventory>(`/api/admin/products/${productId}/inventory/increase`, { method: 'POST', body: JSON.stringify(body) }),
  getInventory: (productId: number) => request<Inventory>(`/api/admin/products/${productId}/inventory`),
  listOrders: () => request<Order[]>('/api/admin/orders'),
  createOrder: (body: { memberId: number; lines: Array<{ productId: number; quantity: number }> }) =>
    request<Order>('/api/admin/orders', { method: 'POST', body: JSON.stringify(body) }),
  cancelOrder: (orderId: number) =>
    request<Order>(`/api/admin/orders/${orderId}/cancel`, { method: 'POST' }),
  payOrder: (orderId: number, body: { idempotencyKey: string }) =>
    request<PayOrderResult>(`/api/admin/orders/${orderId}/pay`, { method: 'POST', body: JSON.stringify(body) }),
  refundOrder: (orderId: number) =>
    request<RefundOrderResult>(`/api/admin/orders/${orderId}/refund`, { method: 'POST' }),
  listCoupons: (memberId: number) => request<Coupon[]>(`/api/admin/members/${memberId}/coupons`),
  listCouponHistories: (memberId: number) => request<CouponHistory[]>(`/api/admin/members/${memberId}/coupon-histories`),
  getCouponConsistencyReport: () => request<CouponConsistencyReport>('/api/admin/coupon-consistency'),
  exchangeCoupon: (couponId: number) =>
    request<CouponExchangeResult>(`/api/admin/coupons/${couponId}/exchange`, { method: 'POST' }),
  approveCouponExchange: (memberId: number, body: { productId: number }) =>
    request<ApproveCouponExchangeResult>(`/api/admin/members/${memberId}/coupon-exchanges`, { method: 'POST', body: JSON.stringify(body) }),
}

export const shopCommerceApi = {
  createMember: (body: { name: string; email: string }) =>
    request<Member>('/api/shop/members', { method: 'POST', body: JSON.stringify(body) }),
  listProducts: () => request<Product[]>('/api/shop/products'),
  createOrder: (body: { memberId: number; lines: Array<{ productId: number; quantity: number }> }) =>
    request<Order>('/api/shop/orders', { method: 'POST', body: JSON.stringify(body) }),
  payOrder: (orderId: number, body: { idempotencyKey: string }) =>
    request<PayOrderResult>(`/api/shop/orders/${orderId}/pay`, { method: 'POST', body: JSON.stringify(body) }),
  getCouponWallet: (memberId: number) => request<CouponWallet>(`/api/shop/members/${memberId}/coupon-wallet`),
  listCoupons: (memberId: number) => request<Coupon[]>(`/api/shop/members/${memberId}/coupons`),
  listCouponHistories: (memberId: number) => request<CouponHistory[]>(`/api/shop/members/${memberId}/coupon-histories`),
}
