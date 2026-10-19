import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useNavigate } from 'react-router-dom';
import {
  History,
  Navigation,
  Trash2,
  ExternalLink,
  Calendar,
  Clock,
  TrendingDown,
  Layers,
  MapPin,
  Search,
} from 'lucide-react';
import { RouteMap } from '../components/map/RouteMap';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const RouteHistoryPage = () => {
  const navigate = useNavigate();
  const [routes, setRoutes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedRoute, setSelectedRoute] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchRoutes = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 8,
        sort: 'createdAt,desc',
      };
      if (search.trim()) params.search = search.trim();

      const data = await api.getRoutes(params);
      setRoutes(data.content || []);
      setTotalPages(data.totalPages || 1);
      if (data.content && data.content.length > 0 && !selectedRoute) {
        setSelectedRoute(data.content[0]);
      }
    } catch (err) {
      console.error('Failed to load route history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoutes();
  }, [page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchRoutes();
  };

  const handleDeleteRoute = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this saved route?')) return;
    try {
      await api.deleteRoute(id);
      if (selectedRoute?.id === id) {
        setSelectedRoute(null);
      }
      fetchRoutes();
    } catch (err) {
      alert(err.message || 'Failed to delete route');
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <History className="w-7 h-7 text-blue-600" />
            Route History & Logs
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Browse and inspect previous multi-stop dispatches and verified distance metrics.
          </p>
        </div>

        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3 pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search saved routes..."
            className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600 shadow-sm"
          />
        </form>
      </div>

      {loading ? (
        <div className="py-20">
          <LoadingSpinner text="Retrieving saved route plans..." />
        </div>
      ) : routes.length === 0 ? (
        <div className="bg-white p-12 rounded-2xl border border-slate-200 text-center text-slate-400">
          <Navigation className="w-12 h-12 mx-auto mb-3 opacity-30 text-blue-600" />
          <p className="font-semibold text-slate-800 text-base">No saved routes yet</p>
          <p className="text-xs text-slate-500 mt-1">
            Use the Route Planner to optimize your delivery orders and save routes to history.
          </p>
          <button
            onClick={() => navigate('/planner')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-700 transition-colors"
          >
            Go to Route Planner
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Route Cards List (5 columns) */}
          <div className="lg:col-span-5 space-y-3">
            {routes.map((route) => {
              const isSelected = selectedRoute?.id === route.id;
              return (
                <div
                  key={route.id}
                  onClick={() => setSelectedRoute(route)}
                  className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-blue-50/60 border-blue-500 shadow-sm'
                      : 'bg-white border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <h3 className="font-bold text-sm text-slate-900">{route.name}</h3>
                      <p className="text-xs text-slate-500 truncate mt-0.5">{route.depotAddress}</p>
                    </div>

                    <button
                      onClick={(e) => handleDeleteRoute(route.id, e)}
                      className="text-slate-400 hover:text-red-600 p-1 rounded"
                      title="Delete Route"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <div className="mt-3 flex items-center justify-between text-xs pt-3 border-t border-slate-100">
                    <div className="flex items-center space-x-3 text-slate-600">
                      <span><strong>{route.totalDistanceKm}</strong> km</span>
                      <span>•</span>
                      <span><strong>~{route.estimatedDurationMin}</strong> min</span>
                      <span>•</span>
                      <span>{route.stops?.length ?? 0} stops</span>
                    </div>

                    {route.savingsPercentage > 0 && (
                      <span className="font-bold text-emerald-600 text-[11px]">
                        -{route.savingsPercentage}%
                      </span>
                    )}
                  </div>
                </div>
              );
            })}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between text-xs text-slate-500 pt-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-3 py-1 bg-white border border-slate-200 rounded-lg disabled:opacity-40"
                >
                  Previous
                </button>
                <span>Page {page + 1} of {totalPages}</span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-3 py-1 bg-white border border-slate-200 rounded-lg disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            )}
          </div>

          {/* Detailed Inspector & Map (7 columns) */}
          <div className="lg:col-span-7 space-y-4">
            {selectedRoute ? (
              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
                <div className="flex items-start justify-between border-b border-slate-100 pb-3">
                  <div>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                      {selectedRoute.optimizationMethod}
                    </span>
                    <h2 className="text-lg font-bold text-slate-900 mt-1">{selectedRoute.name}</h2>
                    <p className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
                      <Calendar className="w-3.5 h-3.5" />
                      Saved on {new Date(selectedRoute.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="text-right">
                    <div className="text-xl font-extrabold text-blue-600">{selectedRoute.totalDistanceKm} km</div>
                    <div className="text-xs text-slate-500">~{selectedRoute.estimatedDurationMin} minutes</div>
                  </div>
                </div>

                {/* Map Display */}
                <RouteMap
                  depot={{
                    address: selectedRoute.depotAddress,
                    latitude: selectedRoute.depotLatitude,
                    longitude: selectedRoute.depotLongitude,
                  }}
                  stops={selectedRoute.stops || []}
                  routeGeometry={selectedRoute.routeGeometry}
                  height="340px"
                />

                {/* Stop Sequence Breakdown */}
                <div className="space-y-2 pt-2">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-600">
                    Ordered Itinerary ({selectedRoute.stops?.length ?? 0} stops)
                  </h4>
                  <div className="max-h-48 overflow-y-auto divide-y divide-slate-100 text-xs">
                    {selectedRoute.stops?.map((stop, i) => (
                      <div key={stop.id || i} className="py-2 flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <span className="w-4 h-4 rounded-full bg-blue-100 text-blue-700 text-[10px] font-bold flex items-center justify-center">
                            {stop.sequence}
                          </span>
                          <span className="font-semibold text-slate-800 truncate max-w-xs">{stop.customerName || stop.address}</span>
                        </div>
                        <div className="text-slate-500 text-[11px]">
                          {stop.distanceToNextKm != null ? `${stop.distanceToNextKm} km` : ''}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="bg-white p-12 rounded-2xl border border-slate-200 text-center text-slate-400">
                Select a route from the list to inspect details.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
