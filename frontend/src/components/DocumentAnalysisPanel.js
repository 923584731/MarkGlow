import React, { useState, useEffect } from 'react';
import { Card, Statistic, Row, Col, Button, Spin, Progress, Tag } from 'antd';
import { 
  FileTextOutlined, ClockCircleOutlined, 
  ReadOutlined, BarChartOutlined 
} from '@ant-design/icons';
import { analyzeContent } from '../services/api';
import './DocumentAnalysisPanel.css';

function DocumentAnalysisPanel({ content, documentId, onAnalyze }) {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (content) {
      handleAnalyze();
    }
  }, [content]);

  const handleAnalyze = async () => {
    if (!content || !content.trim()) {
      return;
    }
    
    setLoading(true);
    try {
      const result = await analyzeContent(content);
      setAnalysis(result);
      if (onAnalyze) {
        onAnalyze(result);
      }
    } catch (error) {
      console.error('分析失败:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '20px' }}>
        <Spin size="large" />
        <p style={{ marginTop: 16 }}>正在分析文档...</p>
      </div>
    );
  }

  if (!analysis) {
    return (
      <Card>
        <Button type="primary" block onClick={handleAnalyze} disabled={!content}>
          分析文档
        </Button>
      </Card>
    );
  }

  const { complexity } = analysis;
  const readabilityLevel = complexity?.readabilityScore >= 80 ? '简单' : 
                          complexity?.readabilityScore >= 60 ? '中等' : '复杂';

  return (
    <div className="document-analysis-panel">
      <Card 
        title={
          <span>
            <BarChartOutlined /> 文档分析
          </span>
        }
        extra={
          <Button size="small" onClick={handleAnalyze}>
            重新分析
          </Button>
        }
      >
        <Row gutter={[16, 16]}>
          <Col xs={12} sm={12} md={6}>
            <Statistic
              title="总字符数"
              value={analysis.totalChars}
              prefix={<FileTextOutlined />}
            />
          </Col>
          <Col xs={12} sm={12} md={6}>
            <Statistic
              title="中文字符"
              value={analysis.chineseChars}
            />
          </Col>
          <Col xs={12} sm={12} md={6}>
            <Statistic
              title="英文单词"
              value={analysis.englishWords}
            />
          </Col>
          <Col xs={12} sm={12} md={6}>
            <Statistic
              title="阅读时间"
              value={analysis.readingTimeFormatted}
              prefix={<ClockCircleOutlined />}
            />
          </Col>
        </Row>

        {complexity && (
          <>
            <Card 
              type="inner" 
              title="复杂度分析" 
              style={{ marginTop: 16 }}
            >
              <Row gutter={[16, 16]}>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">标题层级</div>
                    <div className="metric-value">
                      <Tag color="blue">{complexity.headingDepth} 级</Tag>
                    </div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">标题数量</div>
                    <div className="metric-value">{complexity.headingCount}</div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">段落数</div>
                    <div className="metric-value">{complexity.paragraphCount}</div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">平均段落长度</div>
                    <div className="metric-value">{complexity.averageParagraphLength} 字</div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">代码块</div>
                    <div className="metric-value">{complexity.codeBlockCount}</div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">链接数</div>
                    <div className="metric-value">{complexity.linkCount}</div>
                  </div>
                </Col>
                <Col xs={12} sm={8} md={6}>
                  <div className="metric-item">
                    <div className="metric-label">列表项</div>
                    <div className="metric-value">{complexity.listItemCount}</div>
                  </div>
                </Col>
                <Col xs={24} sm={24} md={12}>
                  <div className="metric-item">
                    <div className="metric-label">可读性评分</div>
                    <div style={{ marginTop: 8 }}>
                      <Progress 
                        percent={complexity.readabilityScore} 
                        status={complexity.readabilityScore >= 60 ? 'success' : 'exception'}
                        format={() => `${complexity.readabilityScore}分 (${readabilityLevel})`}
                      />
                    </div>
                  </div>
                </Col>
              </Row>
            </Card>
          </>
        )}
      </Card>
    </div>
  );
}

export default DocumentAnalysisPanel;

