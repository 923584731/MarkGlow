import React, { useEffect, useRef } from 'react';
import { Modal, Button, Space } from 'antd';
import './AIStreamModal.css';

function AIStreamModal({ open, action, text, onStop, onApplyReplace, onApplyAppend, onClose }) {
  const contentRef = useRef(null);

  // 当文本更新时，自动滚动到底部
  useEffect(() => {
    if (contentRef.current && text) {
      contentRef.current.scrollTop = contentRef.current.scrollHeight;
    }
  }, [text]);

  return (
    <Modal
      title={`AI 流式生成：${action || ''}`}
      open={open}
      onCancel={onClose}
      footer={
        <Space>
          <Button onClick={onStop}>停止</Button>
          <Button onClick={() => onApplyAppend(text, action)} disabled={!text}>追加到文末</Button>
          <Button type="primary" onClick={() => onApplyReplace(text)} disabled={!text}>替换全文</Button>
        </Space>
      }
      width={800}
      destroyOnClose
    >
      <pre 
        ref={contentRef}
        className="ai-stream-content" 
        style={{ 
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
          margin: 0,
          fontFamily: 'inherit',
          overflow: 'auto'
        }}
      >
        {text || '正在生成中...'}
      </pre>
    </Modal>
  );
}

export default AIStreamModal;


