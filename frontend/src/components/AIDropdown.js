import React, { useState, useRef, useEffect } from 'react';
import { Dropdown, Button, message } from 'antd';
import { 
  RobotOutlined, 
  ThunderboltOutlined, 
  StarOutlined, 
  FileTextOutlined, 
  GlobalOutlined,
  AppstoreOutlined
} from '@ant-design/icons';
import { beautifyMarkdown, aiImprove, aiSummarize, aiTranslate } from '../services/api';
import './AIDropdown.css';

function AIDropdown({ content, selectedText, onResult, onOpenSidebar, onStartStream, useStream = false }) {
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
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

  const handleQuickAction = async (action) => {
    const fallbackContent = content || '';
    if (!fallbackContent.trim()) {
      message.warning('请先输入一些内容');
      setIsOpen(false);
      return;
    }

    const sourceText = selectedText && selectedText.trim().length > 0 ? selectedText : fallbackContent;

    // 如果启用流式输出，使用流式接口（包括美化功能）
    if (useStream && onStartStream) {
      setIsOpen(false);
      onStartStream(action, sourceText);
      return;
    }

    setLoading(true);
    setIsOpen(false);
    
    try {
      let result;
      switch (action) {
        case 'beautify':
          result = await beautifyMarkdown(fallbackContent);
          break;
        case 'improve': {
          if (!sourceText.trim()) {
            message.warning('请先输入或选择需要润色的内容');
            return;
          }
          const improveResult = await aiImprove(sourceText, '专业、清晰、易读');
          result = improveResult?.result || improveResult?.data || improveResult;
          break;
        }
        case 'summarize': {
          if (!sourceText.trim()) {
            message.warning('请先输入或选择需要摘要的内容');
            return;
          }
          const summarizeResult = await aiSummarize(sourceText);
          result = summarizeResult?.result || summarizeResult?.data || summarizeResult;
          break;
        }
        case 'translate': {
          if (!sourceText.trim()) {
            message.warning('请先输入或选择需要翻译的内容');
            return;
          }
          const translateResult = await aiTranslate(sourceText, '英文');
          result = translateResult?.result || translateResult?.data || translateResult;
          break;
        }
        default:
          return;
      }

      if (result) {
        onResult(result, action, false);
      }
    } catch (error) {
      console.error('快速操作失败:', error);
      message.error('操作失败: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const menuItems = [
    {
      type: 'group',
      label: '常用功能',
      children: [
        {
          key: 'beautify',
          icon: <ThunderboltOutlined />,
          label: 'AI美化',
        },
        {
          key: 'improve',
          icon: <StarOutlined />,
          label: '语言润色',
        },
        {
          key: 'summarize',
          icon: <FileTextOutlined />,
          label: '生成摘要',
        },
        {
          key: 'translate',
          icon: <GlobalOutlined />,
          label: '翻译文档',
        },
      ],
    },
    {
      type: 'divider',
    },
    {
      key: 'more',
      icon: <AppstoreOutlined />,
      label: '所有功能...',
    },
  ];

  const handleMenuClick = ({ key }) => {
    if (key === 'more') {
      setIsOpen(false);
      onOpenSidebar();
      return;
    }
    handleQuickAction(key);
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
        loading={loading}
        onClick={() => setIsOpen(!isOpen)}
      >
        AI {loading ? '处理中...' : '▼'}
      </Button>
    </Dropdown>
  );
}

export default AIDropdown;

