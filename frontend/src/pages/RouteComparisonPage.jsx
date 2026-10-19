import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { RouteMap } from '../components/map/RouteMap';
import {
  GitCompare,
  TrendingDown,
  Clock,
  Navigation,
  Layers,
  Info,
  CheckCircle2,
  Sparkles,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const RouteComparisonPage = () => {
  const { user } = useAuth();

  const [depot, setDepot] = useState({
    address: user?.defaultDepotAddress || 'Central Hub, 500 Howard St, San Francisco, CA',
    latitude: user?.defaultDepotLat || 37.7885,
    longitude: user?.defaultDepotLng || -122.3995,
  });

  const [stops, setStops] = useState([
    {
      sequence: 1,
      customerName: 'Fishermans Wharf Gift Shop',
      address: '2801 Leavenworth St, San Francisco, CA',
      latitude: 37.8080,
      longitude: -122.4177,
    },
    {
      sequence: 2,
      customerName: 'Mission District Clinic',
      address: '2450 Mission St, San Francisco, CA',
      latitude: 37.7587,
      longitude: -122.4191,
    },
    {
      sequence: 3,
      customerName: 'Embarcadero Tech Labs',
      address: '201 Mission St, San Francisco, CA',
      latitude: 37.7915,
      longitude: -122.3972,
    },
    {
      sequence: 4,
      customerName: 'Blue Bottle Coffee',
      address: '315 Linden St, San Francisco, CA',
      latitude: 37.7763,
      longitude: -122.4231,
    },
  ]);

  const [comparison, setComparison] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchComparison = async () => {
    setLoading(true);
    setError('');

    try {
      const payload = {
        depotAddress: depot.address,
        depotLatitude: depot.latitude,
        depotLongitude: depot.longitude,
        roundTrip: true,
        stops: stops.map((s, idx) => ({
          sequence: idx + 1,
          address: s.address,
          latitude: s.latitude,
          longitude: s.longitude,
          customerName: s.customerName,
        })),
      };

      const result = await api.compareRoutes(payload);
      setComparison(result);
    } catch (err) {
      console.error('Comparison calculation failed:', err);
      setError(err.message || 'Failed to compare routes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComparison();
  }, []);

  // Prepare chart comparison data
  const chartData = comparison
    ? [
        {
          metric: 'Distance (km)',
          Baseline: comparison.baselineRoute?.totalDistanceKm || 0,
          Optimized: comparison.optimizedRoute?.totalDistanceKm || 0,
        },
        {
          metric: 'Duration (min)',
          Baseline: comparison.baselineRoute?.estimatedDurationMin || 0,
          Optimized: comparison.optimizedRoute?.estimatedDurationMin || 0,
        },
      ]
    : [];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <GitCompare className="w-7 h-7 text-blue-600" />
            Route Comparison & Benchmark
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Compare unoptimized baseline order against 2-Opt and exact Held-Karp permutations.
          </p>
        </div>

        <button
          onClick={fetchComparison}
          disabled={loading}
          className="inline-flex items-center space-x-1.5 px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-semibold hover:bg-blue-700 shadow-sm"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>{loading ? 'Re-benchmarking...' : 'Recalculate Benchmark'}</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm">
          {error}
        </div>
      )}

      {loading && !comparison ? (
        <LoadingSpinner text="Computing alternative route matrices and permutations..." />
      ) : (
        comparison && (
          <div className="space-y-6">
            {/* KPI Highlight Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Distance Reduction</span>
                <div className="flex items-baseline space-x-2 mt-2">
                  <span className="text-3xl font-extrabold text-emerald-600">
                    -{comparison.distanceDifferenceKm}
                  </span>
                  <span className="text-sm font-semibold text-slate-600">km saved</span>
                </div>
                <p className="text-xs text-slate-400 mt-1">
                  From {comparison.baselineRoute?.totalDistanceKm} km down to {comparison.optimizedRoute?.totalDistanceKm} km
                </p>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Travel Time Saved</span>
                <div className="flex items-baseline space-x-2 mt-2">
                  <span className="text-3xl font-extrabold text-blue-600">
                    -{comparison.durationDifferenceMin}
                  </span>
                  <span className="text-sm font-semibold text-slate-600">minutes saved</span>
                </div>
                <p className="text-xs text-slate-400 mt-1">
                  From ~{comparison.baselineRoute?.estimatedDurationMin} min to ~{comparison.optimizedRoute?.estimatedDurationMin} min
                </p>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Verified Improvement</span>
                <div className="flex items-baseline space-x-2 mt-2">
                  <span className="text-3xl font-extrabold text-purple-600">
                    +{comparison.savingsPercentage}%
                  </span>
                  <span className="text-sm font-semibold text-slate-600">efficiency gain</span>
                </div>
                <p className="text-xs text-slate-400 mt-1">
                  Formula: (Baseline − Optimized) / Baseline
                </p>
              </div>
            </div>

            {/* Side-by-Side Map & Comparison Chart */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              {/* Overlay Map (7 columns) */}
              <div className="lg:col-span-7 bg-white p-4 rounded-2xl border border-slate-200 shadow-sm space-y-3">
                <div className="flex items-center justify-between px-2">
                  <h3 className="font-bold text-sm text-slate-900">Map Trajectory Overlay</h3>
                  <div className="flex items-center space-x-3 text-xs font-semibold">
                    <span className="flex items-center gap-1.5 text-amber-600">
                      <span className="w-3 h-0.5 border-t-2 border-dashed border-amber-500 inline-block"></span>
                      Baseline Order
                    </span>
                    <span className="flex items-center gap-1.5 text-blue-600">
                      <span className="w-3 h-0.5 bg-blue-600 inline-block"></span>
                      Optimized Order
                    </span>
                  </div>
                </div>

                <RouteMap
                  depot={depot}
                  stops={comparison.optimizedRoute?.stops || stops}
                  routeGeometry={comparison.optimizedRoute?.routeGeometry}
                  baselineGeometry={comparison.baselineRoute?.routeGeometry}
                  height="450px"
                />
              </div>

              {/* Bar Chart (5 columns) */}
              <div className="lg:col-span-5 bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between">
                <div>
                  <h3 className="font-bold text-sm text-slate-900 mb-1">Direct Metric Comparison</h3>
                  <p className="text-xs text-slate-500 mb-4">Baseline sequence vs 2-Opt optimization</p>

                  <div className="h-64 w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                        <XAxis dataKey="metric" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                        <YAxis tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
                        <Tooltip
                          contentStyle={{
                            backgroundColor: '#0f172a',
                            borderRadius: '8px',
                            color: '#fff',
                            fontSize: '12px',
                          }}
                        />
                        <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                        <Bar dataKey="Baseline" fill="#f59e0b" radius={[4, 4, 0, 0]} />
                        <Bar dataKey="Optimized" fill="#2563eb" radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                <div className="mt-4 p-3.5 bg-blue-50/70 border border-blue-200/80 rounded-xl text-xs text-blue-900 space-y-1">
                  <div className="flex items-center gap-1.5 font-bold">
                    <Info className="w-4 h-4 text-blue-600 shrink-0" />
                    <span>Algorithmic Insight</span>
                  </div>
                  <p className="text-[11px] leading-relaxed text-blue-800">
                    {comparison.comparisonExplanation}
                  </p>
                </div>
              </div>
            </div>

            {/* Alternative Route Profiles Card */}
            {comparison.alternatives && comparison.alternatives.length > 0 && (
              <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
                <h3 className="font-bold text-slate-900 text-base">Tested Algorithm Profiles</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {comparison.alternatives.map((alt) => (
                    <div
                      key={alt.algorithmName}
                      className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-sm text-slate-900">{alt.title}</span>
                        <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 font-bold">
                          {alt.savingsPercentage}% Saved
                        </span>
                      </div>
                      <div className="text-xs text-slate-600 flex space-x-4">
                        <span>Distance: <strong>{alt.totalDistanceKm} km</strong></span>
                        <span>Duration: <strong>~{alt.estimatedDurationMin} min</strong></span>
                      </div>
                      <p className="text-[11px] text-slate-500 leading-relaxed pt-1 border-t border-slate-200">
                        {alt.description}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )
      )}
    </div>
  );
};
