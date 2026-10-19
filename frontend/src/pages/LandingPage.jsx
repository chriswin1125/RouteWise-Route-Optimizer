import React from 'react';
import { Link } from 'react-router-dom';
import {
  Navigation,
  ArrowRight,
  TrendingDown,
  Cpu,
  Layers,
  MapPin,
  Clock,
  ShieldCheck,
  CheckCircle2,
  Compass,
} from 'lucide-react';
import { RouteMap } from '../components/map/RouteMap';
import { useAuth } from '../context/AuthContext';

export const LandingPage = () => {
  const { user } = useAuth();

  // Preview data in San Francisco
  const sampleDepot = {
    address: 'Central Distribution Hub, 500 Howard St, San Francisco, CA',
    latitude: 37.7885,
    longitude: -122.3995,
  };

  const sampleStops = [
    { sequence: 1, customerName: 'Embarcadero Tech Labs', address: '201 Mission St', latitude: 37.7915, longitude: -122.3972, distanceToNextKm: 2.3, durationToNextMin: 7 },
    { sequence: 2, customerName: 'Fishermans Wharf Gift Shop', address: '2801 Leavenworth St', latitude: 37.8080, longitude: -122.4177, distanceToNextKm: 6.8, durationToNextMin: 16 },
    { sequence: 3, customerName: 'Blue Bottle Coffee', address: '315 Linden St', latitude: 37.7763, longitude: -122.4231, distanceToNextKm: 4.2, durationToNextMin: 11 },
    { sequence: 4, customerName: 'Mission District Clinic', address: '2450 Mission St', latitude: 37.7587, longitude: -122.4191, distanceToNextKm: 4.5, durationToNextMin: 12 },
  ];

  const sampleGeometry = [
    [37.7885, -122.3995],
    [37.7915, -122.3972],
    [37.8080, -122.4177],
    [37.7763, -122.4231],
    [37.7587, -122.4191],
    [37.7885, -122.3995],
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Top Navigation */}
      <header className="bg-white/80 backdrop-blur-md sticky top-0 z-30 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-md shadow-blue-500/20">
              <Navigation className="w-5 h-5 transform -rotate-45" />
            </div>
            <span className="font-extrabold text-xl tracking-tight text-slate-900">RouteWise</span>
          </div>

          <div className="flex items-center space-x-4">
            {user ? (
              <Link
                to="/dashboard"
                className="inline-flex items-center space-x-1.5 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
              >
                <span>Go to Dashboard</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-sm font-semibold text-slate-600 hover:text-slate-900 px-3 py-2 transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="inline-flex items-center space-x-1.5 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
                >
                  <span>Start Free</span>
                  <ArrowRight className="w-4 h-4" />
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="pt-16 pb-20 px-6 max-w-7xl mx-auto w-full">
        <div className="text-center max-w-3xl mx-auto mb-12">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-blue-50 text-blue-700 border border-blue-200 text-xs font-semibold uppercase tracking-wider mb-6">
            <Cpu className="w-3.5 h-3.5" />
            <span>Algorithm-Powered Logistics Routing</span>
          </div>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-tight">
            Intelligent Route Optimization for <span className="text-blue-600">Modern Fleets</span>
          </h1>
          <p className="mt-6 text-lg text-slate-600 leading-relaxed">
            Eliminate criss-crossing stops, cut fuel expenditure by up to 35%, and dispatch multi-stop delivery routes in seconds using advanced 2-Opt local search, Held-Karp exact dynamic programming, and real OpenStreetMap road routing.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to={user ? "/planner" : "/login"}
              className="w-full sm:w-auto inline-flex items-center justify-center space-x-2 px-7 py-3.5 bg-blue-600 text-white rounded-xl text-base font-semibold hover:bg-blue-700 transition-all shadow-md shadow-blue-600/20"
            >
              <Compass className="w-5 h-5" />
              <span>Start Planning Routes</span>
            </Link>
            <Link
              to="/visualizer"
              className="w-full sm:w-auto inline-flex items-center justify-center space-x-2 px-7 py-3.5 bg-white text-slate-700 border border-slate-300 rounded-xl text-base font-semibold hover:bg-slate-50 transition-colors shadow-sm"
            >
              <Cpu className="w-5 h-5 text-blue-600" />
              <span>Inspect Algorithms Trace</span>
            </Link>
          </div>
        </div>

        {/* Live Map Preview Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xl overflow-hidden mt-8">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between pb-3 px-2 border-b border-slate-100 gap-2 mb-3">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-700">Live Optimization Preview</span>
              <span className="text-xs text-slate-400">• San Francisco Metropolitan Fleet</span>
            </div>
            <div className="flex items-center space-x-4 text-xs font-medium text-slate-600">
              <span className="flex items-center text-emerald-600 font-bold">
                <TrendingDown className="w-3.5 h-3.5 mr-1" />
                30.2% Distance Saved
              </span>
              <span>17.8 km Total</span>
              <span>~46 min Duration</span>
            </div>
          </div>
          <RouteMap
            depot={sampleDepot}
            stops={sampleStops}
            routeGeometry={sampleGeometry}
            height="440px"
          />
        </div>
      </section>

      {/* Feature Cards Grid */}
      <section className="py-16 bg-white border-y border-slate-200">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-2xl mx-auto mb-14">
            <h2 className="text-3xl font-bold text-slate-900">Engineering Excellence & Mathematical Rigor</h2>
            <p className="mt-3 text-slate-600">Built from first principles with real graph theory algorithms, scalable Spring Boot 3 architecture, and PostgreSQL persistence.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-slate-50 p-6 rounded-xl border border-slate-200/80 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-4">
                <Layers className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 mb-2">2-Opt Multi-Stop Optimization</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Initializes routes using greedy Nearest-Neighbor and refines with iterative 2-exchange edge reversals, systematically untangling crossing paths.
              </p>
            </div>

            <div className="bg-slate-50 p-6 rounded-xl border border-slate-200/80 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center mb-4">
                <Cpu className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 mb-2">Held-Karp Exact TSP</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Solves small delivery tours (≤ 12 stops) to mathematically proven global optimality using bitmask dynamic programming in O(N²·2ᴺ) time.
              </p>
            </div>

            <div className="bg-slate-50 p-6 rounded-xl border border-slate-200/80 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-lg bg-sky-100 text-sky-600 flex items-center justify-center mb-4">
                <Compass className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 mb-2">Dijkstra & A* Shortest Paths</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Min-heap priority queue Dijkstra and goal-directed A* with Euclidean/Haversine heuristic, complete with step-by-step visual trace tools.
              </p>
            </div>

            <div className="bg-slate-50 p-6 rounded-xl border border-slate-200/80 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center mb-4">
                <MapPin className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 mb-2">Real Road Network Routing</h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                Integrated with OSRM and OpenStreetMap for accurate driving distances and realistic geometry, with graceful straight-line fallback.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Technology & Value Props */}
      <section className="py-16 px-6 max-w-7xl mx-auto w-full">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">
              Designed for Fleet Operators, Logistics Managers, and Engineers
            </h2>
            <p className="mt-4 text-slate-600 leading-relaxed">
              RouteWise provides full data isolation per account, persistent route history, order dispatch workflows, and side-by-side alternative route comparisons with visual analytics.
            </p>

            <div className="mt-6 space-y-3.5">
              {[
                'Measure verified distance savings against baseline sequence order',
                'Assign deliveries seamlessly from pending pool to optimized routes',
                'Interactive Leaflet maps with custom depot and numbered stop markers',
                'Demonstrable algorithm visualizer with step-by-step priority queue trace',
                'Production-grade Spring Boot 3 + PostgreSQL with Flyway database migrations',
              ].map((text, i) => (
                <div key={i} className="flex items-start space-x-3">
                  <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                  <span className="text-sm text-slate-700 font-medium">{text}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-gradient-to-br from-slate-900 to-blue-950 p-8 rounded-2xl text-white shadow-xl">
            <h3 className="text-xl font-bold mb-4 text-blue-200">Full-Stack Technology Stack</h3>
            <div className="space-y-4 text-sm text-slate-300">
              <div className="border-b border-slate-800 pb-3">
                <span className="font-semibold text-white block">Backend:</span>
                Java 21, Spring Boot 3.3, Spring Data JPA, Spring Security, JWT, Flyway
              </div>
              <div className="border-b border-slate-800 pb-3">
                <span className="font-semibold text-white block">Algorithms:</span>
                Dijkstra, A*, 2-Opt Local Search, Held-Karp Exact DP, Haversine
              </div>
              <div className="border-b border-slate-800 pb-3">
                <span className="font-semibold text-white block">Frontend:</span>
                React 18/19, Vite, Tailwind CSS, Leaflet, Recharts, Lucide React
              </div>
              <div>
                <span className="font-semibold text-white block">Database & Services:</span>
                PostgreSQL, Hibernate, OSRM Road Router, OpenStreetMap Nominatim
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-auto bg-slate-900 text-slate-400 py-8 border-t border-slate-800 text-xs">
        <div className="max-w-7xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2 text-white font-bold">
            <Navigation className="w-4 h-4 text-blue-400" />
            <span>RouteWise — Intelligent Route Optimization System</span>
          </div>
          <p>© 2026 RouteWise. Built with Java 21, Spring Boot 3, and React.</p>
        </div>
      </footer>
    </div>
  );
};
