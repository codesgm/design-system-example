import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import api from '../api';

const card = { padding: 16, background: '#f8fafc', borderRadius: 10, border: '1px solid #e2e8f0' };
const stat = { textAlign: 'center', padding: 12, background: 'white', borderRadius: 8 };
const label = { fontSize: 11, color: '#6b7280' };
const value = { fontSize: 20, fontWeight: 700 };

export default function Metrics() {
  const [data, setData] = useState(null);

  const load = () => api.get('/api/metrics').then(({ data }) => setData(data)).catch(() => {});
  useEffect(() => { load(); const id = setInterval(load, 1000); return () => clearInterval(id); }, []);

  if (!data) return <p>Carregando métricas...</p>;

  const { database, tables, imports } = data;
  const conn = database.connections;
  const tx = database.transactions;
  const wp = data.writePressure || {};

  const scoreColor = wp.score <= 20 ? '#10b981' : wp.score <= 50 ? '#f59e0b' : wp.score <= 75 ? '#f97316' : '#ef4444';
  const scoreBg = wp.score <= 20 ? '#ecfdf5' : wp.score <= 50 ? '#fffbeb' : wp.score <= 75 ? '#fff7ed' : '#fef2f2';

  const tableChart = (tables || []).map(t => ({
    name: t.table_name.replace('_', '\n'),
    linhas: Number(t.row_count),
  }));

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <h2>📊 Métricas do Sistema</h2>

      {/* Write Pressure */}
      <div style={{ ...card, marginBottom: 16, background: scoreBg, borderColor: scoreColor }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 12 }}>
          <div style={{ fontSize: 48, fontWeight: 800, color: scoreColor, lineHeight: 1 }}>{wp.score}</div>
          <div>
            <div style={{ fontSize: 18, fontWeight: 700 }}>Escrita DB</div>
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
          <div style={stat}>
            <div style={{ ...value, color: wp.activeWrites > 3 ? '#ef4444' : '#10b981' }}>{wp.activeWrites}</div>
            <div style={label}>Escritas ativas</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: wp.lockWaits > 0 ? '#ef4444' : '#10b981' }}>{wp.lockWaits}</div>
            <div style={label}>Esperando lock</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: wp.slowQueries > 0 ? '#f59e0b' : '#10b981' }}>{wp.slowQueries}</div>
            <div style={label}>Queries lentas (&gt;5s)</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: wp.connectionUsagePercent > 70 ? '#ef4444' : '#10b981' }}>{wp.connectionUsagePercent}%</div>
            <div style={label}>Uso conexões ({wp.currentConnections}/{wp.maxConnections})</div>
          </div>
        </div>
      </div>

      {/* DB Overview */}
      <div style={{ ...card, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 12px' }}>🗄️ Banco de Dados</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
          <div style={stat}>
            <div style={{ ...value, color: '#3b82f6' }}>{conn.active}</div>
            <div style={label}>Conexões ativas</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: '#6b7280' }}>{conn.idle}</div>
            <div style={label}>Conexões idle</div>
          </div>
          <div style={stat}>
            <div style={value}>{conn.total}</div>
            <div style={label}>Total conexões</div>
          </div>
          <div style={stat}>
            <div style={value}>{database.size}</div>
            <div style={label}>Tamanho DB</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: database.cacheHitRatio >= 99 ? '#10b981' : '#f59e0b' }}>
              {database.cacheHitRatio ?? '-'}%
            </div>
            <div style={label}>Cache hit ratio</div>
          </div>
        </div>
      </div>

      {/* Transactions stats */}
      <div style={{ ...card, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 12px' }}>⚡ Operações do Banco</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
          <div style={stat}>
            <div style={{ ...value, color: '#10b981' }}>{Number(tx.inserts).toLocaleString('pt-BR')}</div>
            <div style={label}>Inserts</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: '#3b82f6' }}>{Number(tx.updates).toLocaleString('pt-BR')}</div>
            <div style={label}>Updates</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: '#ef4444' }}>{Number(tx.deletes).toLocaleString('pt-BR')}</div>
            <div style={label}>Deletes</div>
          </div>
          <div style={stat}>
            <div style={value}>{Number(tx.commits).toLocaleString('pt-BR')}</div>
            <div style={label}>Commits</div>
          </div>
          <div style={stat}>
            <div style={{ ...value, color: tx.rollbacks > 0 ? '#ef4444' : '#6b7280' }}>{Number(tx.rollbacks).toLocaleString('pt-BR')}</div>
            <div style={label}>Rollbacks</div>
          </div>
        </div>
      </div>

      {/* Import stats */}
      <div style={{ ...card, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 12px' }}>📁 Importações</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
          <div style={stat}><div style={value}>{imports.total}</div><div style={label}>Total</div></div>
          <div style={stat}><div style={{ ...value, color: '#f59e0b' }}>{imports.pending}</div><div style={label}>Pendentes</div></div>
          <div style={stat}><div style={{ ...value, color: '#3b82f6' }}>{imports.processing}</div><div style={label}>Processando</div></div>
          <div style={stat}><div style={{ ...value, color: '#10b981' }}>{imports.completed}</div><div style={label}>Concluídas</div></div>
          <div style={stat}><div style={{ ...value, color: '#ef4444' }}>{imports.failed}</div><div style={label}>Falhas</div></div>
        </div>
      </div>

      {/* Table sizes chart */}
      <div style={{ ...card, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 12px' }}>📋 Linhas por Tabela</h3>
        {tableChart.length > 0 ? (
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={tableChart}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={v => v >= 1000000 ? `${(v/1000000).toFixed(1)}M` : v >= 1000 ? `${(v/1000).toFixed(0)}k` : v} />
              <Tooltip formatter={v => v.toLocaleString('pt-BR')} />
              <Bar dataKey="linhas" fill="#8b5cf6" />
            </BarChart>
          </ResponsiveContainer>
        ) : <p style={{ color: '#9ca3af' }}>Sem dados</p>}

        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 8, fontSize: 13 }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #e2e8f0', textAlign: 'left' }}>
              <th>Tabela</th><th>Linhas</th><th>Tamanho</th><th>Inserts</th><th>Updates</th><th>Deletes</th>
            </tr>
          </thead>
          <tbody>
            {(tables || []).map((t, i) => (
              <tr key={i} style={{ borderBottom: '1px solid #f1f5f9' }}>
                <td style={{ fontWeight: 600 }}>{t.table_name}</td>
                <td>{Number(t.row_count).toLocaleString('pt-BR')}</td>
                <td>{t.total_size}</td>
                <td>{Number(t.inserts).toLocaleString('pt-BR')}</td>
                <td>{Number(t.updates).toLocaleString('pt-BR')}</td>
                <td>{Number(t.deletes).toLocaleString('pt-BR')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
