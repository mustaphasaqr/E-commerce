/**
 * Root App Component
 * Main entry point for the React application
 */

import React from 'react';
import { Provider } from 'react-redux';
import { RouterProvider } from 'react-router-dom';
import store from '@store/index';
import router from '@router/index';

// Debug component
const DebugInfo: React.FC = () => {
  const [showDebug, setShowDebug] = React.useState(false);
  const storeState = store.getState();
  
  if (!showDebug) {
    return (
      <div style={{ position: 'fixed', bottom: 20, right: 20, zIndex: 9999 }}>
        <button 
          onClick={() => setShowDebug(true)}
          style={{ padding: '8px 12px', cursor: 'pointer', fontSize: '12px' }}
        >
          ?
        </button>
      </div>
    );
  }

  return (
    <div style={{
      position: 'fixed',
      bottom: 20,
      right: 20,
      zIndex: 9999,
      background: '#f0f0f0',
      padding: '12px',
      borderRadius: '4px',
      maxWidth: '300px',
      maxHeight: '400px',
      overflow: 'auto',
      fontSize: '11px',
      fontFamily: 'monospace',
    }}>
      <div>{`URL: ${window.location.href}`}</div>
      <div>{`Auth ${storeState.auth.isAuthenticated ? '✓' : '✗'}`}</div>
      <div>{`User: ${storeState.auth.user?.email || 'none'}`}</div>
      <div>{`Token: ${store.getState().auth.tokens?.accessToken ? '✓' : '✗'}`}</div>
      <button 
        onClick={() => setShowDebug(false)}
        style={{ marginTop: '8px', padding: '4px 8px', cursor: 'pointer' }}
      >
        Close
      </button>
    </div>
  );
};

// Error Boundary Component
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: Error | null }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('[ERROR BOUNDARY] React Error:', error);
    console.error('[ERROR BOUNDARY] Component Stack:', errorInfo.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          padding: '40px',
          textAlign: 'center',
          backgroundColor: '#fff5f5',
          color: '#c53030',
          fontFamily: 'monospace',
          whiteSpace: 'pre-wrap',
          fontSize: '12px',
          maxHeight: '100vh',
          overflow: 'auto',
        }}>
          <h1>⚠️ Render Error</h1>
          <p>{this.state.error?.message}</p>
          <button onClick={() => window.location.reload()}>Reload Page</button>
        </div>
      );
    }

    return this.props.children;
  }
}

/**
 * Auth Initializer Component
 * Loads auth state from localStorage on app mount
 * Simplified: No Redux dependency during init
 */
const AuthInitializer: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isInitialized, setIsInitialized] = React.useState(false);

  React.useEffect(() => {
    console.log('[AUTH INIT] Starting');
    
    // Always complete within 2 seconds max
    const timeout = setTimeout(() => {
      console.log('[AUTH INIT] Timeout fired, completing');
      setIsInitialized(true);
    }, 2000);

    // Minimal init - just mark as done
    try {
      console.log('[AUTH INIT] Checking localStorage');
      const stored = localStorage.getItem('auth_token');
      console.log('[AUTH INIT] Token exists:', !!stored);
      
      // That's it - router will handle auth state from Redux store
      console.log('[AUTH INIT] Done checking');
      clearTimeout(timeout);
      setIsInitialized(true);
    } catch (err) {
      console.error('[AUTH INIT] Error:', err);
      clearTimeout(timeout);
      setIsInitialized(true);
    }
  }, []);

  if (!isInitialized) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        backgroundColor: '#667eea',
        fontFamily: '-apple-system, BlinkMacSystemFont, Segoe UI, sans-serif',
      }}>
        <div style={{ textAlign: 'center', color: 'white' }}>
          <div style={{
            width: '50px',
            height: '50px',
            border: '4px solid rgba(255,255,255,0.3)',
            borderTop: '4px solid white',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            margin: '0 auto 20px',
          }} />
          <h1 style={{ margin: 0, fontSize: '28px', letterSpacing: '-0.5px' }}>E-Commerce</h1>
          <p style={{ margin: '8px 0 0 0', fontSize: '14px', opacity: 0.9 }}>
            Loading application...
          </p>
        </div>
        <style>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  return <>{children}</>;
};

function App() {
  return (
    <ErrorBoundary>
      <Provider store={store}>
        <AuthInitializer>
          <DebugInfo />
          <RouterProvider router={router} />
        </AuthInitializer>
      </Provider>
    </ErrorBoundary>
  );
}

export default App;
