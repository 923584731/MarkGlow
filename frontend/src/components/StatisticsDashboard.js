import React, { useState, useEffect } from 'react';
import { Card, Statistic, Row, Col, Select, Table, Tag, Space, Spin } from 'antd';
import { 
  ThunderboltOutlined, 
  DollarOutlined, 
  FileTextOutlined,
  BarChartOutlined 
} from '@ant-design/icons';
import { getStatisticsSummary, getUsageRecords } from '../services/api';
import './StatisticsDashboard.css';

const { Option } = Select;

function StatisticsDashboard() {
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [usageLoading, setUsageLoading] = useState(false);
  const [summary, setSummary] = useState(null);
  const [usageRecords, setUsageRecords] = useState([]);
  const [usagePagination, setUsagePagination] = useState({ current: 1, pageSize: 20, total: 0 });
  const [period, setPeriod] = useState('today');

  useEffect(() => {
    loadSummary();
  }, [period]);

  useEffect(() => {
    loadUsageRecords(true);
  }, [period, usagePagination.current, usagePagination.pageSize]);

  useEffect(() => {
    const refreshInterval = setInterval(() => {
      loadSummary();
      loadUsageRecords(true);
    }, 5000);
    return () => clearInterval(refreshInterval);
  }, [period, usagePagination.current, usagePagination.pageSize]);

  useEffect(() => {
    setUsagePagination((prev) => ({ ...prev, current: 1 }));
  }, [period]);

  const loadSummary = async () => {
    try {
      setSummaryLoading(true);
      const data = await getStatisticsSummary(period);
      setSummary(data);
    } catch (error) {
      console.error('加载统计摘要失败:', error);
    } finally {
      setSummaryLoading(false);
    }
  };

  const loadUsageRecords = async (keepCurrentPage = false) => {
    try {
      setUsageLoading(true);
      const end = new Date();
      const start = new Date();
      if (period === 'today') {
        start.setHours(0, 0, 0, 0);
      } else if (period === 'week') {
        start.setDate(start.getDate() - 7);
        start.setHours(0, 0, 0, 0);
      } else if (period === 'month') {
        start.setDate(start.getDate() - 30);
        start.setHours(0, 0, 0, 0);
      }
      // 确保end时间包含当前时刻（添加1秒缓冲）
      end.setSeconds(end.getSeconds() + 1);
      
      const page = keepCurrentPage ? usagePagination.current : 1;
      const data = await getUsageRecords(
        start.toISOString(),
        end.toISOString(),
        page - 1,
        usagePagination.pageSize
      );
      setUsageRecords(data.records || []);
      setUsagePagination((prev) => ({
        ...prev,
        current: keepCurrentPage ? prev.current : 1,
        total: data.total || 0,
      }));
    } catch (error) {
      console.error('加载使用统计失败:', error);
    } finally {
      setUsageLoading(false);
    }
  };

  const usageColumns = [
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => {
        if (!text) return '-';
        const date = new Date(text);
        return date.toLocaleString('zh-CN');
      },
    },
    {
      title: '操作类型',
      dataIndex: 'action',
      key: 'action',
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '服务商',
      dataIndex: 'provider',
      key: 'provider',
      render: (text) => (
        <Tag color={text === 'ernie' ? 'green' : 'orange'}>
          {text === 'ernie' ? '文心一言' : '通义千问'}
        </Tag>
      ),
    },
    {
      title: '模型',
      dataIndex: 'model',
      key: 'model',
      render: (val) => val || '-',
    },
    {
      title: '输入Token',
      dataIndex: 'inputTokens',
      key: 'inputTokens',
      align: 'right',
      render: (val) => val ? val.toLocaleString() : 0,
    },
    {
      title: '输出Token',
      dataIndex: 'outputTokens',
      key: 'outputTokens',
      align: 'right',
      render: (val) => val ? val.toLocaleString() : 0,
    },
    {
      title: '总Token',
      key: 'totalTokens',
      align: 'right',
      render: (_, record) => {
        const total = (record.inputTokens || 0) + (record.outputTokens || 0);
        return total.toLocaleString();
      },
    },
    {
      title: '成本（元）',
      dataIndex: 'cost',
      key: 'cost',
      align: 'right',
      render: (val) => val ? Number(val).toFixed(4) : '0.0000',
    },
    {
      title: '耗时（ms）',
      dataIndex: 'duration',
      key: 'duration',
      align: 'right',
      render: (val) => val ? val.toLocaleString() : 0,
    },
  ];

  const handleUsageTableChange = (pagination) => {
    setUsagePagination((prev) => ({
      ...prev,
      current: pagination.current,
      pageSize: pagination.pageSize,
    }));
  };

  if (summaryLoading && !summary) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="statistics-dashboard">
      <Card 
        title={
          <span>
            <BarChartOutlined /> AI使用统计
          </span>
        }
        extra={
          <Select
            value={period}
            onChange={setPeriod}
            style={{ width: 120 }}
          >
            <Option value="today">今日</Option>
            <Option value="week">本周</Option>
            <Option value="month">本月</Option>
          </Select>
        }
      >
        {summary && (
          <>
            <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="总调用次数"
                    value={summary.totalCalls || 0}
                    prefix={<ThunderboltOutlined />}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="总Token数"
                    value={summary.totalTokens || 0}
                    prefix={<FileTextOutlined />}
                    formatter={(value) => value.toLocaleString()}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="输入Token"
                    value={summary.totalInputTokens || 0}
                    formatter={(value) => value.toLocaleString()}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="输出Token"
                    value={summary.totalOutputTokens || 0}
                    formatter={(value) => value.toLocaleString()}
                  />
                </Card>
              </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
              <Col xs={24} sm={12}>
                <Card title="总成本">
                  <Statistic
                    value={summary.totalCost || 0}
                    prefix={<DollarOutlined />}
                    suffix="元"
                    precision={4}
                    valueStyle={{ color: '#cf1322', fontSize: '24px' }}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12}>
                <Card title="操作类型分布">
                  {summary.actionCounts && Object.keys(summary.actionCounts).length > 0 ? (
                    <Space wrap>
                      {Object.entries(summary.actionCounts).map(([action, count]) => (
                        <Tag key={action} color="blue">
                          {action}: {count}
                        </Tag>
                      ))}
                    </Space>
                  ) : (
                    <span>暂无数据</span>
                  )}
                </Card>
              </Col>
            </Row>

            <Row gutter={[16, 16]}>
              <Col xs={24}>
                <Card title="服务提供商分布">
                  {summary.providerCounts && Object.keys(summary.providerCounts).length > 0 ? (
                    <Space wrap>
                      {Object.entries(summary.providerCounts).map(([provider, count]) => (
                        <Tag key={provider} color={provider === 'ernie' ? 'green' : 'orange'}>
                          {provider === 'ernie' ? '文心一言' : '通义千问'}: {count}次
                        </Tag>
                      ))}
                    </Space>
                  ) : (
                    <span>暂无数据</span>
                  )}
                </Card>
              </Col>
            </Row>
          </>
        )}
      </Card>

      <Card 
        title="使用统计详情" 
        style={{ marginTop: 16 }}
      >
        <Table
          columns={usageColumns}
          dataSource={usageRecords}
          rowKey={(record) => record.id || `${record.action}-${record.createdAt}`}
          pagination={{
            current: usagePagination.current,
            pageSize: usagePagination.pageSize,
            total: usagePagination.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          loading={usageLoading}
          onChange={handleUsageTableChange}
        />
      </Card>
    </div>
  );
}

export default StatisticsDashboard;

