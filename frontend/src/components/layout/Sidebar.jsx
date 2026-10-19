import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  MapPin,
  GitCompare,
  Package,
  History,
  GitGraph,
  Settings,
  Navigation,
} from 'lucide-react';

export const Sidebar = () => {
  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Route Planner', path: '/planner', icon: Navigation },
    { name: 'Route Comparison', path: '/compare', icon: GitCompare },
    { name: 'Deliveries', path: '/deliveries', icon: Package },
    { name: 'Route History', path: '/history', icon: History },
    { name: 'Algorithm Trace', path: '/visualizer', icon: GitGraph },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  return (
    <aside className="w-64 bg-slate-900 text-white flex flex-col shrink-0 border-r border-slate-800">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-slate-800 space-x-3">
        <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-md shadow-blue-500/20">
          <Navigation className="w-5 h-5 transform -rotate-45" />
        </div>
        <div>
          <span className="font-bold text-lg tracking-tight text-white">RouteWise</span>
          <span className="block text-[10px] uppercase tracking-wider font-semibold text-blue-400">Optimization Engine</span>
        </div>
      </div>

      {/* Nav Links */}
      <nav className="flex-1 py-6 px-3 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
          Planning & Fleet
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              <Icon className="w-4 h-4 mr-3 shrink-0" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40 text-xs text-slate-400">
        <div className="flex items-center justify-between mb-1">
          <span className="font-medium text-slate-300">Engine Core</span>
          <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800/40 font-mono">v1.0-LIVE</span>
        </div>
        <p className="text-[11px] text-slate-400">Dijkstra • A* • 2-Opt • Held-Karp</p>
      </div>
    </aside>
  );
};
