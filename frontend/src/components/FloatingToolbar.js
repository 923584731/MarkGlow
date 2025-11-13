import React, { useState, useEffect } from 'react';
import { Button, Space, Popover, message } from 'antd';
import { 
  StarOutlined, 
  FileTextOutlined, 
  GlobalOutlined, 
  BulbOutlined,
  MoreOutlined
} from '@ant-design/icons';
import { aiImprove, aiExpand, aiTranslate, aiExplainCode } from '../services/api';
import './FloatingToolbar.css';

function FloatingToolbar({ selectedText, onResult, position }) {
  const [loading, setLoading] = useState(false);
  const [showMenu, setShowMenu] = useState(false);

  if (!selectedText || selectedText.trim().length === 0 || !position) {
    return null;
  }

  const handleQuickAction = async (action) => {
    setLoading(true);
    setShowMenu(false);
    
    try {
      let result;
      switch (action) {
        case 'improve':
          const improveResult = await aiImprove(selectedText, '专业、清晰、易读');
          result = improveResult.result;
          break;
        case 'expand':
          const expandResult = await aiExpand(selectedText);
          result = expandResult.result;
          break;
        case 'translate':
          const translateResult = await aiTranslate(selectedText, '英文');
          result = translateResult.result;
          break;
        case 'explain':
          const explainResult = await aiExplainCode(selectedText, 'auto');
          result = explainResult.result;
          break;
        default:
          return;
      }

      if (result) {
        onResult(result, action, true); // 传递action类型和true表示替换选中文本
      }
    } catch (error) {
      console.error('快速操作失败:', error);
      message.error('操作失败: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const style = {
    position: 'absolute',
    top: `${position.top}px`,
    left: `${position.left}px`,
    transform: 'translateX(-50%)',
    zIndex: 1000,
    background: '#fff',
    padding: '4px',
    borderRadius: '4px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
    display: 'flex',
    gap: '4px'
  };

  const moreMenu = (
    <Space direction="vertical" size="small">
      <Button 
        size="small"
        icon={<BulbOutlined />}
        onClick={() => handleQuickAction('explain')}
        loading={loading}
      >
        解释
      </Button>
    </Space>
  );

  return (
    <div style={style}>
      <Space size="small">
        <Button
          size="small"
          icon={<StarOutlined />}
          onClick={() => handleQuickAction('improve')}
          loading={loading}
          title="润色"
        >
          润色
        </Button>
        <Button
          size="small"
          icon={<FileTextOutlined />}
          onClick={() => handleQuickAction('expand')}
          loading={loading}
          title="扩展"
        >
          扩展
        </Button>
        <Button
          size="small"
          icon={<GlobalOutlined />}
          onClick={() => handleQuickAction('translate')}
          loading={loading}
          title="翻译"
        >
          翻译
        </Button>
        <Popover content={moreMenu} trigger="click" placement="bottom">
          <Button
            size="small"
            icon={<MoreOutlined />}
            loading={loading}
            title="更多"
          />
        </Popover>
      </Space>
    </div>
  );
}

export default FloatingToolbar;

