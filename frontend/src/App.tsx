/**
 * Root App Component
 * Main entry point for the React application
 */

function App() {
  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center',
      minHeight: '100vh',
      backgroundColor: '#f5f5f5',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <div style={{
        textAlign: 'center',
        padding: '2rem',
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        maxWidth: '500px'
      }}>
        <h1 style={{ color: '#333', marginBottom: '1rem' }}>
          ✨ E-Commerce Frontend
        </h1>
        <p style={{ color: '#666', fontSize: '1.1rem', marginBottom: '2rem' }}>
          React 18 + TypeScript + Redux Toolkit + Vite
        </p>
        <div style={{
          backgroundColor: '#f0f4ff',
          padding: '1rem',
          borderRadius: '8px',
          marginBottom: '2rem',
          textAlign: 'left',
          fontSize: '0.95rem'
        }}>
          <p style={{ margin: '0.5rem 0', color: '#333' }}>
            ✅ Development server running
          </p>
          <p style={{ margin: '0.5rem 0', color: '#333' }}>
            ✅ Hot module reload enabled
          </p>
          <p style={{ margin: '0.5rem 0', color: '#333' }}>
            ✅ TypeScript strict mode
          </p>
          <p style={{ margin: '0.5rem 0', color: '#333' }}>
            ✅ API proxy to localhost:8080
          </p>
        </div>
        <p style={{ color: '#999', fontSize: '0.9rem' }}>
          🚀 Ready to build amazing features!
        </p>
      </div>
    </div>
  )
}

export default App
