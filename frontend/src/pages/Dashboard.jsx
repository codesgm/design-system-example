import { useEffect, useState, useCallback } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import api, { COMPANY_ID } from '../api';

const input = { padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 13 };
const btn = (active) => ({
  padding: '6px 14px', border: 'none', borderRadius: 6, fontSize: 13, cursor: 'pointer',
  background: active ? '#3b82f6' : '#e5e7eb', color: active ? 'white' : '#374151', fontWeight: 600,
});

export default function Dashboard() {
  const [balances, setBalances] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [accountId, setAccountId] = useState('');
  const [tipo, setTipo] = useState('');
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');

  useEffect(() => {
    api.get('/api/dashboards/balances', { params: { companyId: COMPANY_ID } }).then(({ data }) => {
      setBalances(data);
      setAccounts(data);
    });
  }, []);

  const loadTransactions = useCallback(() => {
    const params = { companyId: COMPANY_ID, page, size, sort: 'dataLancamento,desc' };
    if (accountId) params.financialAccountId = accountId;
    if (tipo) params.tipo = tipo;
    if (search) params.search = search;
    api.get('/api/transactions', { params }).then(({ data }) => setTransactions(data));
  }, [page, size, accountId, tipo, search]);

  useEffect(() => { loadTransactions(); }, [loadTransactions]);

  const chartData = balances.filter(b => b.balance !== 0).map(b => ({ name: b.name, saldo: Number(b.balance) }));

  const handleSearch = (e) => { e.preventDefault(); setSearch(searchInput); setPage(0); };

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      <h2>📊 Dashboard</h2>

      {/* Saldos chart */}
      {chartData.length > 0 && (
        <div style={{ marginBottom: 24 }}>
          <h3>Saldo por Conta</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis tickFormatter={v => `R$ ${(v/1000).toFixed(0)}k`} />
              <Tooltip formatter={v => `R$ ${v.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`} />
              <Bar dataKey="saldo" fill="#4f46e5" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Filters */}
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
        <select value={accountId} onChange={e => { setAccountId(e.target.value); setPage(0); }} style={input}>
          <option value="">Todas as contas</option>
          {accounts.map(a => <option key={a.accountId} value={a.accountId}>{a.name}</option>)}
        </select>

        <div style={{ display: 'flex', gap: 4 }}>
          <button style={btn(!tipo)} onClick={() => { setTipo(''); setPage(0); }}>Todos</button>
          <button style={btn(tipo === 'Receita')} onClick={() => { setTipo('Receita'); setPage(0); }}>Receita</button>
          <button style={btn(tipo === 'Despesa')} onClick={() => { setTipo('Despesa'); setPage(0); }}>Despesa</button>
        </div>

        <form onSubmit={handleSearch} style={{ display: 'flex', gap: 4, marginLeft: 'auto' }}>
          <input value={searchInput} onChange={e => setSearchInput(e.target.value)}
            placeholder="Buscar descrição..." style={{ ...input, width: 200 }} />
          <button type="submit" style={btn(!!search)}>🔍</button>
          {search && <button type="button" style={btn(false)} onClick={() => { setSearch(''); setSearchInput(''); setPage(0); }}>✕</button>}
        </form>
      </div>

      {/* Info */}
      <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 8 }}>
        {transactions.totalElements.toLocaleString('pt-BR')} transações encontradas
        {search && <span> — buscando "<strong>{search}</strong>"</span>}
      </div>

      {/* Table */}
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
            <th style={{ padding: 8 }}>NSU</th>
            <th style={{ padding: 8 }}>Descrição</th>
            <th style={{ padding: 8 }}>Conta</th>
            <th style={{ padding: 8 }}>Tipo</th>
            <th style={{ padding: 8, textAlign: 'right' }}>Valor</th>
            <th style={{ padding: 8 }}>Data Lanç.</th>
          </tr>
        </thead>
        <tbody>
          {transactions.content?.map((t, i) => (
            <tr key={i} style={{ borderBottom: '1px solid #f1f5f9' }}>
              <td style={{ padding: 8, fontFamily: 'monospace', fontSize: 12 }}>{t.nsu}</td>
              <td style={{ padding: 8 }}>{t.descricao}</td>
              <td style={{ padding: 8 }}>{t.contaBancaria}</td>
              <td style={{ padding: 8 }}>
                <span style={{
                  padding: '2px 8px', borderRadius: 4, fontSize: 11, fontWeight: 600,
                  background: t.tipo === 'Receita' ? '#ecfdf5' : '#fef2f2',
                  color: t.tipo === 'Receita' ? '#059669' : '#dc2626',
                }}>{t.tipo}</span>
              </td>
              <td style={{ padding: 8, textAlign: 'right', fontWeight: 600,
                color: t.tipo === 'Receita' ? '#059669' : '#dc2626' }}>
                R$ {Number(t.valorTotal).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </td>
              <td style={{ padding: 8, color: '#6b7280' }}>
                {t.dataLancamento ? new Date(t.dataLancamento + 'T00:00:00').toLocaleDateString('pt-BR') : '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {transactions.content?.length === 0 && (
        <p style={{ textAlign: 'center', color: '#9ca3af', padding: 24 }}>Nenhuma transação encontrada</p>
      )}

      {/* Pagination */}
      {transactions.totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 16, alignItems: 'center' }}>
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
            style={{ ...btn(false), opacity: page === 0 ? 0.5 : 1 }}>← Anterior</button>
          <span style={{ fontSize: 13, color: '#6b7280' }}>
            Página {page + 1} de {transactions.totalPages}
          </span>
          <button disabled={page >= transactions.totalPages - 1} onClick={() => setPage(p => p + 1)}
            style={{ ...btn(false), opacity: page >= transactions.totalPages - 1 ? 0.5 : 1 }}>Próxima →</button>
        </div>
      )}
    </div>
  );
}
