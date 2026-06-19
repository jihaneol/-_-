import { request } from '../../shared/api/client'
import type { Coupon, CouponHistory, DashboardSummary, Inventory, Member, Order, PayOrderResult, Product, RefundOrderResult } from './types'

export const commerceKeys = {
  summary: ['commerce', 'dashboard', 'summary'] as const,
  members: ['commerce', 'members'] as const,
  products: ['commerce', 'products'] as const,
  orders: ['commerce', 'orders'] as const,
  coupons: (memberId: number) => ['commerce', 'coupons', memberId] as const,
  histories: (memberId: number) => ['commerce', 'coupon-histories', memberId] as const,
  inventory: (productId: number) => ['commerce', 'inventory', productId] as const,
}

export const commerceApi = {
  getDashboardSummary: () => request<DashboardSummary>('/api/dashboard/summary'),
  listMembers: () => request<Member[]>('/api/members'),
  createMember: (body: { name: string; email: string }) =>
    request<Member>('/api/members', { method: 'POST', body: JSON.stringify(body) }),
  listProducts: () => request<Product[]>('/api/products'),
  createProduct: (body: { name: string; price: number }) =>
    request<Product>('/api/products', { method: 'POST', body: JSON.stringify(body) }),
  createInventory: (productId: number, body: { quantity: number }) =>
    request<Inventory>(`/api/products/${productId}/inventory`, { method: 'POST', body: JSON.stringify(body) }),
  increaseInventory: (productId: number, body: { quantity: number }) =>
    request<Inventory>(`/api/products/${productId}/inventory/increase`, { method: 'POST', body: JSON.stringify(body) }),
  getInventory: (productId: number) => request<Inventory>(`/api/products/${productId}/inventory`),
  listOrders: () => request<Order[]>('/api/orders'),
  createOrder: (body: { memberId: number; lines: Array<{ productId: number; quantity: number }> }) =>
    request<Order>('/api/orders', { method: 'POST', body: JSON.stringify(body) }),
  cancelOrder: (orderId: number) =>
    request<Order>(`/api/orders/${orderId}/cancel`, { method: 'POST' }),
  payOrder: (orderId: number, body: { idempotencyKey: string }) =>
    request<PayOrderResult>(`/api/orders/${orderId}/pay`, { method: 'POST', body: JSON.stringify(body) }),
  refundOrder: (orderId: number) =>
    request<RefundOrderResult>(`/api/orders/${orderId}/refund`, { method: 'POST' }),
  listCoupons: (memberId: number) => request<Coupon[]>(`/api/members/${memberId}/coupons`),
  listCouponHistories: (memberId: number) => request<CouponHistory[]>(`/api/members/${memberId}/coupon-histories`),
}
