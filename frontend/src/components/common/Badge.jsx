import React from 'react';

export const StatusBadge = ({ status }) => {
  const styles = {
    PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
    ASSIGNED: 'bg-blue-50 text-blue-700 border-blue-200',
    IN_TRANSIT: 'bg-purple-50 text-purple-700 border-purple-200',
    DELIVERED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    CANCELLED: 'bg-rose-50 text-rose-700 border-rose-200',
  };

  const labels = {
    PENDING: 'Pending',
    ASSIGNED: 'Assigned',
    IN_TRANSIT: 'In Transit',
    DELIVERED: 'Delivered',
    CANCELLED: 'Cancelled',
  };

  const style = styles[status] || 'bg-slate-50 text-slate-700 border-slate-200';
  const label = labels[status] || status;

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${style}`}>
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70"></span>
      {label}
    </span>
  );
};

export const PriorityBadge = ({ priority }) => {
  const styles = {
    LOW: 'bg-slate-100 text-slate-700 border-slate-200',
    MEDIUM: 'bg-sky-50 text-sky-700 border-sky-200',
    HIGH: 'bg-orange-50 text-orange-700 border-orange-200',
    URGENT: 'bg-red-50 text-red-700 border-red-200 font-semibold',
  };

  const style = styles[priority] || 'bg-slate-50 text-slate-700 border-slate-200';

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${style}`}>
      {priority}
    </span>
  );
};
