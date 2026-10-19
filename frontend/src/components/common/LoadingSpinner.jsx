import React from 'react';

export const LoadingSpinner = ({ size = 'md', text = 'Loading...' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
  };

  return (
    <div className="flex flex-col items-center justify-center p-6 space-y-3">
      <div
        className={`${sizeClasses[size] || sizeClasses.md} border-blue-600 border-t-transparent rounded-full animate-spin`}
      />
      {text && <p className="text-sm text-slate-500 font-medium animate-pulse">{text}</p>}
    </div>
  );
};

export const CardSkeleton = () => (
  <div className="bg-white rounded-xl border border-slate-200 p-6 animate-pulse space-y-4 shadow-sm">
    <div className="h-4 bg-slate-200 rounded w-1/3"></div>
    <div className="h-8 bg-slate-200 rounded w-1/2"></div>
    <div className="h-3 bg-slate-100 rounded w-full"></div>
  </div>
);
