import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Imports from './pages/Imports';
import Dashboard from './pages/Dashboard';
import Metrics from './pages/Metrics';
import api, { COMPANY_ID } from './api';

const navStyle = { display: 'flex', gap: 16, padding: '12px 24px', background: '#1e293b', alignItems: 'center' };
const linkStyle = ({ isActive }) => ({ color: isActive ? '#60a5fa' : '#94a3b8', textDecoration: 'none', fontWeight: 'bold' });

export default function App() {
  const handleReset = async () => {
    if (!confirm('Limpar TODOS os dados de importação e transações?')) return;
    await api.delete('/api/imports/reset', { params: { companyId: COMPANY_ID } });
    alert('Dados limpos!');
    window.location.reload();
  };

  return (
    <BrowserRouter>
      <nav style={navStyle}>
        <NavLink to="/" style={linkStyle}>Importações</NavLink>
        <NavLink to="/dashboard" style={linkStyle}>Dashboard</NavLink>
        <NavLink to="/metrics" style={linkStyle}>Métricas</NavLink>
        <button onClick={handleReset} style={{
          marginLeft: 'auto', padding: '6px 14px', background: '#dc2626', color: 'white',
          border: 'none', borderRadius: 6, fontSize: 13, cursor: 'pointer',
        }}>🗑 Limpar dados</button>
      </nav>
      <div style={{ padding: '24px', fontFamily: 'system-ui, sans-serif' }}>
        <Routes>
          <Route path="/" element={<Imports />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/metrics" element={<Metrics />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}
