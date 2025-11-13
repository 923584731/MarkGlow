import axios from 'axios';

export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getDocuments = async () => {
  const response = await api.get('/documents');
  return response.data;
};

export const searchDocuments = async (keyword, type = 'all') => {
  const response = await api.get('/documents/search', {
    params: { keyword, type }
  });
  return response.data;
};

export const getDocumentById = async (id) => {
  const response = await api.get(`/documents/${id}`);
  return response.data;
};

export const saveDocument = async (document) => {
  const response = await api.post('/documents', document);
  return response.data;
};

export const updateDocument = async (id, document) => {
  const response = await api.put(`/documents/${id}`, document);
  return response.data;
};

export const deleteDocument = async (id) => {
  const response = await api.delete(`/documents/${id}`);
  return response.data;
};

export const beautifyMarkdown = async (content) => {
  const response = await api.post('/beautify', { content });
  return response.data.beautifiedContent;
};

// AI功能API
export const aiGenerate = async (title, context) => {
  const response = await api.post('/ai/generate', { title, context });
  return response.data;
};

export const aiImprove = async (content, style) => {
  const response = await api.post('/ai/improve', { content, style });
  return response.data;
};

export const aiCheckGrammar = async (content) => {
  const response = await api.post('/ai/check-grammar', { content });
  return response.data;
};

export const aiSummarize = async (content) => {
  const response = await api.post('/ai/summarize', { content });
  return response.data;
};

export const aiTranslate = async (content, targetLang) => {
  const response = await api.post('/ai/translate', { content, targetLang });
  return response.data;
};

export const aiExplainCode = async (code, language) => {
  const response = await api.post('/ai/explain-code', { content: code, language });
  return response.data;
};

export const aiComplete = async (partialText) => {
  const response = await api.post('/ai/complete', { content: partialText });
  return response.data;
};

export const aiExpand = async (paragraph) => {
  const response = await api.post('/ai/expand', { content: paragraph });
  return response.data;
};

export const aiGenerateList = async (topic) => {
  const response = await api.post('/ai/generate-list', { topic });
  return response.data;
};

export const aiOptimizeTitles = async (content) => {
  const response = await api.post('/ai/optimize-titles', { content });
  return response.data;
};

export const aiGenerateTable = async (description) => {
  const response = await api.post('/ai/generate-table', { description });
  return response.data;
};

export const aiAnswerQuestion = async (document, question) => {
  const response = await api.post('/ai/qa', { content: document, question });
  return response.data;
};

export const aiAnalyze = async (content) => {
  const response = await api.post('/ai/analyze', { content });
  return response.data;
};

export const switchAIProvider = async (provider) => {
  const response = await api.post('/ai/switch-provider', { provider });
  return response.data;
};

export const getCurrentAIProvider = async () => {
  const response = await api.get('/ai/provider');
  return response.data;
};

