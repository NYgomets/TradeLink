import { Outlet } from 'react-router-dom'
import Header from './Header'

export default function Layout() {
  return (
    <div className="min-h-screen bg-bg-primary text-text-primary font-sans">
      <Header />
      <main className="mx-auto max-w-7xl px-6 pt-20 pb-12">
        <Outlet />
      </main>
    </div>
  )
}
