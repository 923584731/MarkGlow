import React, { useState, useEffect } from 'react';
import { Drawer, Tabs, Button, Input, Select, Space, message, Radio, Card, InputNumber } from 'antd';
import { PlayCircleOutlined } from '@ant-design/icons';
import {
  aiGenerate, aiImprove, aiCheckGrammar, aiSummarize, aiTranslate,
  aiExplainCode, aiComplete, aiExpand, aiGenerateList, aiOptimizeTitles,
  aiGenerateTable, aiAnswerQuestion, aiAnalyze,
  switchAIProvider, getCurrentAIProvider
} from '../services/api';
import './AISidebar.css';

const { TextArea } = Input;

function AISidebar({ content, selectedText, onResult, onClose, isOpen, aiParams, onParamsChange, onStartStream, onToggleStream }) {
  const [activeTab, setActiveTab] = useState('writing');
  const [loading, setLoading] = useState(false);
  const [currentProvider, setCurrentProvider] = useState('ernie');
  const [input, setInput] = useState('');
  const [style, setStyle] = useState('专业、清晰、易读');
  const [targetLang, setTargetLang] = useState('英文');
  const [language, setLanguage] = useState('javascript');
  const [question, setQuestion] = useState('');

  useEffect(() => {
    if (isOpen) {
      loadCurrentProvider();
    }
  }, [isOpen]);

  const loadCurrentProvider = async () => {
    try {
      const response = await getCurrentAIProvider();
      if (response.data && response.data.provider) {
        setCurrentProvider(response.data.provider);
      }
    } catch (error) {
      console.error('获取AI服务提供商失败:', error);
    }
  };

  const streamableActions = new Set(['improve', 'summarize', 'translate', 'expand', 'complete']);

  const handleSwitchProvider = async (provider) => {
    try {
      await switchAIProvider(provider);
      setCurrentProvider(provider);
      message.success(`已切换到${provider === 'ernie' ? '文心一言' : '通义千问'}`);
    } catch (error) {
      console.error('切换AI服务失败:', error);
      message.error('切换失败，请重试');
    }
  };

  const handleAction = async (action, params = {}) => {
    setLoading(true);
    try {
      let result;
      switch (action) {
        case 'generate':
          if (!input.trim()) {
            message.warning('请输入文档标题');
            setLoading(false);
            return;
          }
          result = await aiGenerate(input, content);
          break;
        case 'improve':
          const improveContent = (selectedText && selectedText.trim().length > 0) ? selectedText : (content || input);
          if (!improveContent.trim()) {
            message.warning('请先输入内容或选中要润色的文本');
            setLoading(false);
            return;
          }
          if (aiParams?.useStream && streamableActions.has(action) && onStartStream) {
            onStartStream(action, improveContent, { style });
            setLoading(false);
            return;
          }
          result = await aiImprove(improveContent, style);
          break;
        case 'checkGrammar':
          const checkContent = content || input;
          if (!checkContent.trim()) {
            message.warning('请先输入内容或选中要检查的文本');
            setLoading(false);
            return;
          }
          result = await aiCheckGrammar(checkContent);
          break;
        case 'summarize':
          const summarizeContent = (selectedText && selectedText.trim().length > 0) ? selectedText : (content || input);
          if (!summarizeContent.trim()) {
            message.warning('请先输入内容或选中要摘要的文本');
            setLoading(false);
            return;
          }
          if (aiParams?.useStream && streamableActions.has(action) && onStartStream) {
            onStartStream(action, summarizeContent);
            setLoading(false);
            return;
          }
          result = await aiSummarize(summarizeContent);
          break;
        case 'translate':
          const translateContent = (selectedText && selectedText.trim().length > 0) ? selectedText : (content || input);
          if (!translateContent.trim()) {
            message.warning('请先输入内容或选中要翻译的文本');
            setLoading(false);
            return;
          }
          if (aiParams?.useStream && streamableActions.has(action) && onStartStream) {
            onStartStream(action, translateContent, { targetLang });
            setLoading(false);
            return;
          }
          result = await aiTranslate(translateContent, targetLang);
          break;
        case 'explainCode':
          const codeContent = content || input;
          if (!codeContent.trim()) {
            message.warning('请先输入代码或选中要解释的代码');
            setLoading(false);
            return;
          }
          result = await aiExplainCode(codeContent, language);
          break;
        case 'complete':
          const completeContent = (selectedText && selectedText.trim().length > 0) ? selectedText : (content || input);
          if (!completeContent.trim()) {
            message.warning('请先输入部分文本');
            setLoading(false);
            return;
          }
          if (aiParams?.useStream && streamableActions.has(action) && onStartStream) {
            onStartStream(action, completeContent);
            setLoading(false);
            return;
          }
          result = await aiComplete(completeContent);
          break;
        case 'expand':
          const expandContent = (selectedText && selectedText.trim().length > 0) ? selectedText : (content || input);
          if (!expandContent.trim()) {
            message.warning('请先选中要扩展的段落');
            setLoading(false);
            return;
          }
          if (aiParams?.useStream && streamableActions.has(action) && onStartStream) {
            onStartStream(action, expandContent);
            setLoading(false);
            return;
          }
          result = await aiExpand(expandContent);
          break;
        case 'generateList':
          if (!input.trim()) {
            message.warning('请输入主题');
            setLoading(false);
            return;
          }
          result = await aiGenerateList(input);
          break;
        case 'optimizeTitles':
          const optimizeContent = content || input;
          if (!optimizeContent.trim()) {
            message.warning('请先输入内容或选中要优化的文档');
            setLoading(false);
            return;
          }
          result = await aiOptimizeTitles(optimizeContent);
          break;
        case 'generateTable':
          if (!input.trim()) {
            message.warning('请输入表格描述');
            setLoading(false);
            return;
          }
          result = await aiGenerateTable(input);
          break;
        case 'qa':
          if (!content.trim()) {
            message.warning('请先输入文档内容');
            setLoading(false);
            return;
          }
          if (!question.trim()) {
            message.warning('请输入问题');
            setLoading(false);
            return;
          }
          result = await aiAnswerQuestion(content, question);
          break;
        case 'analyze':
          const analyzeContent = content || input;
          if (!analyzeContent.trim()) {
            message.warning('请先输入内容或选中要分析的文档');
            setLoading(false);
            return;
          }
          result = await aiAnalyze(analyzeContent);
          break;
        default:
          setLoading(false);
          return;
      }

      if (result && result.result) {
        onResult(result.result, action, false);
      } else if (result && result.message) {
        message.error('错误: ' + result.message);
      } else {
        message.error('操作失败，请检查API配置');
      }
    } catch (error) {
      console.error('AI操作失败:', error);
      message.error('操作失败: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Drawer
      title="AI 功能"
      placement="right"
      onClose={onClose}
      open={isOpen}
      width={500}
      extra={
        <Radio.Group
          value={currentProvider}
          onChange={(e) => handleSwitchProvider(e.target.value)}
          size="small"
        >
          <Radio.Button value="ernie">文心一言</Radio.Button>
          <Radio.Button value="qwen">通义千问</Radio.Button>
        </Radio.Group>
      }
    >
      <Space style={{ marginBottom: 12 }} wrap>
        <Select
          style={{ width: 200 }}
          placeholder="模型（可选）"
          value={aiParams?.model}
          onChange={(v) => onParamsChange && onParamsChange({ model: v })}
          options={[
            { label: '自动/默认', value: '' },
            { label: 'ERNIE-4.5', value: 'ernie-4.5' },
            { label: 'ERNIE-4.5-turbo-128k', value: 'ernie-4.5-turbo-128k' },
            { label: 'Qwen-72B', value: 'qwen-72b' },
            { label: 'Qwen-2-72B-Instruct', value: 'qwen-2-72b-instruct' },
          ]}
        />
        <span>温度</span>
        <InputNumber min={0} max={2} step={0.1} value={aiParams?.temperature ?? 0.7} onChange={(v) => onParamsChange && onParamsChange({ temperature: v })} />
        <span>MaxTokens</span>
        <InputNumber min={256} max={8192} step={256} value={aiParams?.maxTokens ?? 2048} onChange={(v) => onParamsChange && onParamsChange({ maxTokens: v })} />
        <Button
          icon={<PlayCircleOutlined />}
          type={aiParams?.useStream ? 'primary' : 'default'}
          onClick={() => onToggleStream && onToggleStream()}
        >
          {aiParams?.useStream ? '流式输出：开' : '流式输出：关'}
        </Button>
      </Space>

      <Tabs 
        activeKey={activeTab} 
        onChange={setActiveTab}
        items={[
          {
            key: 'writing',
            label: '写作',
            children: (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Card title="AI 写作" size="small">
              <p>根据标题生成完整的Markdown文档</p>
              <Input
                placeholder="输入文档标题"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                style={{ marginBottom: 8 }}
              />
              <Button type="primary" block onClick={() => handleAction('generate')} loading={loading}>
                生成文档
              </Button>
              <Button style={{ marginTop: 8 }} block onClick={() => onStartStream && onStartStream('generate')}>
                流式生成
              </Button>
            </Card>

            <Card title="扩展段落" size="small">
              <p>将选中的段落扩展为更详细的内容</p>
              <Button type="primary" block onClick={() => handleAction('expand')} loading={loading}>
                扩展段落
              </Button>
              <Button style={{ marginTop: 8 }} block onClick={() => onStartStream && onStartStream('expand')}>
                流式扩展
              </Button>
            </Card>

            <Card title="生成列表" size="small">
              <p>根据主题生成Markdown列表</p>
              <Input
                placeholder="输入主题"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                style={{ marginBottom: 8 }}
              />
              <Button type="primary" block onClick={() => handleAction('generateList')} loading={loading}>
                生成列表
              </Button>
            </Card>

            <Card title="生成表格" size="small">
              <p>根据描述生成Markdown表格</p>
              <TextArea
                placeholder="描述表格内容"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                rows={3}
                style={{ marginBottom: 8 }}
              />
              <Button type="primary" block onClick={() => handleAction('generateTable')} loading={loading}>
                生成表格
              </Button>
            </Card>

            <Card title="智能补全" size="small">
              <p>补全当前输入的文本</p>
              <Button type="primary" block onClick={() => handleAction('complete')} loading={loading}>
                智能补全
              </Button>
            </Card>
          </Space>
            )
          },
          {
            key: 'improve',
            label: '优化',
            children: (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Card title="语言润色" size="small">
              <p>优化文本表达，提升可读性</p>
              <Input
                placeholder="风格（如：专业、清晰、易读）"
                value={style}
                onChange={(e) => setStyle(e.target.value)}
                style={{ marginBottom: 8 }}
              />
              <Button type="primary" block onClick={() => handleAction('improve')} loading={loading}>
                润色文本
              </Button>
            </Card>

            <Card title="语法检查" size="small">
              <p>检查语法错误和格式问题</p>
              <Button type="primary" block onClick={() => handleAction('checkGrammar')} loading={loading}>
                检查语法
              </Button>
            </Card>

            <Card title="优化标题" size="small">
              <p>优化文档标题层级和文字</p>
              <Button type="primary" block onClick={() => handleAction('optimizeTitles')} loading={loading}>
                优化标题
              </Button>
            </Card>

            <Card title="翻译文档" size="small">
              <p>将文档翻译为其他语言</p>
              <Select
                value={targetLang}
                onChange={setTargetLang}
                style={{ width: '100%', marginBottom: 8 }}
              >
                <Select.Option value="英文">英文</Select.Option>
                <Select.Option value="日文">日文</Select.Option>
                <Select.Option value="韩文">韩文</Select.Option>
                <Select.Option value="法文">法文</Select.Option>
                <Select.Option value="德文">德文</Select.Option>
              </Select>
              <Button type="primary" block onClick={() => handleAction('translate')} loading={loading}>
                翻译文档
              </Button>
            </Card>
          </Space>
            )
          },
          {
            key: 'analysis',
            label: '分析',
            children: (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Card title="生成摘要" size="small">
              <p>生成文档摘要和关键信息</p>
              <Button type="primary" block onClick={() => handleAction('summarize')} loading={loading}>
                生成摘要
              </Button>
            </Card>

            <Card title="分析文档" size="small">
              <p>分析文档结构、主题和关键词</p>
              <Button type="primary" block onClick={() => handleAction('analyze')} loading={loading}>
                分析文档
              </Button>
            </Card>

            <Card title="文档问答" size="small">
              <p>对文档内容进行问答</p>
              <Input
                placeholder="输入问题"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                style={{ marginBottom: 8 }}
              />
              <Button type="primary" block onClick={() => handleAction('qa')} disabled={!question} loading={loading}>
                提问
              </Button>
            </Card>
          </Space>
            )
          },
          {
            key: 'code',
            label: '代码',
            children: (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Card title="解释代码" size="small">
              <p>详细解释代码的功能和逻辑</p>
              <Select
                value={language}
                onChange={setLanguage}
                style={{ width: '100%', marginBottom: 8 }}
              >
                <Select.Option value="javascript">JavaScript</Select.Option>
                <Select.Option value="java">Java</Select.Option>
                <Select.Option value="python">Python</Select.Option>
                <Select.Option value="cpp">C++</Select.Option>
                <Select.Option value="c">C</Select.Option>
                <Select.Option value="go">Go</Select.Option>
                <Select.Option value="rust">Rust</Select.Option>
              </Select>
              <Button type="primary" block onClick={() => handleAction('explainCode')} loading={loading}>
                解释代码
              </Button>
            </Card>
          </Space>
            )
          }
        ]}
      />
    </Drawer>
  );
}

export default AISidebar;

