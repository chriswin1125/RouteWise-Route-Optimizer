import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import {
  Package,
  Navigation,
  TrendingDown,
  Percent,
  Plus,
  ArrowUpRight,
  Clock,
  Sparkles,
  GitCompare,
  GitGraph,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  Legend,
} from 'recharts';
import { LoadingSpinner, CardSkeleton } from '../components/common/LoadingSpinner';

const STATUS_COLORS = {
  PENDING: '#f59e0b',
  ASSIGNED: '#3b82f6',
  IN_TRANSIT: '#a855f7',
  DELIVERED: '#10b981',
  CANCELLED: '#ef4444',
};

export const DashboardPage = () => {
  const [summary, setSummary] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [sumData, anaData] = await Promise.all([
          api.getDashboardSummary(),
          api.getDashboardAnalytics(),
        ]);
        setSummary(sumData);
        setAnalytics(anaData);
      } catch (err) {
        console.error('Failed to load dashboard:', err);
        setError(err.message || 'Error connecting to analytics engine');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 bg-slate-200 rounded w-1/4 animate-pulse"></div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <CardSkeleton />
          <CardSkeleton />
          <CardSkeleton />
          <CardSkeleton />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 text-center bg-white rounded-2xl border border-red-200 text-red-600">
        <p className="font-semibold text-lg">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-semibold hover:bg-red-700"
        >
          Retry
        </button>
      </div>
    );
  }

  // Prepare Pie Chart data from summary.deliveriesByStatus
  const statusPieData = summary?.deliveriesByStatus
    ? Object.entries(summary.deliveriesByStatus)
        .filter(([_, count]) => count > 0)
        .map(([status, count]) => ({
          name: status.replace('_', ' '),
          value: count,
          color: STATUS_COLORS[status] || '#94a3b8',
        }))
    : [];

  return (
    <div className="space-y-8 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Logistics Command Center
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Real-time delivery fulfillment metrics, distance savings, and fleet performance.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <Link
            to="/planner"
            className="inline-flex items-center space-x-1.5 px-4 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
          >
            <Plus className="w-4 h-4" />
            <span>Create Route</span>
          </Link>
          <Link
            to="/deliveries"
            className="inline-flex items-center space-x-1.5 px-4 py-2.5 bg-white text-slate-700 border border-slate-300 rounded-xl text-sm font-semibold hover:bg-slate-50 transition-colors shadow-sm"
          >
            <Package className="w-4 h-4 text-slate-500" />
            <span>New Delivery</span>
          </Link>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Deliveries */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Total Deliveries</span>
            <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Package className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-slate-900 mt-3">{summary?.totalDeliveries ?? 0}</p>
          <div className="mt-2 flex items-center text-xs text-slate-500 space-x-2">
            <span className="text-emerald-600 font-semibold">{summary?.completedDeliveries ?? 0} completed</span>
            <span>•</span>
            <span className="text-amber-600 font-medium">{summary?.pendingDeliveries ?? 0} pending</span>
          </div>
        </div>

        {/* Routes Optimized */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Routes Optimized</span>
            <div className="w-9 h-9 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Navigation className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-slate-900 mt-3">{summary?.routesOptimized ?? 0}</p>
          <p className="mt-2 text-xs text-slate-500">2-Opt & Held-Karp runs recorded</p>
        </div>

        {/* Distance Saved */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Distance Saved</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingDown className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-emerald-600 mt-3">
            {summary?.totalDistanceSavedKm ?? 0} <span className="text-lg text-slate-500 font-semibold">km</span>
          </p>
          <p className="mt-2 text-xs text-slate-500">Verified versus baseline sequence</p>
        </div>

        {/* Average Route Efficiency */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Avg Efficiency Gain</span>
            <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
              <Percent className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-purple-600 mt-3">
            +{summary?.averageRouteEfficiencyPercent ?? 0}%
          </p>
          <p className="mt-2 text-xs text-slate-500">Average reduction in travel distance</p>
        </div>
      </div>

      {/* Visual Analytics Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Daily Distance Saved Chart (2 columns) */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-slate-900">Distance Saved Over Time</h2>
              <p className="text-xs text-slate-500">Calculated savings (km) across daily dispatches</p>
            </div>
          </div>

          <div className="h-64 w-full">
            {analytics?.dailySavings && analytics.dailySavings.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={analytics.dailySavings} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorSavings" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#2563eb" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#0f172a',
                      borderRadius: '8px',
                      color: '#fff',
                      fontSize: '12px',
                      border: 'none',
                    }}
                    formatter={(val) => [`${val} km`, 'Distance Saved']}
                  />
                  <Area type="monotone" dataKey="distanceSavedKm" stroke="#2563eb" strokeWidth={2.5} fillOpacity={1} fill="url(#colorSavings)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                No historical savings data recorded yet
              </div>
            )}
          </div>
        </div>

        {/* Deliveries by Status (1 column) */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm flex flex-col">
          <h2 className="text-base font-bold text-slate-900">Delivery Status Ratio</h2>
          <p className="text-xs text-slate-500 mb-2">Live fulfillment pipeline</p>

          <div className="h-48 w-full flex items-center justify-center my-auto">
            {statusPieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statusPieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={75}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {statusPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-slate-400 text-xs">No active deliveries</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs pt-3 border-t border-slate-100">
            {statusPieData.map((s) => (
              <div key={s.name} className="flex items-center space-x-1.5">
                <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: s.color }} />
                <span className="text-slate-600 truncate">{s.name}:</span>
                <span className="font-bold text-slate-900">{s.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick Workflows / Highlights & Recent Routes */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Quick Actions Panel */}
        <div className="bg-gradient-to-br from-slate-900 to-slate-800 rounded-2xl p-6 text-white shadow-sm flex flex-col justify-between">
          <div>
            <div className="inline-flex items-center space-x-2 px-2.5 py-1 rounded bg-blue-500/20 text-blue-300 text-xs font-semibold uppercase mb-4">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Recommended Actions</span>
            </div>
            <h3 className="text-lg font-bold text-white mb-2">Optimize Delivery Schedule</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              You have {summary?.pendingDeliveries ?? 0} pending deliveries awaiting route assignment. Launch the Route Planner to automatically sequence them.
            </p>
          </div>

          <div className="space-y-2.5">
            <Link
              to="/planner"
              className="w-full flex items-center justify-between p-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs transition-colors shadow-sm"
            >
              <span className="flex items-center gap-2">
                <Navigation className="w-4 h-4" />
                Launch Multi-Stop Planner
              </span>
              <ArrowUpRight className="w-4 h-4" />
            </Link>

            <Link
              to="/compare"
              className="w-full flex items-center justify-between p-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium text-xs border border-slate-700 transition-colors"
            >
              <span className="flex items-center gap-2">
                <GitCompare className="w-4 h-4 text-amber-400" />
                Compare Route Alternatives
              </span>
              <ArrowUpRight className="w-4 h-4" />
            </Link>

            <Link
              to="/visualizer"
              className="w-full flex items-center justify-between p-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium text-xs border border-slate-700 transition-colors"
            >
              <span className="flex items-center gap-2">
                <GitGraph className="w-4 h-4 text-purple-400" />
                Explore Dijkstra & A* Step Trace
              </span>
              <ArrowUpRight className="w-4 h-4" />
            </Link>
          </div>
        </div>

        {/* Recent Routes Table (2 columns) */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200/90 shadow-sm p-6 overflow-hidden">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-slate-900">Recent Optimized Routes</h2>
              <p className="text-xs text-slate-500">Most recent calculations persisted in database</p>
            </div>
            <Link to="/history" className="text-xs font-semibold text-blue-600 hover:text-blue-500">
              View All History &rarr;
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                <tr>
                  <th className="py-2.5 px-3 font-semibold">Route Name</th>
                  <th className="py-2.5 px-3 font-semibold">Method</th>
                  <th className="py-2.5 px-3 font-semibold">Distance</th>
                  <th className="py-2.5 px-3 font-semibold">Duration</th>
                  <th className="py-2.5 px-3 font-semibold">Savings</th>
                  <th className="py-2.5 px-3 font-semibold text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {summary?.recentRoutes && summary.recentRoutes.length > 0 ? (
                  summary.recentRoutes.map((r) => (
                    <tr key={r.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-3 font-semibold text-slate-900">
                        {r.name}
                        <span className="block text-[11px] text-slate-400 font-normal">
                          {r.stops?.length ?? 0} stops
                        </span>
                      </td>
                      <td className="py-3 px-3">
                        <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-100 text-slate-700">
                          {r.optimizationMethod}
                        </span>
                      </td>
                      <td className="py-3 px-3 font-medium text-slate-800">{r.totalDistanceKm} km</td>
                      <td className="py-3 px-3 text-slate-600">~{r.estimatedDurationMin} min</td>
                      <td className="py-3 px-3 font-bold text-emerald-600">
                        {r.savingsPercentage > 0 ? `-${r.savingsPercentage}%` : '0%'}
                      </td>
                      <td className="py-3 px-3 text-right">
                        <Link
                          to={`/history`}
                          className="text-blue-600 hover:text-blue-800 font-semibold"
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="py-8 text-center text-slate-400">
                      No saved routes found yet. Click 'Create Route' to calculate your first route.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
