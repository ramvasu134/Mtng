import React, { useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { students as api } from '../api';

export default function CreateStudentPage() {
  const navigate = useNavigate();
  const { toast } = useOutletContext();
  const [form, setForm] = useState({ name: '', username: '', password: '', email: '', phone: '' });
  const [loading, setLoading] = useState(false);

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.create(form);
      toast.success(`Student "${form.name}" created successfully!`);
      navigate('/students');
    } catch (err) {
      toast.error(err.message || 'Failed to create student');
    } finally { setLoading(false); }
  };

  return (
    <div className="create-student-page">
      <div className="card" style={{ maxWidth: 560, margin: '0 auto' }}>
        <h2 className="card-title">➕ Create New Student</h2>
        <form onSubmit={handleSubmit} className="form-stack">
          <div className="form-group">
            <label>👤 Full Name *</label>
            <input className="form-input" value={form.name} onChange={set('name')} required placeholder="John Doe" />
          </div>
          <div className="form-group">
            <label>🔑 Username *</label>
            <input className="form-input" value={form.username} onChange={set('username')} required placeholder="johnd" />
          </div>
          <div className="form-group">
            <label>🔒 Password *</label>
            <input className="form-input" type="password" value={form.password} onChange={set('password')} required minLength={4} placeholder="Min 4 characters" />
          </div>
          <div className="form-group">
            <label>📧 Email</label>
            <input className="form-input" type="email" value={form.email} onChange={set('email')} placeholder="john@example.com" />
          </div>
          <div className="form-group">
            <label>📱 Phone</label>
            <input className="form-input" value={form.phone} onChange={set('phone')} placeholder="+1 234 567 8900" />
          </div>
          <div className="btn-row">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? '⏳ Creating…' : '✅ Create Student'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate('/students')}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}

