import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Wallet() {
  const [wallet, setWallet]   = useState(null);
  const { user } = useAuth();
  const [txns, setTxns]       = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert]     = useState(null);
  const [modal, setModal]     = useState(null);
  const [amount, setAmount]   = useState('');
  const [toAccount, setToAccount] = useState('');
  const [desc, setDesc]       = useState('');
  const [submitting, setSubmitting] = useState(false);

  const showAlert = (msg, type = 'success') => {
    setAlert({ msg, type });
    setTimeout(() => setAlert(null), 4000);
  };

  const extractError = (err) => {
    return err.response?.data?.message || err.response?.data?.errors
      ? (typeof err.response.data.errors === 'object'
          ? Object.values(err.response.data.errors).join(', ')
          : err.response.data.message)
      : err.message || 'Something went wrong';
  };

  const loadWallet = async () => {
    try {
      const { data } = await api.get('/api/wallet');
      setWallet(data.data);
    } catch (err) {
      showAlert(extractError(err), 'error');
    }
  };

  const loadTxns = useCallback(async () => {
    try {
      const { data } = await api.get(`/api/wallet/transactions?page=${page}&size=10`);
      setTxns(data.data?.content || []);
      setTotal(data.data?.totalPages || 0);
    } catch (err) {
      showAlert(extractError(err), 'error');
    }
  }, [page]);

  useEffect(() => {
    setLoading(true);
    setWallet(null);
    setTxns([]);
    Promise.all([loadWallet(), loadTxns()]).finally(() => setLoading(false));
  }, [loadTxns, user?.id]);

  const topUp = async () => {
    if (!amount || Number(amount) <= 0) { showAlert('Enter a valid amount', 'error'); return; }
    setSubmitting(true);
    try {
      await api.post(`/api/wallet/topup?amount=${amount}${desc ? `&description=${encodeURIComponent(desc)}` : ''}`);
      showAlert(`₹${Number(amount).toLocaleString('en-IN')} added to wallet!`);
      setModal(null); setAmount(''); setDesc('');
      await loadWallet();
      await loadTxns();
    } catch (err) {
      showAlert(extractError(err), 'error');
    } finally { setSubmitting(false); }
  };

  const transfer = async () => {
    if (!amount || Number(amount) <= 0) { showAlert('Enter a valid amount', 'error'); return; }
    if (!toAccount.trim()) { showAlert('Enter recipient account number', 'error'); return; }
    if (toAccount.trim() === wallet?.accountNumber) { showAlert('Cannot transfer to your own account', 'error'); return; }
    setSubmitting(true);
    try {
      await api.post(`/api/wallet/transfer?toAccountNumber=${toAccount.trim()}&amount=${amount}${desc ? `&description=${encodeURIComponent(desc)}` : ''}`);
      showAlert('Transfer successful!');
      setModal(null); setAmount(''); setToAccount(''); setDesc('');
      await loadWallet();
      await loadTxns();
    } catch (err) {
      showAlert(extractError(err), 'error');
    } finally { setSubmitting(false); }
  };

  const txnBadge = (type) => {
    const map = { CREDIT: 'badge-green', DEBIT: 'badge-red', REFUND: 'badge-blue', TRANSFER: 'badge-orange' };
    return <span className={`badge ${map[type] || 'badge-gray'}`}>{type}</span>;
  };

  const balance = wallet?.balance ?? wallet?.walletBalance ?? 0;

  if (loading) return <div className="spinner-wrap"><div className="spinner" /></div>;

  return (
    <div className="page">
      <div className="page-title">Wallet</div>
      {alert && <div className={`alert alert-${alert.type}`}>{alert.msg}</div>}

      <div className="wallet-hero">
        <div className="balance-label">Available Balance</div>
        <div className="balance-amount">₹{Number(balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
        <div className="account">Account: {wallet?.accountNumber}</div>
      </div>

      <div className="wallet-actions">
        <button className="btn btn-success" onClick={() => { setModal('topup'); setAmount(''); setDesc(''); }}>+ Top Up</button>
        <button className="btn btn-outline" onClick={() => { setModal('transfer'); setAmount(''); setToAccount(''); setDesc(''); }}>↗ Transfer</button>
      </div>

      <div className="card">
        <strong style={{ display: 'block', marginBottom: '1rem' }}>Transaction History</strong>
        {txns.length === 0 ? (
          <div className="empty">No transactions yet.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Type</th><th>Amount</th><th>Description</th><th>Balance After</th><th>Date</th></tr></thead>
              <tbody>
                {txns.map(t => (
                  <tr key={t.id}>
                    <td>{txnBadge(t.type)}</td>
                    <td style={{ fontWeight: 600, color: t.type === 'DEBIT' || t.type === 'TRANSFER' ? '#c62828' : '#2e7d32' }}>
                      {t.type === 'DEBIT' || t.type === 'TRANSFER' ? '-' : '+'}₹{Number(t.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td>{t.description || '—'}</td>
                    <td>₹{Number(t.balanceAfter || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                    <td>{t.createdAt ? new Date(t.createdAt).toLocaleString('en-IN') : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
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
      </div>

      {modal === 'topup' && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Top Up Wallet</h3>
            <div className="form-group">
              <label>Amount (₹)</label>
              <input type="number" min="1" max="1000000" value={amount} onChange={e => setAmount(e.target.value)} placeholder="500" />
            </div>
            <div className="form-group">
              <label>Description (optional)</label>
              <input value={desc} onChange={e => setDesc(e.target.value)} placeholder="Salary credit" />
            </div>
            <div className="modal-actions">
              <button className="btn btn-outline btn-sm" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-success btn-sm" onClick={topUp} disabled={!amount || submitting}>
                {submitting ? 'Processing...' : 'Add Money'}
              </button>
            </div>
          </div>
        </div>
      )}

      {modal === 'transfer' && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Transfer Funds</h3>
            <div className="form-group">
              <label>Recipient Account Number</label>
              <input value={toAccount} onChange={e => setToAccount(e.target.value)} placeholder="BFSI123456789" />
            </div>
            <div className="form-group">
              <label>Amount (₹)</label>
              <input type="number" min="1" value={amount} onChange={e => setAmount(e.target.value)} placeholder="100" />
            </div>
            <div className="form-group">
              <label>Description (optional)</label>
              <input value={desc} onChange={e => setDesc(e.target.value)} placeholder="Rent payment" />
            </div>
            <p style={{ fontSize: '0.82rem', color: '#888', marginBottom: '0.5rem' }}>
      Your balance: ₹{Number(balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
            <div className="modal-actions">
              <button className="btn btn-outline btn-sm" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary btn-sm" onClick={transfer} disabled={!amount || !toAccount || submitting}>
                {submitting ? 'Processing...' : 'Transfer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
