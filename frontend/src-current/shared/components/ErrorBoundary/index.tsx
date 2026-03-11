/**
 * ErrorBoundary Component
 * Catches React rendering errors and displays fallback UI
 */

import React, { ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('[ERROR BOUNDARY] React error caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          padding: '40px 20px',
          fontFamily: 'monospace',
          backgroundColor: '#fee',
          color: '#c00',
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <div style={{ maxWidth: '600px', whiteSpace: 'pre-wrap' }}>
            <h1>❌ App Error</h1>
            <p><strong>{this.state.error?.message}</strong></p>
            <details style={{ marginTop: '20px', cursor: 'pointer' }}>
              <summary>Stack Trace</summary>
              <pre style={{ 
                background: '#fff',
                padding: '12px',
                borderRadius: '4px',
                overflow: 'auto',
                fontSize: '12px',
              }}>
                {this.state.error?.stack}
              </pre>
            </details>
            <button 
              onClick={() => window.location.reload()}
              style={{
                marginTop: '20px',
                padding: '10px 20px',
                cursor: 'pointer',
                background: '#c00',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '14px',
              }}
            >
              Reload Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
