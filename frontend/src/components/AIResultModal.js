import React from 'react';
import { Modal, Button, Space, Typography } from 'antd';
import { 
  ReloadOutlined, 
  PlusOutlined, 
  CopyOutlined, 
  EditOutlined, 
  CheckOutlined 
} from '@ant-design/icons';
import './AIResultModal.css';

const { Text } = Typography;

/**
 * AI结果展示弹窗
 * @param {Object} props
 * @param {string} props.action - 功能类型：beautify, translate, summarize, improve, checkGrammar, explainCode, expand, generate
 * @param {string} props.result - AI返回的结果
 * @param {string} props.originalContent - 原始内容
 * @param {string} props.selectedText - 选中的文本（如果有）
 * @param {Function} props.onReplace - 替换整个文档的回调
 * @param {Function} props.onAppend - 追加到末尾的回调
 * @param {Function} props.onReplaceSelected - 替换选中部分的回调
 * @param {Function} props.onCopy - 复制结果的回调
 * @param {Function} props.onClose - 关闭弹窗的回调
 */
function AIResultModal({
  action,
  result,
  originalContent,
  selectedText,
  onReplace,
  onAppend,
  onReplaceSelected,
  onCopy,
  onClose
}) {
  if (!result) return null;

  // 根据功能类型决定显示哪些操作按钮
  const getAvailableActions = () => {
    const actions = [];
    
    switch (action) {
      case 'beautify':
        // 美化：只显示替换
        actions.push({ key: 'replace', label: '替换文档', icon: <ReloadOutlined /> });
        break;
      
      case 'translate':
        // 翻译：替换、追加、仅查看
        actions.push({ key: 'replace', label: '替换原文', icon: <ReloadOutlined /> });
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        actions.push({ key: 'copy', label: '复制结果', icon: <CopyOutlined /> });
        break;
      
      case 'summarize':
        // 摘要：追加、复制
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        actions.push({ key: 'copy', label: '复制摘要', icon: <CopyOutlined /> });
        break;
      
      case 'improve':
        // 润色：如果有选中文本，显示替换选中；否则显示替换和追加
        if (selectedText) {
          actions.push({ key: 'replaceSelected', label: '替换选中文本', icon: <EditOutlined /> });
        }
        actions.push({ key: 'replace', label: '替换整个文档', icon: <ReloadOutlined /> });
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        break;
      
      case 'checkGrammar':
        // 语法检查：替换、复制
        actions.push({ key: 'replace', label: '应用修复', icon: <CheckOutlined /> });
        actions.push({ key: 'copy', label: '复制结果', icon: <CopyOutlined /> });
        break;
      
      case 'explainCode':
        // 代码解释：追加、复制
        actions.push({ key: 'append', label: '追加到代码下方', icon: <PlusOutlined /> });
        actions.push({ key: 'copy', label: '复制解释', icon: <CopyOutlined /> });
        break;
      
      case 'expand':
        // 扩展段落：替换选中或追加
        if (selectedText) {
          actions.push({ key: 'replaceSelected', label: '替换选中文本', icon: <EditOutlined /> });
        }
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        break;
      
      case 'generate':
      case 'generateList':
      case 'generateTable':
        // 生成内容：追加、复制
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        actions.push({ key: 'copy', label: '复制结果', icon: <CopyOutlined /> });
        break;
      
      default:
        // 默认：替换、追加、复制
        actions.push({ key: 'replace', label: '替换文档', icon: <ReloadOutlined /> });
        actions.push({ key: 'append', label: '追加到末尾', icon: <PlusOutlined /> });
        actions.push({ key: 'copy', label: '复制结果', icon: <CopyOutlined /> });
    }
    
    return actions;
  };

  const handleAction = (actionKey) => {
    switch (actionKey) {
      case 'replace':
        onReplace(result);
        onClose();
        break;
      case 'append':
        onAppend(result, action);
        onClose();
        break;
      case 'replaceSelected':
        onReplaceSelected(result);
        onClose();
        break;
      case 'copy':
        onCopy(result);
        break;
      default:
        onClose();
    }
  };

  const getTitle = () => {
    const titles = {
      beautify: 'AI美化结果',
      translate: '翻译结果',
      summarize: '文档摘要',
      improve: '润色结果',
      checkGrammar: '语法检查结果',
      explainCode: '代码解释',
      expand: '扩展内容',
      generate: '生成内容',
      generateList: '生成的列表',
      generateTable: '生成的表格'
    };
    return titles[action] || 'AI处理结果';
  };

  const availableActions = getAvailableActions();

  return (
    <Modal
      title={getTitle()}
      open={!!result}
      onCancel={onClose}
      width={800}
      footer={[
        <Space key="actions">
          {availableActions.map(actionItem => (
            <Button
              key={actionItem.key}
              type={actionItem.key === 'replace' ? 'primary' : 'default'}
              icon={actionItem.icon}
              onClick={() => handleAction(actionItem.key)}
            >
              {actionItem.label}
            </Button>
          ))}
        </Space>
      ]}
    >
      <div style={{ maxHeight: '500px', overflow: 'auto' }}>
        <Text code style={{ whiteSpace: 'pre-wrap', display: 'block', padding: '12px', background: '#f5f5f5', borderRadius: '4px' }}>
          {result}
        </Text>
      </div>
    </Modal>
  );
}

export default AIResultModal;

