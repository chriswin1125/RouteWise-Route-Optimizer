import React, { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';

// Fix for default Leaflet icon assets
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Helper component to auto-center & zoom the map to fit all markers and lines
function AutoFitBounds({ points }) {
  const map = useMap();

  useEffect(() => {
    if (!points || points.length === 0) return;
    try {
      const bounds = L.latLngBounds(points);
      if (bounds.isValid()) {
        map.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 });
      }
    } catch (e) {
      console.warn('Could not fit map bounds:', e);
    }
  }, [points, map]);

  return null;
}

// Create custom colored HTML markers with numbers
const createNumberedIcon = (number, color = '#2563eb') => {
  return L.divIcon({
    className: 'custom-map-icon',
    html: `
      <div style="
        background-color: ${color};
        color: #ffffff;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 12px;
        border: 2px solid #ffffff;
        box-shadow: 0 3px 6px rgba(0,0,0,0.3);
      ">
        ${number}
      </div>
    `,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
    popupAnchor: [0, -14],
  });
};

const createDepotIcon = (isEnd = false) => {
  const color = isEnd ? '#dc2626' : '#0f172a';
  const label = isEnd ? 'END' : 'HUB';
  return L.divIcon({
    className: 'custom-depot-icon',
    html: `
      <div style="
        background-color: ${color};
        color: #38bdf8;
        padding: 2px 6px;
        height: 26px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 800;
        font-size: 11px;
        letter-spacing: 0.5px;
        border: 2px solid #ffffff;
        box-shadow: 0 3px 6px rgba(0,0,0,0.35);
      ">
        ${label}
      </div>
    `,
    iconSize: [40, 26],
    iconAnchor: [20, 13],
    popupAnchor: [0, -13],
  });
};

export const RouteMap = ({
  depot,
  endDepot,
  stops = [],
  routeGeometry = null,
  baselineGeometry = null,
  height = '500px',
  interactive = true,
}) => {
  // Center default: San Francisco
  const defaultCenter = [37.7749, -122.4194];
  const center = depot?.latitude && depot?.longitude
    ? [depot.latitude, depot.longitude]
    : defaultCenter;

  // Collect all points for fitBounds
  const allFitPoints = [];
  if (depot?.latitude && depot?.longitude) {
    allFitPoints.push([depot.latitude, depot.longitude]);
  }
  if (endDepot?.latitude && endDepot?.longitude) {
    allFitPoints.push([endDepot.latitude, endDepot.longitude]);
  }
  stops.forEach((s) => {
    if (s.latitude && s.longitude) {
      allFitPoints.push([s.latitude, s.longitude]);
    }
  });

  // Parse geometries if string
  let parsedPolyline = [];
  if (routeGeometry) {
    try {
      parsedPolyline = typeof routeGeometry === 'string' ? JSON.parse(routeGeometry) : routeGeometry;
    } catch (e) {
      console.warn('Failed to parse routeGeometry:', e);
    }
  }

  let parsedBaselinePolyline = [];
  if (baselineGeometry) {
    try {
      parsedBaselinePolyline = typeof baselineGeometry === 'string' ? JSON.parse(baselineGeometry) : baselineGeometry;
    } catch (e) {
      console.warn('Failed to parse baselineGeometry:', e);
    }
  }

  // Include polyline points in bounds
  if (parsedPolyline.length > 0) {
    parsedPolyline.forEach((pt) => allFitPoints.push(pt));
  }

  return (
    <div style={{ height }} className="w-full relative rounded-xl overflow-hidden shadow-inner border border-slate-200">
      <MapContainer
        center={center}
        zoom={13}
        scrollWheelZoom={interactive}
        dragging={interactive}
        className="w-full h-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <AutoFitBounds points={allFitPoints} />

        {/* Baseline (Original) Polyline - Dashed Gray/Amber */}
        {parsedBaselinePolyline.length > 0 && (
          <Polyline
            positions={parsedBaselinePolyline}
            pathOptions={{ color: '#f59e0b', weight: 3, dashArray: '6, 8', opacity: 0.8 }}
          />
        )}

        {/* Optimized Road Polyline - Solid Blue */}
        {parsedPolyline.length > 0 && (
          <Polyline
            positions={parsedPolyline}
            pathOptions={{ color: '#2563eb', weight: 4.5, opacity: 0.9 }}
          />
        )}

        {/* Starting Depot Marker */}
        {depot?.latitude && depot?.longitude && (
          <Marker position={[depot.latitude, depot.longitude]} icon={createDepotIcon(false)}>
            <Popup>
              <div className="text-sm font-sans">
                <span className="font-bold text-slate-800">Depot Hub (Start)</span>
                <p className="text-xs text-slate-600 mt-1">{depot.address || 'Starting Depot'}</p>
              </div>
            </Popup>
          </Marker>
        )}

        {/* Fixed End Depot Marker (if distinct) */}
        {endDepot?.latitude && endDepot?.longitude && (
          <Marker position={[endDepot.latitude, endDepot.longitude]} icon={createDepotIcon(true)}>
            <Popup>
              <div className="text-sm font-sans">
                <span className="font-bold text-red-600">Ending Depot</span>
                <p className="text-xs text-slate-600 mt-1">{endDepot.address || 'Final Destination'}</p>
              </div>
            </Popup>
          </Marker>
        )}

        {/* Numbered Delivery Stops */}
        {stops.map((stop, index) => {
          if (!stop.latitude || !stop.longitude) return null;
          const stopNumber = stop.sequence || index + 1;
          return (
            <Marker
              key={stop.id || `stop-${index}`}
              position={[stop.latitude, stop.longitude]}
              icon={createNumberedIcon(stopNumber, '#2563eb')}
            >
              <Popup>
                <div className="text-sm font-sans space-y-1">
                  <div className="flex items-center space-x-1.5 font-bold text-blue-600">
                    <span className="bg-blue-100 text-blue-800 px-1.5 py-0.5 rounded text-xs">Stop #{stopNumber}</span>
                    <span>{stop.customerName || 'Customer'}</span>
                  </div>
                  <p className="text-xs text-slate-600">{stop.address}</p>
                  {stop.distanceToNextKm != null && (
                    <div className="text-xs text-slate-500 pt-1 border-t border-slate-100 flex justify-between">
                      <span>Leg: {stop.distanceToNextKm} km</span>
                      <span>~{stop.durationToNextMin} min</span>
                    </div>
                  )}
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
};
