import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Modal,
  Input,
  Select,
  Space,
  Typography,
  Switch,
  InputNumber,
  Divider,
  Button,
  message,
} from 'antd';
import {
  aiGenerate,
  aiImprove,
  aiCheckGrammar,
  aiSummarize,
  aiTranslate,
  aiExplainCode,
  aiComplete,
  aiExpand,
  aiGenerateList,
  aiOptimizeTitles,
  aiGenerateTable,
  aiAnswerQuestion,
  aiAnalyze,
  getPromptTemplates,
  renderPromptTemplate,
} from '../services/api';
import PromptTemplateManager from './PromptTemplateManager';
import './AISidebar.css';

const { TextArea } = Input;
const { Text } = Typography;

const DEFAULT_MODEL = 'ernie-4.5-turbo-128k';

const LANGUAGE_OPTIONS = [
  { label: 'JavaScript', value: 'javascript' },
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'python' },
  { label: 'C++', value: 'cpp' },
  { label: 'C', value: 'c' },
  { label: 'Go', value: 'go' },
  { label: 'Rust', value: 'rust' },
];

const TARGET_LANG_OPTIONS = [
  { label: '英文', value: '英文' },
  { label: '日文', value: '日文' },
  { label: '韩文', value: '韩文' },
  { label: '法文', value: '法文' },
  { label: '德文', value: '德文' },
];

const MODEL_OPTIONS = [
  { label: 'ERNIE-x1.1-preview', value: 'ernie-x1.1-preview' },
  { label: 'Kimi-K2-Instruct', value: 'kimi-k2-instruct' },
  { label: 'ERNIE-4.5-Turbo-128K', value: 'ernie-4.5-turbo-128k' },
];

const ACTION_CONFIG = {
  improve: {
    label: 'AI文档美化',
    description: '提升文本表达与可读性，适合润色内容。',
    streamable: true,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待润色内容',
        placeholder: '默认使用选中文本或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
      {
        key: 'style',
        type: 'input',
        label: '期望风格',
        placeholder: '如：专业、清晰、易读',
        defaultValue: '专业、清晰、易读',
      },
    ],
  },
  generate: {
    label: 'AI写作',
    description: '根据标题生成完整的 Markdown 文档。',
    streamable: true,
    template: true,
    fields: [
      {
        key: 'title',
        type: 'input',
        label: '文档标题',
        placeholder: '请输入文档标题',
        required: true,
      },
      {
        key: 'context',
        type: 'textarea',
        label: '参考内容（可选）',
        placeholder: '可提供参考材料提升生成效果，默认使用当前文档',
        autoFill: 'content',
        rows: 6,
      },
    ],
  },
  expand: {
    label: '扩展段落',
    description: '扩展选中的段落，使内容更充实。',
    streamable: true,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待扩展内容',
        placeholder: '默认使用选中文本或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
    ],
  },
  generateList: {
    label: '生成列表',
    description: '基于主题生成结构化列表。',
    streamable: false,
    fields: [
      {
        key: 'topic',
        type: 'input',
        label: '列表主题',
        placeholder: '请输入主题',
        required: true,
      },
    ],
  },
  generateTable: {
    label: '生成表格',
    description: '根据描述生成 Markdown 表格。',
    streamable: false,
    fields: [
      {
        key: 'tableDescription',
        type: 'textarea',
        label: '表格描述',
        placeholder: '请描述表格需要包含的内容',
        required: true,
        rows: 4,
      },
    ],
  },
  complete: {
    label: '智能补全',
    description: '根据上下文智能续写文本。',
    streamable: true,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待续写内容',
        placeholder: '默认使用选中文本或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
    ],
  },
  summarize: {
    label: '生成摘要',
    description: '提取关键信息生成摘要。',
    streamable: true,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '摘要来源内容',
        placeholder: '默认使用选中文本或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
    ],
  },
  analyze: {
    label: '文档分析',
    description: '分析文档结构、主题与关键词。',
    streamable: false,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '分析内容',
        placeholder: '默认使用当前文档',
        autoFill: 'content',
        required: true,
        rows: 6,
      },
    ],
  },
  translate: {
    label: '文档翻译',
    description: '将内容翻译为指定语言。',
    streamable: true,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待翻译内容',
        placeholder: '默认使用选中文本或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
      {
        key: 'targetLang',
        type: 'select',
        label: '目标语言',
        placeholder: '选择目标语言',
        options: TARGET_LANG_OPTIONS,
        defaultValue: '英文',
      },
    ],
  },
  checkGrammar: {
    label: '语法检查',
    description: '检测语法和格式问题。',
    streamable: false,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待检查内容',
        placeholder: '默认使用当前文档',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
    ],
  },
  optimizeTitles: {
    label: '标题优化',
    description: '优化文档标题层级与文字。',
    streamable: false,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '文档内容',
        placeholder: '默认使用当前文档',
        autoFill: 'content',
        required: true,
        rows: 6,
      },
    ],
  },
  qa: {
    label: '文档问答',
    description: '针对文档内容进行问答。',
    streamable: false,
    fields: [
      {
        key: 'question',
        type: 'input',
        label: '问题',
        placeholder: '请输入问题',
        required: true,
      },
      {
        key: 'text',
        type: 'textarea',
        label: '文档内容',
        placeholder: '默认使用当前文档，可按需调整范围',
        autoFill: 'content',
        required: true,
        rows: 6,
      },
    ],
  },
  explainCode: {
    label: '解释代码',
    description: '解释选中代码的逻辑与用途。',
    streamable: false,
    fields: [
      {
        key: 'text',
        type: 'textarea',
        label: '待解释代码',
        placeholder: '默认使用选中内容或全文',
        autoFill: 'selectedOrContent',
        required: true,
        rows: 6,
      },
      {
        key: 'language',
        type: 'select',
        label: '代码语言',
        placeholder: '选择代码语言',
        options: LANGUAGE_OPTIONS,
        defaultValue: 'javascript',
      },
    ],
  },
};

