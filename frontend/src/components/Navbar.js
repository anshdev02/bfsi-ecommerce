import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAdmin, isBanker } = useAuth();
  const { pathname } = useLocation();

  return (
    <nav className="navbar">
      <Link to="/dashboard" className="navbar-brand">🏦 BFSI Banking</Link>
      <div className="navbar-links">
        <Link to="/dashboard" style={pathname === '/dashboard' ? { color: '#fff' } : {}}>Dashboard</Link>
        <Link to="/products"  style={pathname === '/products'  ? { color: '#fff' } : {}}>Products</Link>
        <Link to="/orders"    style={pathname === '/orders'    ? { color: '#fff' } : {}}>Orders</Link>
        <Link to="/wallet"    style={pathname === '/wallet'    ? { color: '#fff' } : {}}>Wallet</Link>
        <span style={{ color: '#4fc3f7', fontSize: '0.85rem' }}>
          {user?.username} {isAdmin() ? '(Admin)' : isBanker() ? '(Banker)' : ''}
        </span>
        <button onClick={logout}>Logout</button>
      </div>
    </nav>
  );
}
