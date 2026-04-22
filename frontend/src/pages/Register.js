import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '', role: 'user' });
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setLoading(true);
    try {
      const { data } = await api.post('/api/auth/register', {
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        roles: [form.role]
      });
      setSuccess(data.message || data.data || 'Registered successfully!');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <h2>🏦 Create Account</h2>
        <p>Join BFSI Banking today</p>
        {error   && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}
        <form onSubmit={submit}>
          <div className="form-group">
            <label>Full Name</label>
            <input name="fullName" value={form.fullName} onChange={handle} required placeholder="John Doe" />
          </div>
          <div className="form-group">
            <label>Username</label>
            <input name="username" value={form.username} onChange={handle} required placeholder="johndoe" />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input name="email" type="email" value={form.email} onChange={handle} required placeholder="john@example.com" />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={handle} required placeholder="Min 6 characters" />
          </div>
          <div className="form-group">
            <label>Register As</label>
            <select name="role" value={form.role} onChange={handle}>
              <option value="user">User</option>
              <option value="banker">Banker</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>
        <div className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
