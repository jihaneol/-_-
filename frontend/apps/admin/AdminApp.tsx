import { CreditCard, Home, Package, Settings, Stamp, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { adminCommerceApi } from '../../src/entities/commerce/api'
import { MainPage } from '../../src/pages/main/MainPage'
import { MembersPage } from '../../src/pages/members/MembersPage'
import { OrdersPaymentsPage } from '../../src/pages/orders-payments/OrdersPaymentsPage'
import { ProductsPage } from '../../src/pages/products/ProductsPage'
import { setApiAuthToken } from '../../src/shared/api/client'

const routes = [
  { path: '/', label: '대시보드', icon: Home, title: '운영 대시보드', description: '매출, 주문, 쿠폰 적립과 교환 상태를 한 화면에서 확인합니다.' },
  { path: '/members', label: '회원 관리', icon: Users, title: '회원 관리', description: '회원별 누적 구매, 쿠폰 보유량, 교환 가능 여부를 관리합니다.' },
  { path: '/products', label: '상품 관리', icon: Package, title: '상품 관리', description: '일반 판매 상품과 5,000원 교환 상품을 관리합니다.' },
  { path: '/orders-payments', label: '주문 관리', icon: CreditCard, title: '주문/결제 관리', description: '결제 주문, 취소 요청, 쿠폰 교환 주문을 함께 추적합니다.' },
]

const secondaryRoutes = [
  { label: '결제 관리', icon: CreditCard },
  { label: '쿠폰/스탬프', icon: Stamp },
  { label: '설정', icon: Settings },
]

export function AdminApp() {
  const [path, setPath] = useState(window.location.pathname)
  const [authenticated, setAuthenticated] = useState(false)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loginError, setLoginError] = useState('')

  useEffect(() => {
    const onPopState = () => setPath(window.location.pathname)
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  const navigate = (nextPath: string) => {
    window.history.pushState(null, '', nextPath)
    setPath(nextPath)
  }
  const currentRoute = routes.find((route) => route.path === path) ?? routes[0]

  const login = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setLoginError('')
    try {
      const auth = await adminCommerceApi.login({ username, password })
      setApiAuthToken(auth.accessToken)
      setAuthenticated(true)
    } catch {
      setLoginError('관리자 아이디 또는 비밀번호를 확인하세요.')
    }
  }

  if (!authenticated) {
    return (
      <main className="workspace admin-login">
        <section className="panel">
          <h1>관리자 로그인</h1>
          <p>운영 API는 관리자 토큰으로만 접근할 수 있습니다.</p>
          {loginError ? <div className="notice error">{loginError}</div> : null}
          <form className="grid" onSubmit={login}>
            <label className="field">
              <span>아이디</span>
              <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="admin" />
            </label>
            <label className="field">
              <span>비밀번호</span>
              <input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="password1" type="password" />
            </label>
            <button className="button" type="submit">로그인</button>
          </form>
        </section>
      </main>
    )
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <strong>Stamp Mall Admin</strong>
          <span>쿠폰 교환 운영센터</span>
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
          {secondaryRoutes.map((route) => {
            const Icon = route.icon
            return (
              <button key={route.label} className="inactive" disabled>
                <Icon size={17} />
                {route.label}
              </button>
            )
          })}
        </nav>
        <div className="sidebar-help">
          <strong>교환 규칙</strong>
          <span>5,000원 단위 구매마다 쿠폰 1장</span>
          <span>10장 적립 시 5,000원 상품 교환</span>
        </div>
      </aside>
      <main className="workspace">
        <header className="topbar">
          <div>
            <h1>{currentRoute.title}</h1>
            <p>{currentRoute.description}</p>
          </div>
          <div className="topbar-actions">
            <label className="topbar-search">
              <span className="sr-only">검색</span>
              <input placeholder="검색어를 입력하세요" />
            </label>
            <div className="avatar" aria-label="관리자">관</div>
          </div>
        </header>
        <div className="workspace-content">
          {path === '/members' ? <MembersPage /> : null}
          {path === '/products' ? <ProductsPage /> : null}
          {path === '/orders-payments' ? <OrdersPaymentsPage /> : null}
          {!routes.some((route) => route.path === path && route.path !== '/') ? <MainPage /> : null}
        </div>
      </main>
    </div>
  )
}
