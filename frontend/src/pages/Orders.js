import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const STATUS_BADGE = { DELIVERED: 'badge-green', CONFIRMED: 'badge-blue', PENDING: 'badge-orange', PAYMENT_SUCCESS: 'badge-blue', CANCELLED: 'badge-red', REFUNDED: 'badge-gray', SHIPPED: 'badge-blue' };

export default function Orders() {
  const { isAdmin, isBanker } = useAuth();
  const [orders, setOrders]   = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert]     = useState(null);
  const [statusModal, setStatusModal] = useState(null);
  const [newStatus, setNewStatus]     = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get(`/api/orders/my-orders?page=${page}&size=10`);
      setOrders(data.data?.content || []);
      setTotal(data.data?.totalPages || 0);
    } finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const showAlert = (msg, type = 'success') => {
    setAlert({ msg, type });
    setTimeout(() => setAlert(null), 3000);
  };

  const cancel = async (id) => {
    if (!window.confirm('Cancel this order?')) return;
    try {
      await api.patch(`/api/orders/${id}/cancel`);
      showAlert('Order cancelled. Refund initiated.');
      load();
    } catch (err) { showAlert(err.response?.data?.message || 'Failed', 'error'); }
  };

  const updateStatus = async () => {
    try {
      await api.patch(`/api/orders/admin/${statusModal}/status?status=${newStatus}`);
      showAlert('Status updated');
      setStatusModal(null);
      load();
    } catch (err) { showAlert(err.response?.data?.message || 'Failed', 'error'); }
  };

  return (
    <div className="page">
      <div className="page-title">My Orders</div>
      {alert && <div className={`alert alert-${alert.type}`}>{alert.msg}</div>}

      {loading ? (
        <div className="spinner-wrap"><div className="spinner" /></div>
      ) : orders.length === 0 ? (
        <div className="empty">No orders found.</div>
      ) : (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Order #</th><th>Items</th><th>Amount</th><th>Payment</th><th>Status</th><th>Date</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(o => (
                  <tr key={o.id}>
                    <td style={{ fontWeight: 600 }}>{o.orderNumber}</td>
                    <td>{o.items?.map(i => `${i.productName} x${i.quantity}`).join(', ') || '—'}</td>
                    <td>₹{Number(o.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                    <td>{o.paymentMethod}</td>
                    <td><span className={`badge ${STATUS_BADGE[o.status] || 'badge-gray'}`}>{o.status}</span></td>
                    <td>{o.createdAt ? new Date(o.createdAt).toLocaleDateString('en-IN') : '—'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.4rem' }}>
                        {['PENDING', 'PAYMENT_PROCESSING', 'CONFIRMED'].includes(o.status) && (
                          <button className="btn btn-danger btn-sm" onClick={() => cancel(o.id)}>Cancel</button>
                        )}
                        {(isAdmin() || isBanker()) && (
                          <button className="btn btn-outline btn-sm" onClick={() => { setStatusModal(o.id); setNewStatus(o.status); }}>
                            Status
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {total > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>‹ Prev</button>
          {[...Array(total)].map((_, i) => (
            <button key={i} className={page === i ? 'active' : ''} onClick={() => setPage(i)}>{i + 1}</button>
          ))}
          <button disabled={page === total - 1} onClick={() => setPage(p => p + 1)}>Next ›</button>
        </div>
      )}

      {statusModal && (
        <div className="modal-overlay" onClick={() => setStatusModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Update Order Status</h3>
            <div className="form-group">
              <label>New Status</label>
              <select value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                {['PENDING','PAYMENT_PROCESSING','PAYMENT_SUCCESS','PAYMENT_FAILED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED','REFUNDED'].map(s => (
                  <option key={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="modal-actions">
              <button className="btn btn-outline btn-sm" onClick={() => setStatusModal(null)}>Cancel</button>
              <button className="btn btn-primary btn-sm" onClick={updateStatus}>Update</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
