import React, { useState } from 'react';
import { Card, Button, Tag, Space, Drawer, Dropdown, message } from 'antd';
import { ArrowLeftOutlined, DownloadOutlined, BarChartOutlined, FileWordOutlined, FileTextOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { themes } from '../themes/themes';
import DocumentAnalysisPanel from './DocumentAnalysisPanel';
import './DocumentView.css';

function DocumentView({ document, onBack }) {
  const [showAnalysisDrawer, setShowAnalysisDrawer] = useState(false);
  
  // 导出 Markdown
  const handleExportMarkdown = () => {
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
    message.success('Markdown 文件导出成功');
  };

  // 导出 Word
  const handleExportWord = () => {
    try {
      const exportContent = document.beautifiedContent || document.originalContent;
      // 将 Markdown 转换为 HTML
      const htmlContent = markdownToHtml(exportContent);
      // 创建 Word 文档的 HTML 结构
      const wordHtml = `
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
          <head>
            <meta charset="utf-8">
            <title>${document.title || 'Document'}</title>
            <!--[if gte mso 9]>
            <xml>
              <w:WordDocument>
                <w:View>Print</w:View>
                <w:Zoom>90</w:Zoom>
                <w:DoNotOptimizeForBrowser/>
              </w:WordDocument>
            </xml>
            <![endif]-->
            <style>
              body {
                font-family: "Microsoft YaHei", "SimSun", Arial, sans-serif;
                font-size: 12pt;
                line-height: 1.6;
                margin: 20px;
              }
              h1, h2, h3, h4, h5, h6 {
                margin-top: 20px;
                margin-bottom: 10px;
              }
              p {
                margin: 10px 0;
              }
              code {
                background-color: #f5f5f5;
                padding: 2px 4px;
                border-radius: 3px;
                font-family: "Courier New", monospace;
              }
              pre {
                background-color: #f5f5f5;
                padding: 10px;
                border-radius: 5px;
                overflow-x: auto;
              }
              table {
                border-collapse: collapse;
                width: 100%;
                margin: 10px 0;
              }
              table th, table td {
                border: 1px solid #ddd;
                padding: 8px;
                text-align: left;
              }
              table th {
                background-color: #f2f2f2;
              }
            </style>
          </head>
          <body>
            <h1>${document.title || 'Document'}</h1>
            ${htmlContent}
          </body>
        </html>
      `;
      
      const blob = new Blob(['\ufeff', wordHtml], { type: 'application/msword' });
      const url = URL.createObjectURL(blob);
      const a = window.document.createElement('a');
      a.href = url;
      a.download = `${document.title || 'document'}-${Date.now()}.doc`;
      window.document.body.appendChild(a);
      a.click();
      window.document.body.removeChild(a);
      URL.revokeObjectURL(url);
      message.success('Word 文件导出成功');
    } catch (error) {
      console.error('导出 Word 失败:', error);
      message.error('导出 Word 文件失败');
    }
  };

  // 简单的 Markdown 转 HTML 函数
  const markdownToHtml = (markdown) => {
    // 使用 ReactMarkdown 的渲染逻辑，但这里我们需要一个简单的转换
    // 由于 ReactMarkdown 是 React 组件，我们使用一个简单的转换函数
    let html = markdown
      // 标题
      .replace(/^### (.*$)/gim, '<h3>$1</h3>')
      .replace(/^## (.*$)/gim, '<h2>$1</h2>')
      .replace(/^# (.*$)/gim, '<h1>$1</h1>')
      // 粗体
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      // 斜体
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      // 代码块
      .replace(/```[\s\S]*?```/g, (match) => {
        const code = match.replace(/```/g, '').trim();
        return `<pre><code>${code}</code></pre>`;
      })
      // 行内代码
      .replace(/`(.+?)`/g, '<code>$1</code>')
      // 链接
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>')
      // 图片
      .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')
      // 列表
      .replace(/^\* (.+)$/gim, '<li>$1</li>')
      .replace(/^- (.+)$/gim, '<li>$1</li>')
      .replace(/^(\d+)\. (.+)$/gim, '<li>$2</li>')
      // 段落
      .split('\n\n')
      .map(para => {
        if (!para.trim()) return '';
        if (para.startsWith('<')) return para; // 已经是 HTML 标签
        return `<p>${para}</p>`;
      })
      .join('\n');

    // 包装列表项
    html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');

    return html;
  };

  // 导出菜单项
  const exportMenuItems = [
    {
      key: 'markdown',
      label: '导出为 Markdown',
      icon: <FileTextOutlined />,
      onClick: handleExportMarkdown,
    },
    {
      key: 'word',
      label: '导出为 Word',
      icon: <FileWordOutlined />,
      onClick: handleExportWord,
    },
  ];

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
          <Button 
            icon={<BarChartOutlined />} 
            onClick={() => setShowAnalysisDrawer(true)}
          >
            文档分析
          </Button>
          <Dropdown 
            menu={{ items: exportMenuItems }}
            trigger={['click']}
          >
            <Button type="primary" icon={<DownloadOutlined />}>
              导出文件
            </Button>
          </Dropdown>
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

      <Drawer
        title="文档分析"
        placement="right"
        width={600}
        onClose={() => setShowAnalysisDrawer(false)}
        open={showAnalysisDrawer}
      >
        <DocumentAnalysisPanel content={content} documentId={document.id} />
      </Drawer>
    </Card>
  );
}

export default DocumentView;

