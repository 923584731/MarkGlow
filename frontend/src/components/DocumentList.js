import React, { useState } from 'react';
import { List, Input, Select, Card, Button, Space, Tag, Empty, Spin, Typography } from 'antd';
import { SearchOutlined, ReloadOutlined, EyeOutlined, DeleteOutlined, CloseOutlined, EditOutlined } from '@ant-design/icons';
import './DocumentList.css';

const { Paragraph, Text } = Typography;

function DocumentList({ documents, loading, onView, onDelete, onRefresh, onSearch, onEdit }) {
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchType, setSearchType] = useState('all');

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
  };

  const handleSearch = () => {
    if (onSearch) {
      onSearch(searchKeyword, searchType);
    }
  };

  const handleClearSearch = () => {
    setSearchKeyword('');
    if (onSearch) {
      onSearch('', searchType);
    }
  };

  return (
    <div className="document-list-container">
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>文档列表</h2>
          <Space>
            <Space.Compact>
              <Select
                value={searchType}
                onChange={setSearchType}
                style={{ width: 100 }}
              >
                <Select.Option value="all">全部</Select.Option>
                <Select.Option value="title">标题</Select.Option>
              </Select>
              <Input
                placeholder="搜索文档..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onPressEnter={handleSearch}
                style={{ width: 300 }}
                prefix={<SearchOutlined />}
                suffix={searchKeyword && (
                  <CloseOutlined
                    onClick={handleClearSearch}
                    style={{ cursor: 'pointer', color: '#999' }}
                  />
                )}
              />
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                搜索
              </Button>
            </Space.Compact>
            <Button icon={<ReloadOutlined />} onClick={onRefresh}>
              刷新
            </Button>
          </Space>
        </div>

        {searchKeyword && (
          <div style={{ marginBottom: 16, color: '#666' }}>
            找到 <strong>{documents.length}</strong> 个结果，关键词: <strong>"{searchKeyword}"</strong>
          </div>
        )}

        <Spin spinning={loading}>
          {documents.length === 0 ? (
            <Empty
              description={
                <span>
                  {searchKeyword ? '没有找到匹配的文档' : '还没有保存的文档'}
                </span>
              }
            >
              {!searchKeyword && (
                <p style={{ color: '#999' }}>在编辑器中创建并保存您的第一个文档吧！</p>
              )}
            </Empty>
          ) : (
            <List
              grid={{
                gutter: 16,
                xs: 1,
                sm: 2,
                md: 3,
                lg: 4,
                xl: 4,
                xxl: 6,
              }}
              dataSource={documents}
              pagination={{
                pageSize: 12,
                showSizeChanger: true,
                showTotal: (total) => `共 ${total} 条`,
              }}
              renderItem={(doc) => {
                const preview = doc.beautifiedContent || doc.originalContent || '无内容';
                return (
                  <List.Item key={doc.id}>
                    <Card
                      hoverable
                      title={
                        <Space>
                          <Text strong ellipsis style={{ maxWidth: 180 }}>{doc.title || '未命名文档'}</Text>
                          <Tag color="blue">{doc.theme || 'default'}</Tag>
                        </Space>
                      }
                      extra={
                        <Space size="small">
                          <Button
                            type="link"
                            icon={<EditOutlined />}
                            onClick={() => onEdit && onEdit(doc)}
                          >
                            编辑
                          </Button>
                          <Button
                            type="link"
                            icon={<EyeOutlined />}
                            onClick={() => onView(doc)}
                          >
                            查看
                          </Button>
                          <Button
                            type="link"
                            danger
                            icon={<DeleteOutlined />}
                            onClick={() => onDelete(doc.id)}
                          >
                            删除
                          </Button>
                        </Space>
                      }
                      styles={{
                        body: { minHeight: 160, display: 'flex', flexDirection: 'column' },
                      }}
                    >
                      <Paragraph
                        ellipsis={{ rows: 4, expandable: false }}
                        style={{ color: '#666', marginBottom: 12 }}
                      >
                        {preview}
                      </Paragraph>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#999' }}>
                        <span>更新时间：{doc.updatedAt ? formatDate(doc.updatedAt) : '-'}</span>
                      </div>
                    </Card>
                  </List.Item>
                );
              }}
            />
          )}
        </Spin>
      </Card>
    </div>
  );
}

export default DocumentList;

