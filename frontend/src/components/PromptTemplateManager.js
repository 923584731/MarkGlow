import React, { useState, useEffect } from 'react';
import { 
  Modal, Button, Form, Input, Select, Table, Space, message, 
  Card, Tag, Popconfirm, Drawer 
} from 'antd';
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, 
  FileTextOutlined, EyeOutlined 
} from '@ant-design/icons';
import {
  getPromptTemplates, createPromptTemplate, updatePromptTemplate,
  deletePromptTemplate, renderPromptTemplate
} from '../services/api';
import './PromptTemplateManager.css';

const { TextArea } = Input;
const { Option } = Select;

const categories = ['写作助手', '内容优化', '代码相关', '分析工具', '其他'];

function PromptTemplateManager({ onSelectTemplate, visible, onClose }) {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const [previewForm] = Form.useForm();
  const [editingTemplate, setEditingTemplate] = useState(null);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);

  useEffect(() => {
    if (visible) {
      loadTemplates();
    }
  }, [visible, selectedCategory]);

  const loadTemplates = async () => {
    try {
      setLoading(true);
      const data = await getPromptTemplates(selectedCategory);
      setTemplates(data);
    } catch (error) {
      console.error('加载模板失败:', error);
      message.error('加载模板失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingTemplate(null);
    form.resetFields();
    form.setFieldsValue({ category: '写作助手' });
    setModalVisible(true);
  };

  const handleEdit = (template) => {
    setEditingTemplate(template);
    form.setFieldsValue({
      name: template.name,
      description: template.description,
      category: template.category,
      content: template.content,
      variables: template.variables
    });
  };

  const handleDelete = async (id) => {
    try {
      await deletePromptTemplate(id);
      message.success('删除成功');
      loadTemplates();
    } catch (error) {
      console.error('删除失败:', error);
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values) => {
    try {
      if (editingTemplate) {
        await updatePromptTemplate(editingTemplate.id, values);
        message.success('更新成功');
      } else {
        await createPromptTemplate(values);
        message.success('创建成功');
      }
      form.resetFields();
      setEditingTemplate(null);
      setModalVisible(false);
      loadTemplates();
    } catch (error) {
      console.error('保存失败:', error);
      message.error('保存失败');
    }
  };

  const handlePreview = async (template) => {
    previewForm.setFieldsValue({ templateId: template.id });
    // 初始化变量表单
    const vars = {};
    if (template.variables) {
      template.variables.forEach(v => {
        vars[v.name] = v.defaultValue || '';
      });
    }
    previewForm.setFieldsValue({ variables: vars });
    setPreviewVisible(true);
  };

  const handleRenderPreview = async () => {
    try {
      const values = previewForm.getFieldsValue();
      const rendered = await renderPromptTemplate(values.templateId, values.variables);
      previewForm.setFieldsValue({ rendered });
    } catch (error) {
      console.error('渲染失败:', error);
      message.error('渲染失败');
    }
  };

  const handleSelectTemplate = async (template) => {
    if (onSelectTemplate) {
      // 如果有变量，先预览让用户填写
      if (template.variables && template.variables.length > 0) {
        handlePreview(template);
      } else {
        // 直接使用模板
        const rendered = await renderPromptTemplate(template.id, {});
        onSelectTemplate(rendered);
        onClose();
      }
    }
  };

  const handleUseRendered = () => {
    const rendered = previewForm.getFieldValue('rendered');
    if (rendered && onSelectTemplate) {
      onSelectTemplate(rendered);
      setPreviewVisible(false);
      onClose();
    }
  };

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category) => <Tag color="blue">{category}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record)}
          >
            预览
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除这个模板吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
          {onSelectTemplate && (
            <Button
              type="primary"
              size="small"
              onClick={() => handleSelectTemplate(record)}
            >
              使用
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <>
      <Drawer
        title="提示词模板库"
        placement="right"
        width={800}
        onClose={onClose}
        open={visible}
        extra={
          <Space>
            <Select
              style={{ width: 150 }}
              placeholder="筛选分类"
              allowClear
              value={selectedCategory}
              onChange={setSelectedCategory}
            >
              {categories.map(cat => (
                <Option key={cat} value={cat}>{cat}</Option>
              ))}
            </Select>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建模板
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={templates}
          loading={loading}
          rowKey="id"
          pagination={{ pageSize: 10 }}
        />

        <Modal
          title={editingTemplate ? '编辑模板' : '新建模板'}
          open={modalVisible || editingTemplate !== null}
          onCancel={() => {
            setEditingTemplate(null);
            setModalVisible(false);
            form.resetFields();
          }}
          footer={null}
          width={700}
        >
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
          >
            <Form.Item
              name="name"
              label="模板名称"
              rules={[{ required: true, message: '请输入模板名称' }]}
            >
              <Input placeholder="例如：技术文档写作" />
            </Form.Item>
            <Form.Item
              name="description"
              label="描述"
            >
              <Input placeholder="模板用途说明" />
            </Form.Item>
            <Form.Item
              name="category"
              label="分类"
              rules={[{ required: true, message: '请选择分类' }]}
            >
              <Select>
                {categories.map(cat => (
                  <Option key={cat} value={cat}>{cat}</Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              name="content"
              label="模板内容"
              rules={[{ required: true, message: '请输入模板内容' }]}
              extra="使用 {{变量名}} 来定义变量，例如：请帮我写一篇关于{{topic}}的文章"
            >
              <TextArea
                rows={8}
                placeholder="请输入模板内容，使用 {{变量名}} 定义变量"
              />
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit">
                  {editingTemplate ? '更新' : '创建'}
                </Button>
                <Button onClick={() => {
                  setEditingTemplate(null);
                  form.resetFields();
                }}>
                  取消
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Modal>

        <Modal
          title="预览模板"
          open={previewVisible}
          onCancel={() => setPreviewVisible(false)}
          footer={[
            <Button key="cancel" onClick={() => setPreviewVisible(false)}>
              关闭
            </Button>,
            <Button key="render" type="primary" onClick={handleRenderPreview}>
              渲染
            </Button>,
            onSelectTemplate && (
              <Button key="use" type="primary" onClick={handleUseRendered}>
                使用此模板
              </Button>
            ),
          ]}
          width={700}
        >
          <Form form={previewForm} layout="vertical">
            <Form.Item name="templateId" hidden>
              <Input />
            </Form.Item>
            {previewForm.getFieldValue('templateId') && (() => {
              const template = templates.find(t => t.id === previewForm.getFieldValue('templateId'));
              if (!template || !template.variables) return null;
              return (
                <>
                  {template.variables.map(v => (
                    <Form.Item
                      key={v.name}
                      name={['variables', v.name]}
                      label={v.description || v.name}
                    >
                      <Input placeholder={v.defaultValue || `请输入${v.name}`} />
                    </Form.Item>
                  ))}
                </>
              );
            })()}
            <Form.Item name="rendered" label="渲染结果">
              <TextArea rows={6} readOnly />
            </Form.Item>
          </Form>
        </Modal>
      </Drawer>
    </>
  );
}

export default PromptTemplateManager;

