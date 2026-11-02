import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import {
  Settings,
  User,
  MapPin,
  Building2,
  Mail,
  CheckCircle2,
  AlertCircle,
  Save,
  Moon,
  Sun,
  Shield,
  Crosshair,
} from 'lucide-react';

export const SettingsPage = () => {
  const { user, refreshUser } = useAuth();

  const [depotAddress, setDepotAddress] = useState(
    user?.defaultDepotAddress || 'Central Hub, 500 Howard St, San Francisco, CA'
  );
  const [depotLat, setDepotLat] = useState(user?.defaultDepotLat || 37.7885);
  const [depotLng, setDepotLng] = useState(user?.defaultDepotLng || -122.3995);
  const [distanceUnit, setDistanceUnit] = useState(user?.distanceUnit || 'km');
  const [theme, setTheme] = useState(user?.theme || 'light');

  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [geocoding, setGeocoding] = useState(false);
  const [detectingGps, setDetectingGps] = useState(false);

  const handleDetectLocation = () => {
    if (!navigator.geolocation) {
      setErrorMessage('Geolocation is not supported by your browser.');
      return;
    }

    setDetectingGps(true);
    setErrorMessage('');
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        try {
          const rev = await api.reverseGeocode(latitude, longitude);
          setDepotAddress(rev?.displayName || `Current Location (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`);
          setDepotLat(latitude);
          setDepotLng(longitude);
        } catch (err) {
          console.warn('Reverse geocode error:', err);
          setDepotAddress(`Current Location (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`);
          setDepotLat(latitude);
          setDepotLng(longitude);
        } finally {
          setDetectingGps(false);
        }
      },
      (err) => {
        setDetectingGps(false);
        setErrorMessage(
          err.code === 1
            ? 'Location permission was denied in your browser settings.'
            : `Could not retrieve location: ${err.message}`
        );
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  };

  const handleGeocode = async () => {
    if (!depotAddress) return;
    setGeocoding(true);
    setErrorMessage('');
    try {
      const res = await api.geocodeAddress(depotAddress);
      if (res.success && res.latitude && res.longitude) {
        setDepotLat(res.latitude);
        setDepotLng(res.longitude);
        setDepotAddress(res.formattedAddress || depotAddress);
      } else {
        setErrorMessage('Could not locate address coordinates.');
      }
    } catch (e) {
      setErrorMessage('Geocoding service unavailable.');
    } finally {
      setGeocoding(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSavedSuccess(false);
    setErrorMessage('');

    try {
      await api.updatePreferences({
        defaultDepotAddress: depotAddress,
        defaultDepotLat: parseFloat(depotLat),
        defaultDepotLng: parseFloat(depotLng),
        distanceUnit,
        theme,
      });
      await refreshUser();
      setSavedSuccess(true);
    } catch (err) {
      setErrorMessage(err.message || 'Failed to update preferences');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
          <Settings className="w-7 h-7 text-blue-600" />
          Settings & Preferences
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Manage your account profile, default distribution hub, and unit configurations.
        </p>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center space-x-2">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
          <span>Preferences successfully saved!</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-center space-x-2">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Profile Card */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <User className="w-5 h-5 text-slate-400" />
          User Profile
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div>
            <label className="block text-slate-500 font-semibold uppercase tracking-wider mb-1">Full Name</label>
            <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-lg font-bold text-slate-800">
              {user?.fullName || 'Not set'}
            </div>
          </div>

          <div>
            <label className="block text-slate-500 font-semibold uppercase tracking-wider mb-1">Email Address</label>
            <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-lg font-mono text-slate-800">
              {user?.email}
            </div>
          </div>

          <div>
            <label className="block text-slate-500 font-semibold uppercase tracking-wider mb-1">Company / Fleet</label>
            <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-lg font-bold text-slate-800">
              {user?.companyName || 'Independent Fleet'}
            </div>
          </div>

          <div>
            <label className="block text-slate-500 font-semibold uppercase tracking-wider mb-1">Authorization Role</label>
            <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-lg font-mono text-blue-600 font-semibold">
              {user?.role || 'ROLE_USER'}
            </div>
          </div>
        </div>
      </div>

      {/* Preferences Form */}
      <form onSubmit={handleSave} className="space-y-6">
        {/* Default Depot */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
            <MapPin className="w-5 h-5 text-blue-600" />
            Default Starting Distribution Depot
          </h2>
          <p className="text-xs text-slate-500">
            This depot is pre-populated whenever you create a new delivery route in the Route Planner.
          </p>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
              Depot Address
            </label>
            <div className="flex flex-col sm:flex-row gap-2">
              <input
                type="text"
                value={depotAddress}
                onChange={(e) => setDepotAddress(e.target.value)}
                className="flex-1 px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="Enter warehouse or central hub address"
              />
              <div className="flex gap-2 shrink-0">
                <button
                  type="button"
                  onClick={handleDetectLocation}
                  disabled={detectingGps}
                  className="inline-flex items-center space-x-1.5 px-3.5 py-2 bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-semibold rounded-lg border border-blue-200 transition-colors"
                >
                  <Crosshair className={`w-3.5 h-3.5 ${detectingGps ? 'animate-spin text-blue-600' : 'text-blue-600'}`} />
                  <span>{detectingGps ? 'Detecting...' : 'Detect My Location'}</span>
                </button>
                <button
                  type="button"
                  onClick={handleGeocode}
                  disabled={geocoding}
                  className="px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
                >
                  {geocoding ? 'Locating...' : 'Locate on Map'}
                </button>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 text-xs">
            <div>
              <label className="block text-slate-500 font-semibold mb-1">Latitude</label>
              <input
                type="number"
                step="any"
                value={depotLat}
                onChange={(e) => setDepotLat(e.target.value)}
                className="w-full px-3 py-1.5 border border-slate-300 rounded-lg font-mono text-slate-800"
              />
            </div>
            <div>
              <label className="block text-slate-500 font-semibold mb-1">Longitude</label>
              <input
                type="number"
                step="any"
                value={depotLng}
                onChange={(e) => setDepotLng(e.target.value)}
                className="w-full px-3 py-1.5 border border-slate-300 rounded-lg font-mono text-slate-800"
              />
            </div>
          </div>
        </div>

        {/* System Settings */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <h2 className="text-base font-bold text-slate-900">Routing & Unit Preferences</h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="block text-slate-700 font-semibold uppercase tracking-wider mb-1.5">
                Distance Unit
              </label>
              <select
                value={distanceUnit}
                onChange={(e) => setDistanceUnit(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 font-semibold"
              >
                <option value="km">Kilometers (km)</option>
                <option value="miles">Miles (mi)</option>
              </select>
            </div>

            <div>
              <label className="block text-slate-700 font-semibold uppercase tracking-wider mb-1.5">
                Application Theme
              </label>
              <select
                value={theme}
                onChange={(e) => setTheme(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-slate-800 font-semibold"
              >
                <option value="light">Light Mode (Default)</option>
                <option value="dark">Dark Slate</option>
              </select>
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button
            type="submit"
            disabled={saving}
            className="inline-flex items-center space-x-2 px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-colors"
          >
            <Save className="w-4 h-4" />
            <span>{saving ? 'Saving...' : 'Save Settings'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
