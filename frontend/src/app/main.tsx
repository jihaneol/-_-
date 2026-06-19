import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { createRoot } from 'react-dom/client'
import { CommerceDashboardPage } from '../pages/commerce-dashboard/CommerceDashboardPage'
import './styles/global.css'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <CommerceDashboardPage />
    </QueryClientProvider>
  </React.StrictMode>,
)
