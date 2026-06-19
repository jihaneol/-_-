import { CreditCard, Home, Package, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import { MainPage } from '../../src/pages/main/MainPage'
import { MembersPage } from '../../src/pages/members/MembersPage'
import { OrdersPaymentsPage } from '../../src/pages/orders-payments/OrdersPaymentsPage'
import { ProductsPage } from '../../src/pages/products/ProductsPage'

const routes = [
  { path: '/', label: '메인', icon: Home },
  { path: '/members', label: '회원', icon: Users },
  { path: '/products', label: '상품', icon: Package },
  { path: '/orders-payments', label: '주문/결제', icon: CreditCard },
]

export function AdminApp() {
  const [path, setPath] = useState(window.location.pathname)

  useEffect(() => {
    const onPopState = () => setPath(window.location.pathname)
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  const navigate = (nextPath: string) => {
    window.history.pushState(null, '', nextPath)
    setPath(nextPath)
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <strong>Coupon Admin</strong>
          <span>도장 쿠폰 운영</span>
        </div>
        <nav className="nav" aria-label="관리자 메뉴">
          {routes.map((route) => {
            const Icon = route.icon
            return (
              <button
                key={route.path}
                className={path === route.path ? 'active' : ''}
                onClick={() => navigate(route.path)}
              >
                <Icon size={17} />
                {route.label}
              </button>
            )
          })}
        </nav>
      </aside>
      <main className="workspace">
        {path === '/members' ? <MembersPage /> : null}
        {path === '/products' ? <ProductsPage /> : null}
        {path === '/orders-payments' ? <OrdersPaymentsPage /> : null}
        {!routes.some((route) => route.path === path && route.path !== '/') ? <MainPage /> : null}
      </main>
    </div>
  )
}
