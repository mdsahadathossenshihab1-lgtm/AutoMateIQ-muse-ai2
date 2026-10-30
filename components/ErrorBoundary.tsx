import React, { Component, ErrorInfo, ReactNode } from 'react';
import { RefreshCw, AlertTriangle } from 'lucide-react';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
  }

  private handleReload = () => {
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6 font-sans">
          <div className="bg-white p-8 md:p-10 rounded-3xl shadow-2xl max-w-lg w-full text-center border border-slate-100 relative overflow-hidden">
            {/* Background decoration */}
            <div className="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-red-500 to-orange-500"></div>
            
            <div className="w-24 h-24 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6 shadow-inner">
              <AlertTriangle className="text-red-500 w-12 h-12" />
            </div>
            
            <h1 className="text-2xl font-bold text-slate-900 mb-2">Something went wrong</h1>
            <p className="text-slate-500 mb-8 text-base leading-relaxed">
              Failed to load the app. Try reloading it.
            </p>
            
            <div className="space-y-3">
              <button
                onClick={this.handleReload}
                className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-bold transition-all shadow-lg hover:shadow-xl flex items-center justify-center gap-2 active:scale-95"
              >
                <RefreshCw size={18} />
                Reload Page
              </button>
            </div>

            {this.state.error && (
              <details className="mt-8 p-4 bg-slate-100 rounded-xl text-left border border-slate-200 open:bg-white open:shadow-sm transition-all cursor-pointer">
                 <summary className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2 select-none">Error Details</summary>
                 <p className="text-xs font-mono text-red-500 break-all bg-red-50 p-2 rounded border border-red-100">
                   {this.state.error.toString()}
                 </p>
              </details>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;