import React, { useState, useRef, useEffect } from 'react';
import { Dropdown, Button } from 'antd';
import {
  RobotOutlined,
  ThunderboltOutlined,
  FileTextOutlined,
  GlobalOutlined,
  BulbOutlined,
  ReadOutlined,
  EditOutlined,
  UnorderedListOutlined,
  TableOutlined,
  FormOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  QuestionCircleOutlined,
  CodeOutlined,
} from '@ant-design/icons';
import './AIDropdown.css';

const DROPDOWN_ACTIONS = [
  { key: 'improve', label: 'AI文档美化', icon: <ThunderboltOutlined /> },
  { key: 'generate', label: 'AI写作', icon: <BulbOutlined /> },
  { key: 'expand', label: '扩展段落', icon: <EditOutlined /> },
  { key: 'generateList', label: '生成列表', icon: <UnorderedListOutlined /> },
  { key: 'generateTable', label: '生成表格', icon: <TableOutlined /> },
  { key: 'complete', label: '智能补全', icon: <FormOutlined /> },
  { key: 'summarize', label: '生成摘要', icon: <FileTextOutlined /> },
  { key: 'analyze', label: '文档分析', icon: <BarChartOutlined /> },
  { key: 'translate', label: '文档翻译', icon: <GlobalOutlined /> },
  { key: 'checkGrammar', label: '语法检查', icon: <CheckCircleOutlined /> },
  { key: 'optimizeTitles', label: '标题优化', icon: <ReadOutlined /> },
  { key: 'qa', label: '文档问答', icon: <QuestionCircleOutlined /> },
  { key: 'explainCode', label: '解释代码', icon: <CodeOutlined /> },
];

/**
 * AI 下拉菜单：仅负责触发 AI 模态框，不直接执行任何 AI 行为
 */
function AIDropdown({ onOpenAiModal }) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const menuItems = [
    {
      type: 'group',
      label: 'AI 功能',
      children: DROPDOWN_ACTIONS.map((action) => ({
        key: action.key,
        icon: action.icon,
        label: action.label,
      })),
    },
  ];

  const handleMenuClick = ({ key }) => {
    setIsOpen(false);
    if (onOpenAiModal) {
      onOpenAiModal(key);
    }
  };

  return (
    <Dropdown 
      menu={{ items: menuItems, onClick: handleMenuClick }} 
      trigger={['click']} 
      open={isOpen} 
      onOpenChange={setIsOpen}
    >
      <Button
        icon={<RobotOutlined />}
        onClick={() => setIsOpen(!isOpen)}
      >
        AI ▼
      </Button>
    </Dropdown>
  );
}

export default AIDropdown;

