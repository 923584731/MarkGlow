import React from 'react';
import { Card, Button, Tag, Space } from 'antd';
import { ArrowLeftOutlined, DownloadOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { themes } from '../themes/themes';
import './DocumentView.css';

function DocumentView({ document, onBack }) {
  const handleExport = () => {
    const exportContent = document.beautifiedContent || document.originalContent;
    const blob = new Blob([exportContent], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = window.document.createElement('a');
    a.href = url;
    a.download = `${document.title || 'document'}-${Date.now()}.md`;
    window.document.body.appendChild(a);
    a.click();
    window.document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
  };

  const currentTheme = document.theme || 'default';
  const currentThemeStyles = themes[currentTheme] || themes.default;
  const content = document.beautifiedContent || document.originalContent || '';

  return (
    <Card>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={onBack}>
            返回列表
          </Button>
          <h1 style={{ margin: 0 }}>{document.title || '未命名文档'}</h1>
        </Space>
        <Space>
          <Tag color="blue">{currentTheme}</Tag>
          <Button type="primary" icon={<DownloadOutlined />} onClick={handleExport}>
            导出文件
          </Button>
        </Space>
      </div>

      <div style={{ marginBottom: 16, color: '#666', fontSize: '14px' }}>
        <Space>
          <span>创建时间: {formatDate(document.createdAt)}</span>
          <span>更新时间: {formatDate(document.updatedAt)}</span>
        </Space>
      </div>

      <div className="view-content">
        <div
          className="markdown-preview"
          style={currentThemeStyles}
        >
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {content}
          </ReactMarkdown>
        </div>
      </div>
    </Card>
  );
}

export default DocumentView;

