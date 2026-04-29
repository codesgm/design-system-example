import { useState, useEffect, useRef } from 'react';
import api, { COMPANY_ID } from '../api';

const STATUS_LABELS = {
  PENDING: '⏳ Aguardando processamento',
  PROCESSING: '⚙️ Processando linhas',
  VALIDATING: '🔍 Validando dados',
  COMPLETED: '✅ Concluído',
  FAILED: '❌ Falhou',
};

const STATUS_COLORS = {
  PENDING: '#f59e0b',
  PROCESSING: '#3b82f6',
  VALIDATING: '#8b5cf6',
  COMPLETED: '#10b981',
  FAILED: '#ef4444',
};

export default function Upload() {
  const [file, setFile] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [importJob, setImportJob] = useState(null);
  const [error, setError] = useState(null);
  const pollRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => { return () => { if (pollRef.current) clearInterval(pollRef.current); }; }, []);

  const pollStatus = (id) => {
    pollRef.current = setInterval(async () => {
      try {
        const { data } = await api.get(`/api/imports/${id}`);
        setImportJob(data);
        if (['COMPLETED', 'FAILED'].includes(data.status)) clearInterval(pollRef.current);
      } catch (_) {}
    }, 2000);
  };

  const handleSubmit = async () => {
    if (!file) return;
    setUploading(true);
    setError(null);
    setImportJob(null);
    setUploadProgress(0);

    const form = new FormData();
    form.append('file', file);
    form.append('companyId', COMPANY_ID);

    try {
      const { data } = await api.post('/api/imports', form, {
        onUploadProgress: (e) => {
          if (e.total) setUploadProgress(Math.round((e.loaded / e.total) * 100));
        },
      });
      setImportJob(data);
      setUploadProgress(100);
      pollStatus(data.id);
    } catch (err) {
      setError(err.response?.data?.detail || err.message);
    } finally {
      setUploading(false);
    }
  };

  const reset = () => {
    setFile(null);
    setImportJob(null);
    setError(null);
    setUploadProgress(0);
    if (pollRef.current) clearInterval(pollRef.current);
  };

  const onDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    const f = e.dataTransfer.files[0];
    if (f && f.name.endsWith('.csv')) setFile(f);
  };

  const processingProgress = importJob && importJob.totalRows > 0
    ? Math.round((importJob.processedRows / importJob.totalRows) * 100)
    : importJob?.status === 'COMPLETED' ? 100 : 0;

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 24 }}>📁 Importar Lançamentos</h2>

      {/* Drop zone */}
      {!importJob && (
        <>
          <div
            onClick={() => inputRef.current?.click()}
            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
            onDragLeave={() => setDragOver(false)}
            onDrop={onDrop}
            style={{
              border: `2px dashed ${dragOver ? '#3b82f6' : '#d1d5db'}`,
              borderRadius: 12,
              padding: 48,
              textAlign: 'center',
              cursor: 'pointer',
              background: dragOver ? '#eff6ff' : '#fafafa',
              transition: 'all 0.2s',
            }}
          >
            <input ref={inputRef} type="file" accept=".csv" hidden onChange={(e) => setFile(e.target.files[0])} />
            <p style={{ fontSize: 36, margin: 0 }}>📄</p>
            <p style={{ color: '#6b7280', margin: '8px 0 0' }}>
              {file ? file.name : 'Arraste um arquivo CSV ou clique para selecionar'}
            </p>
            {file && <p style={{ color: '#9ca3af', fontSize: 13 }}>{(file.size / 1024).toFixed(1)} KB</p>}
          </div>

          {/* Upload progress bar */}
          {uploading && (
            <div style={{ marginTop: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, color: '#6b7280', marginBottom: 4 }}>
                <span>Enviando arquivo...</span>
                <span>{uploadProgress}%</span>
              </div>
              <div style={{ height: 8, background: '#e5e7eb', borderRadius: 4, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${uploadProgress}%`, background: '#3b82f6', borderRadius: 4, transition: 'width 0.3s' }} />
              </div>
            </div>
          )}

          <button
            onClick={handleSubmit}
            disabled={!file || uploading}
            style={{
              marginTop: 16, width: '100%', padding: '12px 0',
              background: !file || uploading ? '#d1d5db' : '#3b82f6',
              color: 'white', border: 'none', borderRadius: 8,
              fontSize: 15, fontWeight: 600, cursor: !file || uploading ? 'default' : 'pointer',
            }}
          >
            {uploading ? 'Enviando...' : 'Enviar CSV'}
          </button>
        </>
      )}

      {/* Processing status */}
      {importJob && (
        <div style={{ marginTop: 16, padding: 20, background: '#f8fafc', borderRadius: 12, border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <span style={{ fontWeight: 600 }}>{importJob.fileName}</span>
            <span style={{ color: STATUS_COLORS[importJob.status], fontWeight: 700, fontSize: 14 }}>
              {STATUS_LABELS[importJob.status] || importJob.status}
            </span>
          </div>

          {/* Processing progress bar */}
          {['PROCESSING', 'VALIDATING', 'PENDING'].includes(importJob.status) && (
            <div style={{ marginBottom: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, color: '#6b7280', marginBottom: 4 }}>
                <span>Processando linhas...</span>
                <span>{importJob.processedRows} / {importJob.totalRows || '?'}</span>
              </div>
              <div style={{ height: 10, background: '#e5e7eb', borderRadius: 5, overflow: 'hidden' }}>
                <div style={{
                  height: '100%', width: `${processingProgress}%`,
                  background: `linear-gradient(90deg, #3b82f6, #8b5cf6)`,
                  borderRadius: 5, transition: 'width 0.5s',
                }} />
              </div>
            </div>
          )}

          {/* Stats */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12, marginTop: 8 }}>
            <div style={{ textAlign: 'center', padding: 12, background: 'white', borderRadius: 8 }}>
              <div style={{ fontSize: 22, fontWeight: 700 }}>{importJob.totalRows}</div>
              <div style={{ fontSize: 12, color: '#6b7280' }}>Total</div>
            </div>
            <div style={{ textAlign: 'center', padding: 12, background: 'white', borderRadius: 8 }}>
              <div style={{ fontSize: 22, fontWeight: 700, color: '#10b981' }}>{importJob.processedRows}</div>
              <div style={{ fontSize: 12, color: '#6b7280' }}>Processadas</div>
            </div>
            <div style={{ textAlign: 'center', padding: 12, background: 'white', borderRadius: 8 }}>
              <div style={{ fontSize: 22, fontWeight: 700, color: importJob.errorRows > 0 ? '#ef4444' : '#6b7280' }}>{importJob.errorRows}</div>
              <div style={{ fontSize: 12, color: '#6b7280' }}>Erros</div>
            </div>
          </div>

          {importJob.finishedAt && (
            <p style={{ fontSize: 12, color: '#9ca3af', marginTop: 12, textAlign: 'center' }}>
              Finalizado em {new Date(importJob.finishedAt).toLocaleString('pt-BR')}
            </p>
          )}

          <button onClick={reset} style={{
            marginTop: 16, width: '100%', padding: '10px 0',
            background: 'white', border: '1px solid #d1d5db', borderRadius: 8,
            fontSize: 14, cursor: 'pointer', color: '#374151',
          }}>
            Nova importação
          </button>
        </div>
      )}

      {error && (
        <div style={{ marginTop: 16, padding: 12, background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 8, color: '#dc2626' }}>
          {error}
        </div>
      )}
    </div>
  );
}
