import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Login() {
  const { login } = useAuth();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/api/auth/login', form);
      const d = data.data;
      const token = d.accessToken || d.token;
      if (!token) throw new Error('No token received');
      login({ id: d.id, username: d.username, email: d.email, roles: d.roles },
            token, d.refreshToken);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <h2>🏦 BFSI Banking</h2>
        <p>Sign in to your account</p>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit} autoComplete="off">
          <div className="form-group">
            <label>Username</label>
            <input name="username" autoComplete="off" value={form.username} onChange={handle} required placeholder="admin / banker / user1" />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input name="password" autoComplete="new-password" type="password" value={form.password} onChange={handle} required placeholder="••••••••" />
          </div>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <div className="auth-switch">
          Don't have an account? <Link to="/register">Register</Link>
        </div>
      </div>
    </div>
  );
}
