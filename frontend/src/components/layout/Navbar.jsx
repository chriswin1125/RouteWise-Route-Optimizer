import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { LogOut, User as UserIcon, Building2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0 z-20">
      <div className="flex items-center space-x-3">
        <h1 className="text-sm font-semibold text-slate-700 hidden md:block">
          Logistics Route Management Platform
        </h1>
      </div>

      <div className="flex items-center space-x-4">
        {user && (
          <div className="flex items-center space-x-3 border-r border-slate-200 pr-4">
            <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs uppercase">
              {user.fullName ? user.fullName.charAt(0) : 'U'}
            </div>
            <div className="text-left hidden sm:block">
              <p className="text-xs font-semibold text-slate-900 leading-tight">{user.fullName || user.email}</p>
              <p className="text-[11px] text-slate-500 leading-tight flex items-center gap-1">
                <Building2 className="w-3 h-3 text-slate-400" />
                {user.companyName || 'Independent Fleet'}
              </p>
            </div>
          </div>
        )}

        <button
          onClick={handleLogout}
          className="flex items-center space-x-1.5 px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors border border-transparent hover:border-red-100"
          title="Sign out"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span className="hidden sm:inline">Sign Out</span>
        </button>
      </div>
    </header>
  );
};
