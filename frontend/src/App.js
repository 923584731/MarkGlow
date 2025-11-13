import React, { useState, useEffect } from 'react';
import { Layout, Menu, message, Modal } from 'antd';
import { EditOutlined, FileTextOutlined } from '@ant-design/icons';
import './App.css';
import Editor from './components/Editor';
import DocumentList from './components/DocumentList';
import DocumentView from './components/DocumentView';
import { getDocuments, saveDocument, updateDocument, deleteDocument, searchDocuments } from './services/api';

const { Header, Content } = Layout;

function App() {
  const [currentView, setCurrentView] = useState('editor'); // 'editor', 'list', 'view'
  const [documents, setDocuments] = useState([]);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      setLoading(true);
      const data = await getDocuments();
      setDocuments(data);
    } catch (error) {
      console.error('加载文档列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDocument = async (documentData) => {
    try {
      if (documentData.id) {
        await updateDocument(documentData.id, documentData);
      } else {
        await saveDocument(documentData);
      }
      await loadDocuments();
      message.success('文档保存成功！');
    } catch (error) {
      console.error('保存文档失败:', error);
      message.error('保存文档失败，请重试');
    }
  };

  const handleDeleteDocument = async (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个文档吗？',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteDocument(id);
          await loadDocuments();
          if (selectedDocument && selectedDocument.id === id) {
            setCurrentView('list');
            setSelectedDocument(null);
          }
          message.success('文档删除成功');
        } catch (error) {
          console.error('删除文档失败:', error);
          message.error('删除文档失败，请重试');
        }
      }
    });
  };

  const handleViewDocument = (document) => {
    setSelectedDocument(document);
    setCurrentView('view');
  };

  const handleEditDocument = (document) => {
    setSelectedDocument(document);
    setCurrentView('editor');
  };

  const handleSearch = async (keyword, type = 'all') => {
    try {
      setLoading(true);
      const data = await searchDocuments(keyword, type);
      setDocuments(data);
    } catch (error) {
      console.error('搜索失败:', error);
      message.error('搜索失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setSelectedDocument(null);
  };

  const handleBackToEditor = () => {
    setCurrentView('editor');
    setSelectedDocument(null);
  };

  return (
    <Layout className="App" style={{ minHeight: '100vh' }}>
      <Header style={{ 
        background: '#fff', 
        padding: '0 24px', 
        display: 'flex', 
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      }}>
        <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
          MarkGlow
        </h1>
        <Menu
          mode="horizontal"
          selectedKeys={[currentView]}
          style={{ borderBottom: 'none', flex: 1, justifyContent: 'flex-end' }}
        >
          <Menu.Item 
            key="editor" 
            icon={<EditOutlined />}
            onClick={handleBackToEditor}
          >
            编辑器
          </Menu.Item>
          <Menu.Item 
            key="list" 
            icon={<FileTextOutlined />}
            onClick={() => setCurrentView('list')}
          >
            文档列表
          </Menu.Item>
        </Menu>
      </Header>

      <Content style={{ padding: '24px', background: '#f0f2f5' }}>
        {currentView === 'editor' && (
          <Editor onSave={handleSaveDocument} document={selectedDocument} />
        )}
        {currentView === 'list' && (
          <DocumentList
            documents={documents}
            loading={loading}
            onView={handleViewDocument}
            onDelete={handleDeleteDocument}
            onRefresh={loadDocuments}
            onSearch={handleSearch}
            onEdit={handleEditDocument}
          />
        )}
        {currentView === 'view' && selectedDocument && (
          <DocumentView
            document={selectedDocument}
            onBack={handleBackToList}
          />
        )}
      </Content>
    </Layout>
  );
}

export default App;

