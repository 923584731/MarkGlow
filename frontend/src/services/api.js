import axios from 'axios';

export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const appendModelParams = (body, options = {}) => {
  const result = { ...body };
  if (options.temperature !== undefined && options.temperature !== null) {
    result.temperature = options.temperature;
  }
  if (options.maxTokens !== undefined && options.maxTokens !== null) {
    result.maxTokens = options.maxTokens;
  }
  if (options.model) {
    result.model = options.model;
  }
  return result;
};

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
export const aiGenerate = async (title, context, options = {}) => {
  const response = await api.post('/ai/generate', appendModelParams({ title, context }, options));
  return response.data;
};

export const aiImprove = async (content, style, options = {}) => {
  const response = await api.post('/ai/improve', appendModelParams({ content, style }, options));
  return response.data;
};

export const aiCheckGrammar = async (content, options = {}) => {
  const response = await api.post('/ai/check-grammar', appendModelParams({ content }, options));
  return response.data;
};

export const aiSummarize = async (content, options = {}) => {
  const response = await api.post('/ai/summarize', appendModelParams({ content }, options));
  return response.data;
};

export const aiTranslate = async (content, targetLang, options = {}) => {
  const response = await api.post('/ai/translate', appendModelParams({ content, targetLang }, options));
  return response.data;
};

export const aiExplainCode = async (code, language, options = {}) => {
  const response = await api.post('/ai/explain-code', appendModelParams({ content: code, language }, options));
  return response.data;
};

export const aiComplete = async (partialText, options = {}) => {
  const response = await api.post('/ai/complete', appendModelParams({ content: partialText }, options));
  return response.data;
};

export const aiExpand = async (paragraph, options = {}) => {
  const response = await api.post('/ai/expand', appendModelParams({ content: paragraph }, options));
  return response.data;
};

export const aiGenerateList = async (topic, options = {}) => {
  const response = await api.post('/ai/generate-list', appendModelParams({ topic }, options));
  return response.data;
};

export const aiOptimizeTitles = async (content, options = {}) => {
  const response = await api.post('/ai/optimize-titles', appendModelParams({ content }, options));
  return response.data;
};

export const aiGenerateTable = async (description, options = {}) => {
  const response = await api.post('/ai/generate-table', appendModelParams({ description }, options));
  return response.data;
};

export const aiAnswerQuestion = async (document, question, options = {}) => {
  const response = await api.post('/ai/qa', appendModelParams({ content: document, question }, options));
  return response.data;
};

export const aiAnalyze = async (content, options = {}) => {
  const response = await api.post('/ai/analyze', appendModelParams({ content }, options));
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

// 提示词模板API
export const getPromptTemplates = async (category) => {
  const response = await api.get('/prompt-templates', {
    params: category ? { category } : {}
  });
  return response.data;
};

export const getPromptTemplateById = async (id) => {
  const response = await api.get(`/prompt-templates/${id}`);
  return response.data;
};

export const createPromptTemplate = async (template) => {
  const response = await api.post('/prompt-templates', template);
  return response.data;
};

export const updatePromptTemplate = async (id, template) => {
  const response = await api.put(`/prompt-templates/${id}`, template);
  return response.data;
};

export const deletePromptTemplate = async (id) => {
  const response = await api.delete(`/prompt-templates/${id}`);
  return response.data;
};

export const renderPromptTemplate = async (id, variables) => {
  const response = await api.post(`/prompt-templates/${id}/render`, variables || {});
  return response.data.rendered;
};

// 文档分析API
export const analyzeDocument = async (id) => {
  const response = await api.post(`/documents/${id}/analyze`);
  return response.data;
};

export const analyzeContent = async (content) => {
  const response = await api.post('/documents/analyze', { content });
  return response.data;
};

// 统计API
export const getStatisticsSummary = async (period = 'today') => {
  const response = await api.get('/statistics/summary', {
    params: { period }
  });
  return response.data;
};

export const getUsageStatistics = async (start, end, groupBy = 'day') => {
  const response = await api.get('/statistics/usage', {
    params: { start, end, groupBy }
  });
  return response.data;
};

export const getUsageRecords = async (start, end, page = 0, size = 20) => {
  const response = await api.get('/statistics/records', {
    params: { start, end, page, size }
  });
  return response.data;
};

export const getCostStatistics = async (start, end) => {
  const response = await api.get('/statistics/cost', {
    params: { start, end }
  });
  return response.data;
};