// 允许所有功能开启流式输出开关；是否真正走流式由后端能力决定
const STREAMABLE_ACTIONS = new Set(Object.keys(ACTION_CONFIG));

const BASE_FORM_VALUES = {
  text: '',
  style: '专业、清晰、易读',
  targetLang: '英文',
  language: 'javascript',
  question: '',
  title: '',
  context: '',
  topic: '',
  tableDescription: '',
  templateId: null,
};

/**
 * AI 模态框入口组件：集中承载所有 AI 功能（模型、参数、模板、输入项、流式输出）
 * - 代替旧的侧边栏交互
 * - 通过 externalTrigger 打开并设置当前功能
 * - 将 model/temperature/maxTokens/useStream 作为统一入口参数
 */
function AISidebar({
  content,
  selectedText,
  onResult,
  aiParams,
  onParamsChange,
  onStartStream,
  externalTrigger,
  onModalClose,
}) {
  const [visible, setVisible] = useState(false);
  const [currentAction, setCurrentAction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [formValues, setFormValues] = useState(BASE_FORM_VALUES);
  const [aiSettings, setAiSettings] = useState({
    model: aiParams?.model || DEFAULT_MODEL,
    temperature: aiParams?.temperature ?? 0.7,
    maxTokens: aiParams?.maxTokens ?? 2048,
    useStream: !!aiParams?.useStream,
  });
  const [templateOptions, setTemplateOptions] = useState([]);
  const [templatesLoading, setTemplatesLoading] = useState(false);
  const [templateManagerVisible, setTemplateManagerVisible] = useState(false);

  const normalizedSelectedText = useMemo(
    () => (selectedText && selectedText.length > 0 ? selectedText : ''),
    [selectedText],
  );
  const normalizedContent = useMemo(() => content || '', [content]);

  const getAutoFillValue = useCallback(
    (type) => {
      switch (type) {
        case 'selectedOrContent':
          return normalizedSelectedText || normalizedContent;
        case 'content':
          return normalizedContent;
        case 'selectedText':
          return normalizedSelectedText;
        default:
          return '';
      }
    },
    [normalizedContent, normalizedSelectedText],
  );

  const buildInitialValues = useCallback(
    (action) => {
      const config = ACTION_CONFIG[action];
      const initial = { ...BASE_FORM_VALUES };
      if (!config) {
        return initial;
      }
      config.fields?.forEach((field) => {
        let value = field.defaultValue ?? initial[field.key] ?? '';
        if (field.autoFill) {
          value = getAutoFillValue(field.autoFill);
        }
        initial[field.key] = value;
      });
      if (config.template) {
        initial.templateId = null;
      }
      return initial;
    },
    [getAutoFillValue],
  );

  useEffect(() => {
    setAiSettings((prev) => ({
      model: aiParams?.model ?? prev.model ?? DEFAULT_MODEL,
      temperature: aiParams?.temperature ?? prev.temperature ?? 0.7,
      maxTokens: aiParams?.maxTokens ?? prev.maxTokens ?? 2048,
      useStream: !!aiParams?.useStream,
    }));
  }, [aiParams?.model, aiParams?.temperature, aiParams?.maxTokens, aiParams?.useStream]);

  useEffect(() => {
    if (!externalTrigger || !externalTrigger.action) {
      return;
    }
    const { action, overrides = {} } = externalTrigger;
    setCurrentAction(action);
    setVisible(true);
    setFormValues(buildInitialValues(action));
    setAiSettings((prev) => ({
      model: prev.model ?? DEFAULT_MODEL,
      temperature: prev.temperature ?? 0.7,
      maxTokens: prev.maxTokens ?? 2048,
      useStream: STREAMABLE_ACTIONS.has(action)
        ? (typeof overrides.useStream === 'boolean' ? overrides.useStream : prev.useStream)
        : false,
    }));
  }, [externalTrigger, buildInitialValues]);

  useEffect(() => {
    if (!visible || currentAction !== 'generate') {
      return;
    }
    if (templateOptions.length > 0) {
      return;
    }
    const loadTemplates = async () => {
      try {
        setTemplatesLoading(true);
        const list = await getPromptTemplates();
        setTemplateOptions(
          (list || []).map((item) => ({
            label: item.name,
            value: item.id,
          })),
        );
      } catch (error) {
        console.error('加载模板列表失败:', error);
        message.error('加载模板列表失败');
      } finally {
        setTemplatesLoading(false);
      }
    };
    loadTemplates();
  }, [visible, currentAction, templateOptions.length]);

  const closeModal = useCallback(() => {
    setVisible(false);
    setCurrentAction(null);
    setFormValues(BASE_FORM_VALUES);
    if (onModalClose) {
      onModalClose();
    }
  }, [onModalClose]);

  const applySettingsChange = useCallback(
    (changes) => {
      setAiSettings((prev) => {
        const next = { ...prev, ...changes };
        if (onParamsChange) {
          onParamsChange(next);
        }
        return next;
      });
    },
    [onParamsChange],
  );

  const handleTemplateSelect = async (templateId) => {
    setFormValues((prev) => ({
      ...prev,
      templateId: templateId || null,
    }));
    if (!templateId) {
      return;
    }
    try {
      setTemplatesLoading(true);
      const rendered = await renderPromptTemplate(templateId, {});
      if (rendered) {
        setFormValues((prev) => ({
          ...prev,
          context: rendered,
        }));
        message.success('模板内容已加载到参考内容');
      }
    } catch (error) {
      console.error('渲染模板失败:', error);
      message.error('渲染模板失败');
    } finally {
      setTemplatesLoading(false);
    }
  };

  const handleFieldChange = (key, value) => {
    setFormValues((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const resolveTextValue = useCallback(
    (candidate) => {
      if (candidate && candidate.trim().length > 0) {
        return candidate;
      }
      if (normalizedSelectedText && normalizedSelectedText.trim().length > 0) {
        return normalizedSelectedText;
      }
      return normalizedContent;
    },
    [normalizedContent, normalizedSelectedText],
  );

  const executeAction = useCallback(
    async (actionKey) => {
      const config = ACTION_CONFIG[actionKey];
      if (!config) {
        message.warning('请选择要执行的功能');
        return;
      }

      for (const field of config.fields || []) {
        if (field.required) {
          const value = formValues[field.key];
          if (!value || (typeof value === 'string' && value.trim().length === 0)) {
            message.warning(`请输入${field.label}`);
            return;
          }
        }
      }

      const { model, temperature, maxTokens, useStream } = aiSettings;
      const requestOptions = {
        model: model || undefined,
        temperature: typeof temperature === 'number' ? temperature : undefined,
        maxTokens: typeof maxTokens === 'number' ? maxTokens : undefined,
      };

      const streamEnabled = useStream && STREAMABLE_ACTIONS.has(actionKey) && onStartStream;

      setLoading(true);

      try {
        let result = null;
        switch (actionKey) {
          case 'generate': {
            const title = (formValues.title || '').trim();
            const contextPayload = formValues.context && formValues.context.trim().length > 0
              ? formValues.context
              : normalizedContent;
            if (!title) {
              message.warning('请输入文档标题');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, contextPayload || normalizedContent, {
                title,
                context: contextPayload,
              });
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiGenerate(title, contextPayload, requestOptions);
            break;
          }
          case 'improve': {
            const textValue = resolveTextValue(formValues.text);
            const styleValue = (formValues.style || '专业、清晰、易读').trim() || '专业、清晰、易读';
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入待润色内容');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, textValue, { style: styleValue });
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiImprove(textValue, styleValue, requestOptions);
            break;
          }
          case 'expand': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入待扩展内容');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, textValue);
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiExpand(textValue, requestOptions);
            break;
          }
          case 'generateList': {
            const topic = (formValues.topic || '').trim();
            if (!topic) {
              message.warning('请输入列表主题');
              break;
            }
            result = await aiGenerateList(topic, requestOptions);
            break;
          }
          case 'generateTable': {
            const description = (formValues.tableDescription || '').trim();
            if (!description) {
              message.warning('请输入表格描述');
              break;
            }
            result = await aiGenerateTable(description, requestOptions);
            break;
          }
          case 'complete': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入待续写内容');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, textValue);
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiComplete(textValue, requestOptions);
            break;
          }
          case 'summarize': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入摘要来源内容');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, textValue);
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiSummarize(textValue, requestOptions);
            break;
          }
          case 'analyze': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入分析内容');
              break;
            }
            result = await aiAnalyze(textValue, requestOptions);
            break;
          }
          case 'translate': {
            const textValue = resolveTextValue(formValues.text);
            const targetLang = formValues.targetLang || '英文';
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入待翻译内容');
              break;
            }
            if (streamEnabled) {
              onStartStream(actionKey, textValue, { targetLang });
              closeModal();
              setLoading(false);
              return;
            }
            result = await aiTranslate(textValue, targetLang, requestOptions);
            break;
          }
          case 'checkGrammar': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入待检查内容');
              break;
            }
            result = await aiCheckGrammar(textValue, requestOptions);
            break;
          }
          case 'optimizeTitles': {
            const textValue = resolveTextValue(formValues.text);
            if (!textValue || textValue.trim().length === 0) {
              message.warning('请输入文档内容');
              break;
            }
            result = await aiOptimizeTitles(textValue, requestOptions);
            break;
          }
          case 'qa': {
            const question = (formValues.question || '').trim();
            const docContent = resolveTextValue(formValues.text);
            if (!question) {
              message.warning('请输入问题');
              break;
            }
            if (!docContent || docContent.trim().length === 0) {
              message.warning('请输入文档内容');
              break;
            }
            result = await aiAnswerQuestion(docContent, question, requestOptions);
            break;
          }
          case 'explainCode': {
            const codeContent = resolveTextValue(formValues.text);
            const language = formValues.language || 'javascript';
            if (!codeContent || codeContent.trim().length === 0) {
              message.warning('请输入代码内容');
              break;
            }
            result = await aiExplainCode(codeContent, language, requestOptions);
            break;
          }
          default:
            message.warning('暂不支持该功能，请选择其他功能');
        }

        if (result && result.result) {
          onResult(result.result, actionKey, false);
          closeModal();
        } else if (result && result.message) {
          message.error(`错误: ${result.message}`);
        } else if (result === null) {
          // 已处理（例如提示缺少参数）
        } else if (result !== undefined) {
          message.error('操作失败，请检查配置');
        }
      } catch (error) {
        console.error('AI 操作失败:', error);
        message.error(`操作失败: ${error.response?.data?.message || error.message}`);
      } finally {
        setLoading(false);
      }
    },
    [
      aiSettings,
      closeModal,
      formValues,
      normalizedContent,
      onResult,
      onStartStream,
      resolveTextValue,
    ],
  );

  const renderField = (field) => {
    const value = formValues[field.key];
    const label = (
      <Text strong>
        {field.label}
        {field.required && <Text type="danger"> *</Text>}
      </Text>
    );

    switch (field.type) {
      case 'input':
        return (
          <Space key={field.key} direction="vertical" style={{ width: '100%' }} size="small">
            {label}
            <Input
              placeholder={field.placeholder}
              value={value}
              onChange={(e) => handleFieldChange(field.key, e.target.value)}
              allowClear
            />
          </Space>
        );
      case 'textarea':
        return (
          <Space key={field.key} direction="vertical" style={{ width: '100%' }} size="small">
            {label}
            <TextArea
              placeholder={field.placeholder}
              value={value}
              onChange={(e) => handleFieldChange(field.key, e.target.value)}
              rows={field.rows || 4}
            />
          </Space>
        );
      case 'select':
        return (
          <Space key={field.key} direction="vertical" style={{ width: '100%' }} size="small">
            {label}
            <Select
              placeholder={field.placeholder}
              options={field.options || []}
              value={value || field.defaultValue}
              onChange={(val) => handleFieldChange(field.key, val)}
              style={{ width: '100%' }}
            />
          </Space>
        );
      default:
        return null;
    }
  };

  const currentConfig = currentAction ? ACTION_CONFIG[currentAction] : null;
  const isStreamable = currentAction ? STREAMABLE_ACTIONS.has(currentAction) : false;

  return (
    <>
      <Modal
        title={currentConfig ? `${currentConfig.label}` : 'AI 操作'}
        open={visible}
        onCancel={closeModal}
        onOk={() => currentAction && executeAction(currentAction)}
        okText="执行"
        cancelText="取消"
        confirmLoading={loading}
        width={720}
        destroyOnClose
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {currentConfig ? (
            <>
              <div>
                <Text type="secondary">{currentConfig.description}</Text>
              </div>

              {currentConfig.template && (
                <Space direction="vertical" style={{ width: '100%' }} size="small">
                  <Text strong>模板库（可选）</Text>
                  <Select
                    placeholder="选择模板"
                    allowClear
                    value={formValues.templateId}
                    onChange={handleTemplateSelect}
                    style={{ width: '100%' }}
                    options={templateOptions}
                    loading={templatesLoading}
                    showSearch
                    optionFilterProp="label"
                  />
                  <Button
                    type="link"
                    style={{ padding: 0 }}
                    onClick={() => setTemplateManagerVisible(true)}
                  >
                    管理模板
                  </Button>
                  <Divider style={{ margin: '12px 0' }} />
                </Space>
              )}

              <Space direction="vertical" size="large" style={{ width: '100%' }}>
                {(currentConfig.fields || []).map((field) => renderField(field))}
              </Space>
            </>
          ) : (
            <Text type="secondary">请选择要执行的功能。</Text>
          )}

          <Divider style={{ margin: '12px 0' }} />

          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Text strong>生成设置</Text>
            <Space wrap size="large">
              <Space direction="vertical" size="small">
                <Text type="secondary">模型选择</Text>
                <Select
                  value={aiSettings.model}
                  options={MODEL_OPTIONS}
                  style={{ width: 200 }}
                  onChange={(value) => applySettingsChange({ model: value })}
                />
              </Space>
              <Space direction="vertical" size="small">
                <Text type="secondary">温度</Text>
                <InputNumber
                  min={0}
                  max={2}
                  step={0.1}
                  value={aiSettings.temperature}
                  onChange={(value) =>
                    applySettingsChange({ temperature: typeof value === 'number' ? value : 0.7 })
                  }
                />
              </Space>
              <Space direction="vertical" size="small">
                <Text type="secondary">MaxTokens</Text>
                <InputNumber
                  min={256}
                  max={8192}
                  step={256}
                  value={aiSettings.maxTokens}
                  onChange={(value) =>
                    applySettingsChange({
                      maxTokens: typeof value === 'number' ? value : 2048,
                    })
                  }
                />
              </Space>
              <Space direction="vertical" size="small">
                <Text type="secondary">流式输出</Text>
                <Switch
                  checked={aiSettings.useStream}
                  onChange={(checked) =>
                    applySettingsChange({
                      useStream: checked,
                    })
                  }
                />
              </Space>
            </Space>
          </Space>
        </Space>
      </Modal>

      <PromptTemplateManager
        visible={templateManagerVisible}
        onClose={() => setTemplateManagerVisible(false)}
        onSelectTemplate={(renderedTemplate) => {
          setFormValues((prev) => ({
            ...prev,
            context: renderedTemplate,
          }));
          message.success('模板内容已加载到参考内容');
          setTemplateManagerVisible(false);
        }}
      />
    </>
  );
}

export default AISidebar;

