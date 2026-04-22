import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Dashboard() {
  const { user } = useAuth();
  const [wallet, setWallet]   = useState(null);
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setWallet(null);
    setOrders([]);
    setLoading(true);
    Promise.all([
      api.get('/api/wallet'),
      api.get('/api/orders/my-orders?page=0&size=5')
    ]).then(([w, o]) => {
      setWallet(w.data.data);
      setOrders(o.data.data?.content || []);
    }).catch(() => {}).finally(() => setLoading(false));
  }, [user?.id]);

  const statusBadge = (s) => {
    const map = { DELIVERED: 'badge-green', CONFIRMED: 'badge-blue', PENDING: 'badge-orange', CANCELLED: 'badge-red', REFUNDED: 'badge-gray' };
    return <span className={`badge ${map[s] || 'badge-gray'}`}>{s}</span>;
  };

  const balance = wallet?.balance ?? wallet?.walletBalance ?? 0;
  const accountNumber = wallet?.accountNumber ?? '—';

  if (loading) return <div className="spinner-wrap"><div className="spinner" /></div>;

  return (
    <div className="page">
      <div className="page-title">Welcome back, {user?.username} 👋</div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="label">Wallet Balance</div>
          <div className="value">₹{Number(balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
        </div>
        <div className="stat-card" style={{ borderLeftColor: '#66bb6a' }}>
          <div className="label">Account Number</div>
          <div className="value" style={{ fontSize: '1rem' }}>{accountNumber}</div>
        </div>
        <div className="stat-card" style={{ borderLeftColor: '#ffa726' }}>
          <div className="label">Total Orders</div>
          <div className="value">{orders.length}</div>
        </div>
        <div className="stat-card" style={{ borderLeftColor: '#ab47bc' }}>
          <div className="label">Role</div>
          <div className="value" style={{ fontSize: '1rem' }}>{user?.roles?.[0]?.replace('ROLE_', '') || 'USER'}</div>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <strong>Recent Orders</strong>
          <Link to="/orders" style={{ fontSize: '0.85rem', color: '#1565c0' }}>View all →</Link>
        </div>
        {orders.length === 0 ? (
          <div className="empty">No orders yet. <Link to="/products">Browse products</Link></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Order #</th><th>Amount</th><th>Payment</th><th>Status</th></tr></thead>
              <tbody>
                {orders.map(o => (
                  <tr key={o.id}>
                    <td>{o.orderNumber}</td>
                    <td>₹{Number(o.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                    <td>{o.paymentMethod}</td>
                    <td>{statusBadge(o.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', flexWrap: 'wrap' }}>
        <Link to="/products" className="btn btn-primary" style={{ width: 'auto' }}>🛒 Browse Products</Link>
        <Link to="/wallet"   className="btn btn-outline" style={{ width: 'auto' }}>💳 Manage Wallet</Link>
      </div>
    </div>
  );
}
