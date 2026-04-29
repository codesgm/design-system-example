import { useState, useEffect, useRef } from 'react';
import api, { COMPANY_ID } from '../api';

const STATUS_LABELS = {
  PENDING: '⏳ Aguardando', PROCESSING: '⚙️ Processando', VALIDATING: '🔍 Validando',
  COMPLETED: '✅ Concluído', FAILED: '❌ Falhou',
};
const STATUS_COLORS = {
  PENDING: '#f59e0b', PROCESSING: '#3b82f6', VALIDATING: '#8b5cf6', COMPLETED: '#10b981', FAILED: '#ef4444',
};

export default function Imports() {
  const [file, setFile] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [imports, setImports] = useState([]);
  const [error, setError] = useState(null);
  const inputRef = useRef(null);

  const load = () => {
    api.get('/api/imports', { params: { companyId: COMPANY_ID, size: 20, sort: 'createdAt,desc' } })
      .then(({ data }) => setImports(data.content || []));
  };

  useEffect(() => { load(); const id = setInterval(load, 3000); return () => clearInterval(id); }, []);

  const handleSubmit = async () => {
    if (!file) return;
    if (file.size > 400 * 1024 * 1024) {
      setError('Arquivo excede o limite de 400MB');
      return;
    }
    setUploading(true); setError(null); setUploadProgress(0);
    const form = new FormData();
    form.append('file', file); form.append('companyId', COMPANY_ID);
    try {
      await api.post('/api/imports', form, {
        onUploadProgress: (e) => { if (e.total) setUploadProgress(Math.round((e.loaded / e.total) * 100)); },
      });
      setFile(null); setUploadProgress(0); load();
    } catch (err) {
      setError(err.response?.data?.detail || err.message);
    } finally { setUploading(false); }
  };

  const onDrop = (e) => { e.preventDefault(); setDragOver(false); const f = e.dataTransfer.files[0]; if (f?.name.endsWith('.csv')) setFile(f); };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h2>📁 Importações</h2>

      {/* Upload area */}
      <div
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDrop}
        style={{
          border: `2px dashed ${dragOver ? '#3b82f6' : '#d1d5db'}`, borderRadius: 12, padding: 32,
          textAlign: 'center', cursor: 'pointer', background: dragOver ? '#eff6ff' : '#fafafa', transition: 'all 0.2s',
        }}
      >
        <input ref={inputRef} type="file" accept=".csv" hidden onChange={(e) => setFile(e.target.files[0])} />
        <p style={{ fontSize: 28, margin: 0 }}>📄</p>
        <p style={{ color: '#6b7280', margin: '4px 0 0', fontSize: 14 }}>
          {file ? `${file.name} (${(file.size / 1024).toFixed(1)} KB)` : 'Arraste um CSV ou clique para selecionar'}
        </p>
      </div>

      {uploading && (
        <div style={{ marginTop: 8 }}>
          <div style={{ height: 6, background: '#e5e7eb', borderRadius: 3, overflow: 'hidden' }}>
            <div style={{ height: '100%', width: `${uploadProgress}%`, background: '#3b82f6', transition: 'width 0.3s' }} />
          </div>
        </div>
      )}

      <button onClick={handleSubmit} disabled={!file || uploading} style={{
        marginTop: 12, width: '100%', padding: '10px 0',
        background: !file || uploading ? '#d1d5db' : '#3b82f6', color: 'white',
        border: 'none', borderRadius: 8, fontSize: 14, fontWeight: 600, cursor: !file || uploading ? 'default' : 'pointer',
      }}>
        {uploading ? 'Enviando...' : 'Enviar CSV'}
      </button>

      {error && <div style={{ marginTop: 8, padding: 10, background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 8, color: '#dc2626', fontSize: 13 }}>{error}</div>}

      {/* Imports list */}
      <div style={{ marginTop: 32 }}>
        {imports.map((imp) => {
          const progress = imp.totalRows > 0 ? Math.round((imp.processedRows / imp.totalRows) * 100) : (imp.status === 'COMPLETED' ? 100 : 0);
          const isActive = ['PROCESSING', 'VALIDATING', 'PENDING'].includes(imp.status);
          return (
            <div key={imp.id} style={{ padding: 16, marginBottom: 12, background: '#f8fafc', borderRadius: 10, border: '1px solid #e2e8f0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{imp.fileName}</span>
                <span style={{ color: STATUS_COLORS[imp.status], fontWeight: 700, fontSize: 13 }}>{STATUS_LABELS[imp.status]}</span>
              </div>

              {isActive && (
                <div style={{ marginTop: 8 }}>
                  <div style={{ height: 6, background: '#e5e7eb', borderRadius: 3, overflow: 'hidden' }}>
                    <div style={{ height: '100%', width: `${progress}%`, background: 'linear-gradient(90deg, #3b82f6, #8b5cf6)', transition: 'width 0.5s' }} />
                  </div>
                  <div style={{ fontSize: 11, color: '#9ca3af', marginTop: 2 }}>{imp.processedRows} / {imp.totalRows || '?'} linhas</div>
                </div>
              )}

              <div style={{ display: 'flex', gap: 16, marginTop: 8, fontSize: 12, color: '#6b7280' }}>
                <span>Total: <strong>{imp.totalRows}</strong></span>
                <span>OK: <strong style={{ color: '#10b981' }}>{imp.processedRows}</strong></span>
                <span>Erros: <strong style={{ color: imp.errorRows > 0 ? '#ef4444' : '#6b7280' }}>{imp.errorRows}</strong></span>
                <span style={{ marginLeft: 'auto' }}>{new Date(imp.createdAt).toLocaleString('pt-BR')}</span>
              </div>
            </div>
          );
        })}
        {imports.length === 0 && <p style={{ color: '#9ca3af', textAlign: 'center' }}>Nenhuma importação ainda</p>}
      </div>
    </div>
  );
}
