import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Products() {
  const { isAdmin } = useAuth();
  const [products, setProducts]   = useState([]);
  const [total, setTotal]         = useState(0);
  const [page, setPage]           = useState(0);
  const [keyword, setKeyword]     = useState('');
  const [loading, setLoading]     = useState(true);
  const [modal, setModal]         = useState(null); // 'order' | 'create' | 'edit'
  const [selected, setSelected]   = useState(null);
  const [alert, setAlert]         = useState(null);
  const [orderForm, setOrderForm] = useState({ quantity: 1, paymentMethod: 'WALLET', shippingAddress: '' });
  const [productForm, setProductForm] = useState({ name: '', description: '', price: '', stockQuantity: '', category: '', imageUrl: '' });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const url = keyword
        ? `/api/products/public/search?keyword=${keyword}&page=${page}&size=9`
        : `/api/products/public?page=${page}&size=9`;
      const { data } = await api.get(url);
      setProducts(data.data?.content || []);
      setTotal(data.data?.totalPages || 0);
    } finally { setLoading(false); }
  }, [keyword, page]);

  useEffect(() => { load(); }, [load]);

  const showAlert = (msg, type = 'success') => {
    setAlert({ msg, type });
    setTimeout(() => setAlert(null), 3000);
  };

  const placeOrder = async () => {
    try {
      await api.post('/api/orders', {
        items: [{ productId: selected.id, quantity: Number(orderForm.quantity) }],
        paymentMethod: orderForm.paymentMethod,
        shippingAddress: orderForm.shippingAddress
      });
      showAlert('Order placed successfully!');
      setModal(null);
    } catch (err) {
      showAlert(err.response?.data?.message || 'Order failed', 'error');
    }
  };

  const saveProduct = async () => {
    try {
      const body = { ...productForm, price: Number(productForm.price), stockQuantity: Number(productForm.stockQuantity) };
      if (modal === 'create') {
        await api.post('/api/admin/products', body);
        showAlert('Product created!');
      } else {
        await api.put(`/api/admin/products/${selected.id}`, body);
        showAlert('Product updated!');
      }
      setModal(null);
      load();
    } catch (err) {
      showAlert(err.response?.data?.message || 'Failed', 'error');
    }
  };

  const deleteProduct = async (id) => {
    if (!window.confirm('Deactivate this product?')) return;
    try {
      await api.delete(`/api/admin/products/${id}`);
      showAlert('Product deactivated');
      load();
    } catch { showAlert('Failed', 'error'); }
  };

  const openEdit = (p) => {
    setSelected(p);
    setProductForm({ name: p.name, description: p.description || '', price: p.price, stockQuantity: p.stockQuantity, category: p.category, imageUrl: p.imageUrl || '' });
    setModal('edit');
  };

  const openCreate = () => {
    setProductForm({ name: '', description: '', price: '', stockQuantity: '', category: '', imageUrl: '' });
    setModal('create');
  };

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div className="page-title" style={{ margin: 0 }}>Products</div>
        {isAdmin() && <button className="btn btn-success btn-sm" onClick={openCreate}>+ Add Product</button>}
      </div>

      {alert && <div className={`alert alert-${alert.type}`}>{alert.msg}</div>}

      <div className="search-bar">
        <input placeholder="Search products..." value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} />
      </div>

      {loading ? (
        <div className="spinner-wrap"><div className="spinner" /></div>
      ) : products.length === 0 ? (
        <div className="empty">No products found.</div>
      ) : (
        <div className="products-grid">
          {products.map(p => (
            <div className="product-card" key={p.id}>
              <span className="category">{p.category}</span>
              <h3>{p.name}</h3>
              <div className="desc">{p.description}</div>
              <div className="price">₹{Number(p.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
              <div className="stock">Stock: {p.stockQuantity}</div>
              <div className="actions">
                <button className="btn btn-primary btn-sm" onClick={() => { setSelected(p); setOrderForm({ quantity: 1, paymentMethod: 'WALLET', shippingAddress: '' }); setModal('order'); }}>
                  Buy Now
                </button>
                {isAdmin() && <>
                  <button className="btn btn-outline btn-sm" onClick={() => openEdit(p)}>Edit</button>
                  <button className="btn btn-danger btn-sm" onClick={() => deleteProduct(p.id)}>Del</button>
                </>}
              </div>
            </div>
          ))}
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

      {/* Order Modal */}
      {modal === 'order' && selected && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Place Order — {selected.name}</h3>
            <div className="form-group">
              <label>Quantity</label>
              <input type="number" min="1" max={selected.stockQuantity} value={orderForm.quantity}
                onChange={e => setOrderForm({ ...orderForm, quantity: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Payment Method</label>
              <select value={orderForm.paymentMethod} onChange={e => setOrderForm({ ...orderForm, paymentMethod: e.target.value })}>
                <option>WALLET</option><option>CARD</option><option>UPI</option><option>NET_BANKING</option>
              </select>
            </div>
            <div className="form-group">
              <label>Shipping Address</label>
              <input value={orderForm.shippingAddress} onChange={e => setOrderForm({ ...orderForm, shippingAddress: e.target.value })} placeholder="123 Main St, City" />
            </div>
            <div style={{ color: '#555', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
              Total: <strong>₹{(Number(selected.price) * Number(orderForm.quantity || 1)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong>
            </div>
            <div className="modal-actions">
              <button className="btn btn-outline btn-sm" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-success btn-sm" onClick={placeOrder}>Confirm Order</button>
            </div>
          </div>
        </div>
      )}

      {/* Create / Edit Product Modal */}
      {(modal === 'create' || modal === 'edit') && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>{modal === 'create' ? 'Add Product' : 'Edit Product'}</h3>
            {['name', 'description', 'category', 'imageUrl'].map(f => (
              <div className="form-group" key={f}>
                <label style={{ textTransform: 'capitalize' }}>{f}</label>
                <input value={productForm[f]} onChange={e => setProductForm({ ...productForm, [f]: e.target.value })} />
              </div>
            ))}
            <div className="form-group">
              <label>Price (₹)</label>
              <input type="number" value={productForm.price} onChange={e => setProductForm({ ...productForm, price: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Stock Quantity</label>
              <input type="number" value={productForm.stockQuantity} onChange={e => setProductForm({ ...productForm, stockQuantity: e.target.value })} />
            </div>
            <div className="modal-actions">
              <button className="btn btn-outline btn-sm" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary btn-sm" onClick={saveProduct}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
