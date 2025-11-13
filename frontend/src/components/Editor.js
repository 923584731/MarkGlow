import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Button, Select, Space, Input, message, Modal, Drawer } from 'antd';
import { 
  ImportOutlined, 
  SaveOutlined, 
  ExportOutlined, 
  BarChartOutlined
} from '@ant-design/icons';
import Split from 'react-split';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { beautifyMarkdown, API_BASE_URL } from '../services/api';
import AISidebar from './AISidebar';
import AIDropdown from './AIDropdown';
import FloatingToolbar from './FloatingToolbar';
import AIResultModal from './AIResultModal';
import AIStreamModal from './AIStreamModal';
import DocumentAnalysisPanel from './DocumentAnalysisPanel';
import './Editor.css';
import { themes } from '../themes/themes';

function Editor({ onSave, document }) {
  const [content, setContent] = useState(`# 欢迎使用 MarkGlow

这是一个 **Markdown 美化平台**，您可以：

- 编辑 Markdown 内容
- 实时预览渲染效果
- 选择不同的主题
- 使用 AI 美化排版
- 保存和导出文档

## 功能特点

1. **实时预览** - 所见即所得
2. **主题切换** - 多种精美主题
3. **AI 美化** - 智能优化排版
4. **文档管理** - 保存和管理您的文档

## 代码示例

\`\`\`javascript
function hello() {
  console.log('Hello, MarkGlow!');
}
\`\`\`

> 开始编辑您的 Markdown 文档吧！
`);
  const [beautifiedContent, setBeautifiedContent] = useState('');
  const [currentTheme, setCurrentTheme] = useState('default');
  const [isBeautifying, setIsBeautifying] = useState(false);
  const [showAnalysisDrawer, setShowAnalysisDrawer] = useState(false);
  const [selectedText, setSelectedText] = useState('');
  const [selectionPosition, setSelectionPosition] = useState(null);
  const [aiResult, setAiResult] = useState(null);
  const [aiAction, setAiAction] = useState(null);
  const [aiModalTrigger, setAiModalTrigger] = useState(null);
  const [aiParams, setAiParams] = useState({ model: '', temperature: 0.7, maxTokens: 2048, provider: '', useStream: false });
  const [streaming, setStreaming] = useState(false);
  const [streamText, setStreamText] = useState('');
  const [streamAction, setStreamAction] = useState('');
  const streamRef = useRef(null);
  const streamResultRef = useRef('');
  const [contentHistory, setContentHistory] = useState([]);
  const fileInputRef = useRef(null);
  const editorRef = useRef(null);

  // 获取当前 provider（用于元数据）
  useEffect(() => {
    fetch(`${API_BASE_URL}/ai/provider`)
      .then(res => res.json())
      .then(data => {
        const provider = data?.data?.provider || '';
        setAiParams(prev => ({ ...prev, provider }));
      })
      .catch(() => {});
  }, []);

  // 当传入的 document 变化时，将内容加载到编辑器
  useEffect(() => {
    if (document) {
      const docContent = document.beautifiedContent || document.originalContent || '';
      setContent(docContent);
      setBeautifiedContent(docContent);
      if (document.theme) {
        setCurrentTheme(document.theme);
      }
    }
  }, [document]);

  const startStream = (action, payload, extraParams = {}) => {
    const sourceText = typeof payload === 'string' && payload.trim().length > 0
      ? payload
      : (selectedText && selectedText.trim().length > 0 ? selectedText : content);

    if (!sourceText || !sourceText.trim()) {
      message.warning('请先输入一些内容');
      return;
    }
    if (streaming) return;
    setAiAction(action);
    setStreamText('');
    setStreamAction(action);
    setStreaming(true);
    streamResultRef.current = '';
    // 使用 POST 请求避免 URL 长度限制（EventSource 只支持 GET，改用 fetch + ReadableStream）
    const requestBody = {
      action: action,
      content: sourceText,
      stream: true
    };
    
    const temperature = typeof aiParams.temperature === 'number' ? aiParams.temperature : 0.7;
    const maxTokens = typeof aiParams.maxTokens === 'number' ? aiParams.maxTokens : 2048;
    if (temperature !== undefined && temperature !== null) {
      requestBody.temperature = temperature;
    }
    if (maxTokens !== undefined && maxTokens !== null) {
      requestBody.maxTokens = maxTokens;
    }
    if (aiParams.model) {
      requestBody.model = aiParams.model;
    }
    if (extraParams && typeof extraParams === 'object') {
      Object.entries(extraParams).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          requestBody[key] = value;
        }
      });
    }

    // 使用 fetch API 处理 POST 请求的 SSE 流
    fetch(`${API_BASE_URL}/ai/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(requestBody)
    }).then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      
      const readChunk = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            // 流结束
            setStreaming(false);
            streamRef.current = null;
            const finalResult = streamResultRef.current;
            if (finalResult) {
              if (action === 'beautify') {
                handleReplace(finalResult);
              } else {
                handleAIResult(finalResult, action, false);
              }
            }
            setStreamText('');
            setStreamAction('');
            streamResultRef.current = '';
            return;
          }
          
          // 解码数据
          buffer += decoder.decode(value, { stream: true });
          
          // 处理 SSE 格式的数据（按行分割）
          const lines = buffer.split('\n');
          buffer = lines.pop() || ''; // 保留最后一行（可能不完整）
          
          let currentEvent = 'chunk'; // 默认事件类型
          let currentData = '';
          
          for (const line of lines) {
            // 注意：不要对整个line使用trim()，这会去掉首尾空格
            // 只检查是否为空行（去除首尾空白后）
            const trimmedForCheck = line.trim();
            if (!trimmedForCheck) {
              // 空行表示一个事件结束，处理累积的数据
              if (currentData) {
                if (currentEvent === 'chunk') {
                  // 检查是否是 JSON 编码的字符串
                  let chunkData = currentData;
                  console.debug('[SSE处理] 处理chunk前 - currentData:', JSON.stringify(currentData));
                  console.debug('[SSE处理] currentData长度:', currentData.length);
                  console.debug('[SSE处理] currentData包含空格:', currentData.includes(' '));
                  console.debug('[SSE处理] currentData字符码:', Array.from(currentData).map(c => c.charCodeAt(0)).join(','));
                  if (chunkData.startsWith('"') && chunkData.endsWith('"')) {
                    try {
                      chunkData = JSON.parse(chunkData);
                      console.debug('[SSE处理] JSON解析后 - chunkData:', JSON.stringify(chunkData));
                      console.debug('[SSE处理] JSON解析后长度:', chunkData.length);
                      console.debug('[SSE处理] JSON解析后包含空格:', chunkData.includes(' '));
                      console.debug('[SSE处理] JSON解析后字符码:', Array.from(chunkData).map(c => c.charCodeAt(0)).join(','));
                    } catch (parseError) {
                      console.warn('JSON解析失败，使用原始数据:', parseError);
                    }
                  }
                  console.debug('[SSE处理] 最终chunkData:', JSON.stringify(chunkData));
                  console.debug('[SSE处理] 最终chunkData长度:', chunkData.length);
                  console.debug('[SSE处理] 最终chunkData包含空格:', chunkData.includes(' '));
                  streamResultRef.current += chunkData;
                  setStreamText(prev => {
                    const newText = prev + chunkData;
                    console.debug('[SSE处理] 更新streamText后长度:', newText.length);
                    console.debug('[SSE处理] streamText包含空格:', newText.includes(' '));
                    if (newText.includes('#')) {
                      console.debug('[SSE处理] streamText预览:', JSON.stringify(newText.substring(0, 50)));
                      // 检查 # 后面的字符
                      const hashIndex = newText.indexOf('#');
                      if (hashIndex >= 0 && hashIndex < newText.length - 1) {
                        const nextChar = newText.charAt(hashIndex + 1);
                        console.debug('[SSE处理] #后面的字符:', JSON.stringify(nextChar), '字符码:', nextChar.charCodeAt(0));
                      }
                    }
                    return newText;
                  });
                } else if (currentEvent === 'end') {
                  // 结束事件
                  setStreaming(false);
                  streamRef.current = null;
                  const finalResult = streamResultRef.current;
                  if (finalResult) {
                    if (action === 'beautify') {
                      handleReplace(finalResult);
                    } else {
                      handleAIResult(finalResult, action, false);
                    }
                  }
                  setStreamText('');
                  setStreamAction('');
                  streamResultRef.current = '';
                  return;
                } else if (currentEvent === 'error') {
                  // 错误事件
                  message.error('流式输出失败: ' + currentData);
                  setStreaming(false);
                  streamRef.current = null;
                  setStreamText('');
                  setStreamAction('');
                  streamResultRef.current = '';
                  return;
                }
                currentData = '';
              }
              continue;
            }
            
            // 处理 SSE 事件类型（只trim事件类型本身，不影响数据）
            if (line.trim().startsWith('event:')) {
              currentEvent = line.trim().substring(6).trim();
              continue;
            }
            
            // 处理 SSE 数据 - 关键：不要对整个line使用trim()
            if (line.trim().startsWith('data:')) {
              // 找到 "data:" 的位置
              const dataIndex = line.indexOf('data:');
              if (dataIndex >= 0) {
                // 从 "data:" 后面开始提取数据
                let data = line.substring(dataIndex + 5);
                // SSE规范：data: 后面的第一个空格是可选的，应该被忽略
                if (data.length > 0 && data[0] === ' ') {
                  data = data.substring(1);
                }
                // 注意：这里不再使用 trim()，以保留数据内容中的所有空格（包括首尾空格）
                
                // 调试：检查空格是否丢失
                if (data.includes('#') || data.includes(' ')) {
                  console.debug('[SSE接收] 原始line:', JSON.stringify(line));
                  console.debug('[SSE接收] 提取的data:', JSON.stringify(data));
                  console.debug('[SSE接收] data长度:', data.length);
                  console.debug('[SSE接收] data包含空格:', data.includes(' '));
                  console.debug('[SSE接收] data包含#号:', data.includes('#'));
                  console.debug('[SSE接收] data字符码:', Array.from(data).map(c => c.charCodeAt(0)).join(','));
                }
                currentData += (currentData ? '\n' : '') + data;
              }
            }
          }
          
          // 继续读取
          readChunk();
        }).catch(error => {
          console.error('读取流失败:', error);
          message.error('流式输出失败');
          setStreaming(false);
          streamRef.current = null;
          setStreamText('');
          setStreamAction('');
          streamResultRef.current = '';
        });
      };
      
      // 开始读取
      readChunk();
      
      // 保存 reader 以便后续可以取消
      streamRef.current = { reader, cancel: () => reader.cancel() };
    }).catch(error => {
      console.error('请求失败:', error);
      message.error('流式输出失败: ' + error.message);
      setStreaming(false);
      streamRef.current = null;
      setStreamText('');
      setStreamAction('');
      streamResultRef.current = '';
    });
    // 注意：现在使用 fetch API 处理 POST 请求，不再使用 EventSource
    // 事件处理逻辑已移到上面的 fetch 回调中
  };

  const stopStream = () => {
    if (streamRef.current) {
      try { streamRef.current.close(); } catch (e) {}
      streamRef.current = null;
    }
    setStreaming(false);
    streamResultRef.current = '';
    setStreamText('');
    setStreamAction('');
  };

  const handleImport = (event) => {
    const file = event.target.files[0];
    if (file && file.type === 'text/markdown' || file.name.endsWith('.md')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setContent(e.target.result);
        message.success('文件导入成功');
      };
      reader.readAsText(file);
    } else {
      message.error('请选择 .md 文件');
    }
  };

  const handleBeautify = async () => {
    if (!content.trim()) {
      message.warning('请先输入一些内容');
      return;
    }

    setIsBeautifying(true);
    try {
      const beautified = await beautifyMarkdown(content);
      setBeautifiedContent(beautified);
      setContent(beautified);
      message.success('美化完成！');
    } catch (error) {
      console.error('美化失败:', error);
      message.error('美化失败，请检查API配置');
    } finally {
      setIsBeautifying(false);
    }
  };

  // 保存内容历史（用于撤销）
  const saveToHistory = (currentContent) => {
    setContentHistory(prev => [...prev.slice(-9), currentContent]); // 保留最近10条
  };

  // 处理AI结果
  const handleAIResult = (result, action = 'beautify', replaceSelected = false) => {
    if (!result) return;

    // 保存当前内容到历史
    saveToHistory(content);

    // 根据功能类型决定处理方式
    if (action === 'beautify') {
      // 美化：直接替换（确保保留原始格式，包括换行符和空格）
      // result 应该已经包含了正确的格式，直接使用
      setContent(result);
      setBeautifiedContent(result);
      return;
    }

    if (replaceSelected && selectedText) {
      // 如果有选中文本且需要替换选中部分
      const textarea = editorRef.current?.querySelector('.markdown-editor');
      if (textarea) {
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const newContent = content.substring(0, start) + result + content.substring(end);
        setContent(newContent);
        setBeautifiedContent(newContent);
        setTimeout(() => {
          textarea.focus();
          textarea.setSelectionRange(start + result.length, start + result.length);
        }, 0);
      }
      setSelectedText('');
      setSelectionPosition(null);
      return;
    }

    // 其他功能：显示结果弹窗
    setAiResult(result);
    setAiAction(action);
  };

  // 替换整个文档
  const handleReplace = (result) => {
    const meta = aiAction ? `\n\n<!--\nAI 生成元数据:\n- Provider: ${aiParams.provider || '-'}\n- Model: ${aiParams.model || '-'}\n- Temperature: ${aiParams.temperature}\n- MaxTokens: ${aiParams.maxTokens}\n- Time: ${new Date().toISOString()}\n- Action: ${aiAction}\n-->` : '';
    const final = result + meta;
    setContent(final);
    setBeautifiedContent(final);
  };

  // 追加到末尾
  const handleAppend = (result, action) => {
    let appendContent = '\n\n';
    
    // 根据功能类型添加不同的分隔和标题
    switch (action) {
      case 'summarize':
        appendContent += '---\n\n## 📝 AI生成的摘要\n\n' + result;
        break;
      case 'explainCode':
        appendContent += '\n<!-- AI解释：\n' + result + '\n-->';
        break;
      case 'generate':
      case 'generateList':
      case 'generateTable':
        appendContent += '\n---\n\n' + result;
        break;
      default:
        appendContent += result;
    }
    const meta = action ? `\n\n<!--\nAI 生成元数据:\n- Provider: ${aiParams.provider || '-'}\n- Model: ${aiParams.model || '-'}\n- Temperature: ${aiParams.temperature}\n- MaxTokens: ${aiParams.maxTokens}\n- Time: ${new Date().toISOString()}\n- Action: ${action}\n-->` : '';
    const newContent = content + appendContent;
    setContent(newContent + meta);
    setBeautifiedContent(newContent + meta);
  };

  // 替换选中文本
  const handleReplaceSelected = (result) => {
    const textarea = editorRef.current?.querySelector('.markdown-editor');
    if (textarea && selectedText) {
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const newContent = content.substring(0, start) + result + content.substring(end);
      setContent(newContent);
      setBeautifiedContent(newContent);
      setTimeout(() => {
        textarea.focus();
        textarea.setSelectionRange(start + result.length, start + result.length);
      }, 0);
    }
    setSelectedText('');
    setSelectionPosition(null);
  };

  // 复制结果
  const handleCopy = (result) => {
    navigator.clipboard.writeText(result).then(() => {
      message.success('已复制到剪贴板');
    }).catch(err => {
      console.error('复制失败:', err);
      message.error('复制失败，请手动复制');
    });
  };

  // 关闭结果弹窗
  const handleCloseResultModal = () => {
    setAiResult(null);
    setAiAction(null);
  };

  // 处理文本选择（针对textarea）
  useEffect(() => {
    const textarea = editorRef.current?.querySelector('.markdown-editor');
    if (!textarea) return;

    const handleSelection = (e) => {
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const selected = content.substring(start, end);
      
      if (selected.trim().length > 0) {
        // 使用鼠标位置或简单计算
        let top = 50;
        let left = 200;
        
        if (e && e.type === 'mouseup') {
          // 如果有鼠标事件，使用鼠标位置
          top = e.clientY - textarea.getBoundingClientRect().top - 40;
          left = e.clientX - textarea.getBoundingClientRect().left;
        } else {
          // 否则使用简单的估算
          const textBeforeSelection = content.substring(0, start);
          const lines = textBeforeSelection.split('\n');
          const lineHeight = 20; // 估算行高
          top = lines.length * lineHeight + 20;
          left = 200;
        }
        
        setSelectedText(selected);
        setSelectionPosition({ top, left });
      } else {
        setSelectedText('');
        setSelectionPosition(null);
      }
    };

    textarea.addEventListener('mouseup', handleSelection);
    textarea.addEventListener('keyup', handleSelection);
    
    return () => {
      textarea.removeEventListener('mouseup', handleSelection);
      textarea.removeEventListener('keyup', handleSelection);
    };
  }, [content]);

  // 快捷键支持
  useEffect(() => {
    const handleKeyDown = (e) => {
      // 如果正在输入，不处理快捷键
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        // Ctrl+B 或 Cmd+B: AI美化（使用流式输出或普通模式）
        if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
          e.preventDefault();
          if (aiParams.useStream && startStream) {
            startStream('beautify', content);
          } else {
            handleBeautify();
          }
        }
        // Ctrl+I 或 Cmd+I: 语言润色
        if ((e.ctrlKey || e.metaKey) && e.key === 'i') {
          e.preventDefault();
          if (content.trim()) {
            handleQuickAction('improve');
          }
        }
        // Ctrl+Shift+S 或 Cmd+Shift+S: 生成摘要
        if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'S') {
          e.preventDefault();
          if (content.trim()) {
            handleQuickAction('summarize');
          }
        }
        // Ctrl+K 或 Cmd+K: 打开AI弹窗
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
          e.preventDefault();
          setAiModalTrigger({ action: 'improve', timestamp: Date.now() });
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [content]);

  const handleQuickAction = async (action) => {
    if (!content.trim()) {
      message.warning('请先输入一些内容');
      return;
    }

    setIsBeautifying(true);
    try {
      let result;
      switch (action) {
        case 'improve':
          const { aiImprove } = await import('../services/api');
          const improveResult = await aiImprove(content, '专业、清晰、易读');
          result = improveResult.result;
          break;
        case 'summarize':
          const { aiSummarize } = await import('../services/api');
          const summarizeResult = await aiSummarize(content);
          result = summarizeResult.result;
          break;
        default:
          return;
      }

      if (result) {
        setContent(result);
        setBeautifiedContent(result);
      }
    } catch (error) {
      console.error('快速操作失败:', error);
      message.error('操作失败: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsBeautifying(false);
    }
  };

  const handleSave = () => {
    let inputValue = document?.title || '未命名文档';
    Modal.confirm({
      title: '保存文档',
      content: (
        <Input
          placeholder="请输入文档标题"
          defaultValue={document?.title || '未命名文档'}
          autoFocus
          onChange={(e) => { inputValue = e.target.value || '未命名文档'; }}
          onPressEnter={() => {
            Modal.destroyAll();
            onSave({
              id: document?.id,
              title: inputValue,
              originalContent: content,
              beautifiedContent: beautifiedContent || content,
              theme: currentTheme,
            });
          }}
        />
      ),
      okText: '保存',
      cancelText: '取消',
      onOk: () => {
        onSave({
          id: document?.id,
          title: inputValue,
          originalContent: content,
          beautifiedContent: beautifiedContent || content,
          theme: currentTheme,
        });
      }
    });
  };

  const handleExport = () => {
    const exportContent = beautifiedContent || content;
    const blob = new Blob([exportContent], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = window.document.createElement('a');
    a.href = url;
    a.download = `markglow-${Date.now()}.md`;
    window.document.body.appendChild(a);
    a.click();
    window.document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const currentThemeStyles = themes[currentTheme] || themes.default;

  const handleOpenAiModal = useCallback((action) => {
    setAiModalTrigger({ action, timestamp: Date.now() });
  }, []);

  return (
    <div className="editor-container">
      <div className="editor-toolbar" style={{ padding: '12px', background: '#fff', borderBottom: '1px solid #f0f0f0' }}>
        <div className="toolbar-left" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleImport}
            accept=".md,text/markdown"
            style={{ display: 'none' }}
          />
          <Button icon={<ImportOutlined />} onClick={() => fileInputRef.current?.click()}>
            导入文件
          </Button>
          <AIDropdown
            onOpenAiModal={handleOpenAiModal}
          />
          {/* 流式输出按钮已移除，改由弹窗内控制 */}
          <Button icon={<SaveOutlined />} onClick={handleSave}>
            保存文档
          </Button>
          <Button icon={<ExportOutlined />} onClick={handleExport}>
            导出文件
          </Button>
          <Button 
            icon={<BarChartOutlined />} 
            onClick={() => setShowAnalysisDrawer(true)}
            title="文档分析"
          >
            文档分析
          </Button>
        </div>
        <div className="toolbar-right" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <span>主题：</span>
          <Select
            value={currentTheme}
            onChange={setCurrentTheme}
            style={{ width: 150 }}
          >
            {Object.keys(themes).map((theme) => (
              <Select.Option key={theme} value={theme}>
                {theme}
              </Select.Option>
            ))}
          </Select>
        </div>
      </div>

      <Split
        className="split-container"
        sizes={[50, 50]}
        minSize={[300, 300]}
        gutterSize={10}
        direction="horizontal"
        snapOffset={0}
      >
        <div className="editor-pane" ref={editorRef}>
          <div className="pane-header">编辑区</div>
          <div style={{ position: 'relative', flex: 1, minWidth: 0, width: '100%', maxWidth: '100%', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
            <textarea
              className="markdown-editor"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="在此输入 Markdown 内容..."
            />
            {selectedText && selectionPosition && (
              <FloatingToolbar
                selectedText={selectedText}
                onResult={handleAIResult}
                position={selectionPosition}
              />
            )}
          </div>
        </div>
        <div className="preview-pane">
          <div className="pane-header">预览区</div>
          <div
            className="markdown-preview"
            style={currentThemeStyles}
          >
            <ReactMarkdown 
              remarkPlugins={[remarkGfm]}
              components={{
                // 自定义链接组件，确保 URL 正确渲染
                a: ({node, ...props}) => (
                  <a {...props} target="_blank" rel="noopener noreferrer" />
                ),
                // 自定义列表项，改善显示
                li: ({node, ...props}) => (
                  <li {...props} style={{ marginBottom: '0.25em' }} />
                ),
                // 自定义段落，改善间距和 URL 识别
                p: ({node, ...props}) => {
                  // 检查是否是纯 URL 段落
                  const text = props.children?.toString() || '';
                  if (text.match(/^https?:\/\/.+/)) {
                    return (
                      <p {...props} style={{ marginBottom: '1em', wordBreak: 'break-all' }}>
                        <a href={text} target="_blank" rel="noopener noreferrer" style={{ color: '#0366d6' }}>
                          {text}
                        </a>
                      </p>
                    );
                  }
                  return <p {...props} style={{ marginBottom: '1em' }} />;
                }
              }}
            >
              {content}
            </ReactMarkdown>
          </div>
        </div>
      </Split>

      <AISidebar
        content={content}
        selectedText={selectedText}
        onResult={handleAIResult}
        aiParams={aiParams}
        onParamsChange={(params) => setAiParams(prev => ({ ...prev, ...params }))}
        onStartStream={startStream}
        externalTrigger={aiModalTrigger}
        onModalClose={() => setAiModalTrigger(null)}
      />

      {aiResult && (
        <AIResultModal
          action={aiAction}
          result={aiResult}
          originalContent={content}
          selectedText={selectedText}
          onReplace={handleReplace}
          onAppend={handleAppend}
          onReplaceSelected={handleReplaceSelected}
          onCopy={handleCopy}
          onClose={handleCloseResultModal}
        />
      )}

      <AIStreamModal
        open={streaming}
        action={streamAction}
        text={streamText}
        onStop={stopStream}
        onApplyReplace={(text) => { stopStream(); handleReplace(text); }}
        onApplyAppend={(text, action) => { stopStream(); handleAppend(text, action || streamAction); }}
        onClose={() => { stopStream(); }}
      />

      <Drawer
        title="文档分析"
        placement="right"
        width={600}
        onClose={() => setShowAnalysisDrawer(false)}
        open={showAnalysisDrawer}
      >
        <DocumentAnalysisPanel content={content} documentId={document?.id} />
      </Drawer>
    </div>
  );
}

export default Editor;

