import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

// Prevent service worker caching issues
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then((registrations) => {
    registrations.forEach((registration) => {
      registration.unregister();
    });
  }).catch(error => {
    console.warn('Service Worker unregistration failed:', error);
  });
}

const rootElement = document.getElementById('root');

if (!rootElement) {
  console.error("Could not find root element to mount to");
} else {
  try {
    const root = ReactDOM.createRoot(rootElement);
    root.render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    );
  } catch (e) {
    console.error("Root render failed:", e);
    rootElement.innerHTML = '<div style="padding: 20px; text-align: center; font-family: sans-serif;"><h1>Failed to start application</h1><p>Please reload the page.</p></div>';
  }
}