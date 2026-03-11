import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import '@shared/styles/globals.css'

// Create and render with bulletproof error handling
const root = document.getElementById('root')

if (!root) {
  document.body.innerHTML = `
    <div style="
      padding: 40px 20px;
      background: #fee;
      color: #c00;
      font-family: monospace;
      font-size: 14px;
    ">
      <h1>❌ App Error</h1>
      <p><strong>Root element not found</strong></p>
      <p>The #root div in index.html is missing or has wrong ID</p>
      <p>Check: &lt;div id="root"&gt;&lt;/div&gt; in index.html</p>
    </div>
  `
  throw new Error('Root element #root not found')
}

try {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>,
  )
} catch (error) {
  const errorMsg = error instanceof Error ? error.message : String(error)
  document.body.innerHTML = `
    <div style="
      padding: 40px 20px;
      background: #fee;
      color: #c00;
      font-family: monospace;
      font-size: 14px;
      white-space: pre-wrap;
    ">
      <h1>❌ React Render Error</h1>
      <p><strong>${errorMsg}</strong></p>
      <details style="margin-top: 20px; cursor: pointer;">
        <summary>Stack trace</summary>
        <pre>${(error instanceof Error) ? error.stack : 'unknown'}</pre>
      </details>
      <button onclick="location.reload()" style="
        margin-top: 20px;
        padding: 10px 20px;
        cursor: pointer;
        background: #c00;
        color: white;
        border: none;
        border-radius: 4px;
      ">Reload Page</button>
    </div>
  `
  console.error('Failed to render React app:', error)
}

