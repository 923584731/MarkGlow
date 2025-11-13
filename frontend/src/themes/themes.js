export const themes = {
  default: {
    color: '#24292e',
    backgroundColor: '#ffffff',
  },
  dark: {
    color: '#c9d1d9',
    backgroundColor: '#0d1117',
  },
  github: {
    color: '#24292e',
    backgroundColor: '#ffffff',
  },
  elegant: {
    color: '#2c3e50',
    backgroundColor: '#f8f9fa',
  },
  minimal: {
    color: '#333333',
    backgroundColor: '#ffffff',
  },
  cozy: {
    color: '#4a5568',
    backgroundColor: '#f7fafc',
  },
};

// 扩展主题样式
const themeStyles = {
  dark: {
    '--pre-bg': '#161b22',
    '--code-bg': '#21262d',
    '--blockquote-border': '#30363d',
    '--table-border': '#30363d',
    '--table-header-bg': '#161b22',
  },
  elegant: {
    '--pre-bg': '#e9ecef',
    '--code-bg': '#f1f3f5',
    '--blockquote-border': '#adb5bd',
  },
  cozy: {
    '--pre-bg': '#edf2f7',
    '--code-bg': '#e2e8f0',
    '--blockquote-border': '#cbd5e0',
  },
};

// 应用主题样式到预览区域
export const applyThemeStyles = (theme, element) => {
  if (!element) return;
  
  const styles = themeStyles[theme];
  if (styles) {
    Object.entries(styles).forEach(([property, value]) => {
      element.style.setProperty(property, value);
    });
  }
};

