import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { RouteMap } from '../components/map/RouteMap';
import {
  MapPin,
  Plus,
  Trash2,
  ArrowUpDown,
  Navigation,
  Save,
  RotateCcw,
  Sparkles,
  Info,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Layers,
  Search,
} from 'lucide-react';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const RoutePlannerPage = () => {
  const { user } = useAuth();

  // Depot configuration
  const [depot, setDepot] = useState({
    address: user?.defaultDepotAddress || 'Central Hub, 500 Howard St, San Francisco, CA',
    latitude: user?.defaultDepotLat || 37.7885,
    longitude: user?.defaultDepotLng || -122.3995,
  });

  const [roundTrip, setRoundTrip] = useState(true);
  const [endDepot, setEndDepot] = useState({
    address: '',
    latitude: null,
    longitude: null,
  });

  // Stops list
  const [stops, setStops] = useState([
    {
      id: 'init-1',
      customerName: 'Embarcadero Tech Labs',
      address: '201 Mission St, San Francisco, CA',
      latitude: 37.7915,
      longitude: -122.3972,
    },
    {
      id: 'init-2',
      customerName: 'Fishermans Wharf Gift Shop',
      address: '2801 Leavenworth St, San Francisco, CA',
      latitude: 37.8080,
      longitude: -122.4177,
    },
    {
      id: 'init-3',
      customerName: 'Mission District Medical Clinic',
      address: '2450 Mission St, San Francisco, CA',
      latitude: 37.7587,
      longitude: -122.4191,
    },
  ]);

  // Algorithm selection
  const [algorithm, setAlgorithm] = useState('HEURISTIC_2OPT');

  // Calculation Results
  const [routeResult, setRouteResult] = useState(null);
  const [optimizing, setOptimizing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Location search state for adding stops
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchingLocation, setSearchingLocation] = useState(false);

  // Pending deliveries modal/dropdown
  const [pendingOrders, setPendingOrders] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);

  // Save Modal
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [routeName, setRouteName] = useState('Metro Delivery Tour');

  useEffect(() => {
    // Load unassigned pending orders for quick import
    api.getPendingDeliveries()
      .then((data) => setPendingOrders(data || []))
      .catch((err) => console.warn('Could not load pending orders:', err));
  }, []);

  // Debounced location search
  useEffect(() => {
    if (!searchQuery || searchQuery.trim().length < 2) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setSearchingLocation(true);
      try {
        const results = await api.searchLocations(searchQuery);
        setSearchResults(results || []);
      } catch (err) {
        console.error('Search error:', err);
      } finally {
        setSearchingLocation(false);
      }
    }, 350);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleAddStopFromSearch = (loc) => {
    const newStop = {
      id: `stop-${Date.now()}`,
      customerName: loc.displayName.split(',')[0],
      address: loc.displayName,
      latitude: loc.latitude,
      longitude: loc.longitude,
    };
    setStops((prev) => [...prev, newStop]);
    setSearchQuery('');
    setSearchResults([]);
  };

  const handleImportPendingOrder = (order) => {
    const newStop = {
      id: `order-${order.id}`,
      deliveryOrderId: order.id,
      customerName: order.customerName,
      address: order.address,
      latitude: order.latitude,
      longitude: order.longitude,
      contactPhone: order.contactPhone,
    };
    setStops((prev) => [...prev, newStop]);
    setPendingOrders((prev) => prev.filter((p) => p.id !== order.id));
  };

  const handleRemoveStop = (index) => {
    setStops((prev) => prev.filter((_, i) => i !== index));
    setRouteResult(null);
  };

  const handleMoveStop = (index, direction) => {
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= stops.length) return;
    const newStops = [...stops];
    const temp = newStops[index];
    newStops[index] = newStops[targetIndex];
    newStops[targetIndex] = temp;
    setStops(newStops);
    setRouteResult(null);
  };

  const handleOptimize = async () => {
    if (stops.length === 0) {
      setErrorMessage('Please add at least 1 delivery stop.');
      return;
    }
    setErrorMessage('');
    setOptimizing(true);
    setSaveSuccess(false);

    try {
      const payload = {
        name: routeName,
        depotAddress: depot.address,
        depotLatitude: depot.latitude,
        depotLongitude: depot.longitude,
        endDepotAddress: !roundTrip && endDepot.latitude ? endDepot.address : null,
        endDepotLatitude: !roundTrip && endDepot.latitude ? endDepot.latitude : null,
        endDepotLongitude: !roundTrip && endDepot.latitude ? endDepot.longitude : null,
        roundTrip,
        algorithm,
        stops: stops.map((s, idx) => ({
          deliveryOrderId: s.deliveryOrderId || null,
          sequence: idx + 1,
          address: s.address,
          latitude: s.latitude,
          longitude: s.longitude,
          customerName: s.customerName,
          contactPhone: s.contactPhone,
        })),
      };

      const result = await api.optimizeRoute(payload);
      setRouteResult(result);
      if (result.stops && result.stops.length > 0) {
        setStops(result.stops);
      }
    } catch (err) {
      console.error('Optimization error:', err);
      setErrorMessage(err.message || 'Route calculation failed');
    } finally {
      setOptimizing(false);
    }
  };

  const handleSaveRoute = async () => {
    if (!routeResult) return;
    setSaving(true);
    setErrorMessage('');

    try {
      const savePayload = {
        name: routeName,
        depotAddress: routeResult.depotAddress,
        depotLatitude: routeResult.depotLatitude,
        depotLongitude: routeResult.depotLongitude,
        endDepotAddress: routeResult.endDepotAddress,
        endDepotLatitude: routeResult.endDepotLatitude,
        endDepotLongitude: routeResult.endDepotLongitude,
        roundTrip: routeResult.roundTrip,
        optimizationMethod: routeResult.optimizationMethod,
        totalDistanceKm: routeResult.totalDistanceKm,
        estimatedDurationMin: routeResult.estimatedDurationMin,
        routeGeometry: routeResult.routeGeometry,
        isEstimated: routeResult.isEstimated,
        baselineDistanceKm: routeResult.baselineDistanceKm,
        baselineDurationMin: routeResult.baselineDurationMin,
        distanceSavedKm: routeResult.distanceSavedKm,
        savingsPercentage: routeResult.savingsPercentage,
        stops: routeResult.stops,
      };

      await api.saveRoute(savePayload);
      setSaveSuccess(true);
      setShowSaveModal(false);
    } catch (err) {
      setErrorMessage(err.message || 'Failed to save route to database');
    } finally {
      setSaving(false);
    }
  };

  const handleReset = () => {
    setRouteResult(null);
    setSaveSuccess(false);
    setErrorMessage('');
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Page Title & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Multi-Stop Route Planner
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Build optimal routes with road distances, sequence optimization, and interactive maps.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={handleReset}
            className="inline-flex items-center space-x-1.5 px-3 py-2 bg-white text-slate-700 border border-slate-300 rounded-xl text-xs font-semibold hover:bg-slate-50 transition-colors shadow-sm"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Reset</span>
          </button>

          <button
            onClick={handleOptimize}
            disabled={optimizing || stops.length === 0}
            className="inline-flex items-center space-x-2 px-5 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 disabled:opacity-50 transition-all shadow-md shadow-blue-500/20"
          >
            <Sparkles className="w-4 h-4" />
            <span>{optimizing ? 'Optimizing Route...' : 'Calculate Optimal Route'}</span>
          </button>
        </div>
      </div>

      {/* Success / Error Alerts */}
      {saveSuccess && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            <span>Route saved successfully to Route History!</span>
          </div>
          <button
            onClick={() => setSaveSuccess(false)}
            className="text-emerald-700 hover:text-emerald-900 text-xs font-semibold"
          >
            Dismiss
          </button>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-center space-x-2">
          <AlertTriangle className="w-5 h-5 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Main Grid: Left Controls (1 col) + Right Map & Metrics (2 cols) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Stops & Configurations (5 columns) */}
        <div className="lg:col-span-5 space-y-5">
          {/* Depot Configuration Card */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                <MapPin className="w-4 h-4 text-slate-900" />
                Distribution Depot (Origin)
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-100 font-mono text-slate-600">Start</span>
            </div>

            <div className="text-sm">
              <input
                type="text"
                value={depot.address}
                onChange={(e) => setDepot({ ...depot, address: e.target.value })}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-800 font-medium"
                placeholder="Enter starting depot address"
              />
              <div className="text-[11px] text-slate-400 mt-1 flex gap-2">
                <span>Lat: {depot.latitude?.toFixed(4)}</span>
                <span>Lng: {depot.longitude?.toFixed(4)}</span>
              </div>
            </div>

            <div className="flex items-center justify-between pt-2 border-t border-slate-100">
              <label className="flex items-center space-x-2 text-xs font-medium text-slate-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={roundTrip}
                  onChange={(e) => setRoundTrip(e.target.checked)}
                  className="rounded border-slate-300 text-blue-600 focus:ring-blue-500 w-4 h-4"
                />
                <span>Round-trip (Return to Depot)</span>
              </label>

              <span className="text-[11px] text-slate-500">
                {roundTrip ? 'Closed loop' : 'Open destination'}
              </span>
            </div>
          </div>

          {/* Algorithm Choice Card */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
              <Layers className="w-4 h-4 text-blue-600" />
              Optimization Engine Method
            </span>

            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => setAlgorithm('HEURISTIC_2OPT')}
                className={`p-3 rounded-xl border text-left transition-all ${
                  algorithm === 'HEURISTIC_2OPT'
                    ? 'border-blue-600 bg-blue-50/50 text-blue-900 font-bold shadow-sm'
                    : 'border-slate-200 hover:border-slate-300 text-slate-600 font-medium'
                }`}
              >
                <div className="font-semibold text-slate-900">2-Opt Heuristic</div>
                <div className="text-[10px] text-slate-500 mt-1">Fast & scalable for 50+ stops. Polynomial time.</div>
              </button>

              <button
                type="button"
                onClick={() => setAlgorithm('EXACT_HELD_KARP')}
                className={`p-3 rounded-xl border text-left transition-all ${
                  algorithm === 'EXACT_HELD_KARP'
                    ? 'border-blue-600 bg-blue-50/50 text-blue-900 font-bold shadow-sm'
                    : 'border-slate-200 hover:border-slate-300 text-slate-600 font-medium'
                }`}
              >
                <div className="font-semibold text-slate-900">Held-Karp Exact DP</div>
                <div className="text-[10px] text-slate-500 mt-1">Proven global optimum. Limit ≤ 12 stops.</div>
              </button>
            </div>
          </div>

          {/* Add Delivery Stops Section */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-700">
                Delivery Stops ({stops.length})
              </span>
              {pendingOrders.length > 0 && (
                <span className="text-[11px] text-blue-600 font-medium">
                  {pendingOrders.length} pending orders ready
                </span>
              )}
            </div>

            {/* Address Search Autocomplete Input */}
            <div className="relative">
              <div className="relative flex items-center">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search address or landmark to add stop..."
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
                {searchingLocation && (
                  <div className="absolute right-3">
                    <div className="w-3.5 h-3.5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
                  </div>
                )}
              </div>

              {/* Suggestions Dropdown */}
              {searchResults.length > 0 && (
                <div className="absolute top-full mt-1.5 left-0 right-0 bg-white border border-slate-200 rounded-xl shadow-lg z-30 max-h-56 overflow-y-auto divide-y divide-slate-100">
                  {searchResults.map((loc) => (
                    <button
                      key={loc.placeId || loc.displayName}
                      onClick={() => handleAddStopFromSearch(loc)}
                      className="w-full p-2.5 text-left text-xs hover:bg-blue-50 transition-colors flex items-start space-x-2"
                    >
                      <MapPin className="w-3.5 h-3.5 text-blue-600 shrink-0 mt-0.5" />
                      <div>
                        <p className="font-semibold text-slate-800">{loc.displayName.split(',')[0]}</p>
                        <p className="text-[11px] text-slate-500 truncate">{loc.displayName}</p>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Quick Import from Pending Deliveries */}
            {pendingOrders.length > 0 && (
              <div className="pt-2 border-t border-slate-100">
                <p className="text-[11px] font-semibold text-slate-500 mb-2">Import from Pending Pool:</p>
                <div className="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                  {pendingOrders.map((order) => (
                    <button
                      key={order.id}
                      onClick={() => handleImportPendingOrder(order)}
                      className="inline-flex items-center space-x-1 px-2.5 py-1 bg-slate-100 hover:bg-blue-100 hover:text-blue-700 text-slate-700 rounded-lg text-[11px] transition-colors"
                      title={order.address}
                    >
                      <Plus className="w-3 h-3" />
                      <span className="font-medium truncate max-w-[120px]">{order.customerName}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Ordered Stop List */}
            <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
              {stops.map((stop, index) => (
                <div
                  key={stop.id || index}
                  className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200/80 hover:border-slate-300 transition-colors text-xs"
                >
                  <div className="flex items-center space-x-2.5 min-w-0">
                    <span className="w-5 h-5 rounded-full bg-blue-600 text-white font-bold text-[10px] flex items-center justify-center shrink-0">
                      {index + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="font-bold text-slate-800 truncate">{stop.customerName || `Stop ${index + 1}`}</p>
                      <p className="text-[11px] text-slate-500 truncate">{stop.address}</p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-1 shrink-0 ml-2">
                    <button
                      onClick={() => handleMoveStop(index, -1)}
                      disabled={index === 0}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30 rounded"
                      title="Move Up"
                    >
                      &uarr;
                    </button>
                    <button
                      onClick={() => handleMoveStop(index, 1)}
                      disabled={index === stops.length - 1}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30 rounded"
                      title="Move Down"
                    >
                      &darr;
                    </button>
                    <button
                      onClick={() => handleRemoveStop(index)}
                      className="p-1 text-slate-400 hover:text-red-600 rounded ml-1"
                      title="Remove Stop"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Live Map and Calculation Results (7 columns) */}
        <div className="lg:col-span-7 space-y-5">
          {/* Optimization Summary Bar (when computed) */}
          {routeResult && (
            <div className="bg-gradient-to-r from-blue-900 to-slate-900 p-5 rounded-2xl text-white shadow-md flex flex-wrap items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="flex items-center space-x-2">
                  <span className="text-xs px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 font-mono text-[10px]">
                    {routeResult.optimizationMethod}
                  </span>
                  {routeResult.isEstimated && (
                    <span className="text-[10px] px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-400/30">
                      Straight-line estimate fallback
                    </span>
                  )}
                </div>
                <div className="flex items-baseline space-x-4 mt-2">
                  <div>
                    <span className="text-2xl font-black">{routeResult.totalDistanceKm}</span>
                    <span className="text-xs text-slate-300 font-medium ml-1">km</span>
                  </div>
                  <span className="text-slate-600">•</span>
                  <div>
                    <span className="text-2xl font-black">~{routeResult.estimatedDurationMin}</span>
                    <span className="text-xs text-slate-300 font-medium ml-1">min</span>
                  </div>
                  {routeResult.savingsPercentage > 0 && (
                    <>
                      <span className="text-slate-600">•</span>
                      <div className="text-emerald-400 font-bold text-sm flex items-center">
                        <span className="bg-emerald-950 px-2 py-0.5 rounded border border-emerald-800/40">
                          -{routeResult.savingsPercentage}% distance saved
                        </span>
                      </div>
                    </>
                  )}
                </div>
              </div>

              <button
                onClick={() => setShowSaveModal(true)}
                className="inline-flex items-center space-x-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-sm transition-colors"
              >
                <Save className="w-4 h-4" />
                <span>Save to History</span>
              </button>
            </div>
          )}

          {/* Interactive Map */}
          <div className="bg-white p-3 rounded-2xl border border-slate-200 shadow-sm">
            <RouteMap
              depot={depot}
              endDepot={!roundTrip && endDepot.latitude ? endDepot : null}
              stops={stops}
              routeGeometry={routeResult?.routeGeometry}
              height="530px"
            />
          </div>
        </div>
      </div>

      {/* Save Route Dialog Modal */}
      {showSaveModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-slate-900">Save Optimized Route</h3>
            <p className="text-xs text-slate-500">
              Save this calculated route into permanent route history and associate assigned delivery orders.
            </p>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                Route Name
              </label>
              <input
                type="text"
                value={routeName}
                onChange={(e) => setRouteName(e.target.value)}
                placeholder="e.g., Morning Downtown Express"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>

            <div className="text-xs text-slate-600 space-y-1 bg-slate-50 p-3 rounded-xl">
              <div className="flex justify-between">
                <span>Total Distance:</span>
                <span className="font-bold">{routeResult?.totalDistanceKm} km</span>
              </div>
              <div className="flex justify-between">
                <span>Estimated Duration:</span>
                <span className="font-bold">~{routeResult?.estimatedDurationMin} min</span>
              </div>
              <div className="flex justify-between">
                <span>Stop Count:</span>
                <span className="font-bold">{routeResult?.stops?.length} stops</span>
              </div>
            </div>

            <div className="flex justify-end space-x-2 pt-2">
              <button
                type="button"
                onClick={() => setShowSaveModal(false)}
                className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSaveRoute}
                disabled={saving}
                className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold shadow-sm transition-colors"
              >
                {saving ? 'Saving...' : 'Confirm Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
