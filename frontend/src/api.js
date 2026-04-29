import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  auth: { username: 'admin', password: 'admin' },
});

export const COMPANY_ID = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890';
export default api;
