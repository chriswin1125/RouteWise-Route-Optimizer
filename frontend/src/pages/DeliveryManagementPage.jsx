import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useNavigate } from 'react-router-dom';
import {
  Package,
  Plus,
  Search,
  Filter,
  Trash2,
  Edit2,
  CheckCircle2,
  MapPin,
  Phone,
  AlertCircle,
  Navigation,
} from 'lucide-react';
import { StatusBadge, PriorityBadge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const DeliveryManagementPage = () => {
  const navigate = useNavigate();
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');

  // Modal State
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingOrder, setEditingOrder] = useState(null);
  const [geocoding, setGeocoding] = useState(false);
  const [formError, setFormError] = useState('');

  const [formData, setFormData] = useState({
    customerName: '',
    address: '',
    latitude: 37.7749,
    longitude: -122.4194,
    contactPhone: '',
    priority: 'MEDIUM',
    status: 'PENDING',
    notes: '',
  });

  const fetchDeliveries = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 10,
        sort: 'createdAt,desc',
      };
      if (search.trim()) params.search = search.trim();
      if (statusFilter) params.status = statusFilter;
      if (priorityFilter) params.priority = priorityFilter;

      const data = await api.getDeliveries(params);
      setDeliveries(data.content || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Failed to load deliveries:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeliveries();
  }, [page, statusFilter, priorityFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchDeliveries();
  };

  const handleGeocodeAddress = async () => {
    if (!formData.address) return;
    setGeocoding(true);
    setFormError('');
    try {
      const result = await api.geocodeAddress(formData.address);
      if (result.success && result.latitude && result.longitude) {
        setFormData((prev) => ({
          ...prev,
          latitude: result.latitude,
          longitude: result.longitude,
          address: result.formattedAddress || prev.address,
        }));
      } else {
        setFormError('Could not locate address coordinates. Please verify address.');
      }
    } catch (err) {
      setFormError('Geocoding service unavailable.');
    } finally {
      setGeocoding(false);
    }
  };

  const handleSaveOrder = async (e) => {
    e.preventDefault();
    setFormError('');

    try {
      if (editingOrder) {
        await api.updateDelivery(editingOrder.id, formData);
      } else {
        await api.createDelivery(formData);
      }
      setShowAddModal(false);
      setEditingOrder(null);
      resetForm();
      fetchDeliveries();
    } catch (err) {
      setFormError(err.message || 'Failed to save delivery order');
    }
  };

  const handleStatusChange = async (id, newStatus) => {
    try {
      await api.updateDeliveryStatus(id, newStatus);
      setDeliveries((prev) =>
        prev.map((d) => (d.id === id ? { ...d, status: newStatus } : d))
      );
    } catch (err) {
      console.error('Status update failed:', err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this delivery order?')) return;
    try {
      await api.deleteDelivery(id);
      fetchDeliveries();
    } catch (err) {
      alert(err.message || 'Failed to delete order');
    }
  };

  const handleOpenEdit = (order) => {
    setEditingOrder(order);
    setFormData({
      customerName: order.customerName,
      address: order.address,
      latitude: order.latitude,
      longitude: order.longitude,
      contactPhone: order.contactPhone || '',
      priority: order.priority || 'MEDIUM',
      status: order.status || 'PENDING',
      notes: order.notes || '',
    });
    setShowAddModal(true);
  };

  const resetForm = () => {
    setFormData({
      customerName: '',
      address: '',
      latitude: 37.7749,
      longitude: -122.4194,
      contactPhone: '',
      priority: 'MEDIUM',
      status: 'PENDING',
      notes: '',
    });
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <Package className="w-7 h-7 text-blue-600" />
            Delivery Management
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Track parcels, priorities, delivery coordinates, and dispatch assignments.
          </p>
        </div>

        <button
          onClick={() => {
            resetForm();
            setEditingOrder(null);
            setShowAddModal(true);
          }}
          className="inline-flex items-center space-x-1.5 px-4 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
        >
          <Plus className="w-4 h-4" />
          <span>New Delivery Order</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex flex-col md:flex-row items-center justify-between gap-3">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3 pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by customer name or address..."
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </form>

        <div className="flex items-center space-x-3 w-full md:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-blue-600"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) => {
              setPriorityFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-blue-600"
          >
            <option value="">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>
      </div>

      {/* Deliveries Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16">
            <LoadingSpinner text="Fetching delivery orders..." />
          </div>
        ) : deliveries.length === 0 ? (
          <div className="text-center py-16 text-slate-400">
            <Package className="w-12 h-12 mx-auto mb-3 opacity-40" />
            <p className="font-semibold text-slate-700">No delivery orders found</p>
            <p className="text-xs text-slate-400 mt-1">Create an order or adjust your active filters.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold text-[11px] tracking-wider">
                <tr>
                  <th className="py-3 px-4">Customer & Phone</th>
                  <th className="py-3 px-4">Delivery Address</th>
                  <th className="py-3 px-4">Priority</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Assigned Route</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {deliveries.map((order) => (
                  <tr key={order.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-bold text-slate-900">{order.customerName}</div>
                      {order.contactPhone && (
                        <div className="text-[11px] text-slate-400 flex items-center gap-1 mt-0.5">
                          <Phone className="w-3 h-3" />
                          <span>{order.contactPhone}</span>
                        </div>
                      )}
                    </td>

                    <td className="py-3.5 px-4 max-w-xs">
                      <div className="text-slate-700 font-medium truncate flex items-start gap-1">
                        <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
                        <span className="truncate">{order.address}</span>
                      </div>
                      <div className="text-[10px] text-slate-400 font-mono mt-0.5 ml-4.5">
                        {order.latitude.toFixed(4)}, {order.longitude.toFixed(4)}
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <PriorityBadge priority={order.priority} />
                    </td>

                    <td className="py-3.5 px-4">
                      <select
                        value={order.status}
                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                        className="text-xs font-semibold rounded-lg px-2 py-1 border border-slate-200 bg-white text-slate-800 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      >
                        <option value="PENDING">Pending</option>
                        <option value="ASSIGNED">Assigned</option>
                        <option value="IN_TRANSIT">In Transit</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="CANCELLED">Cancelled</option>
                      </select>
                    </td>

                    <td className="py-3.5 px-4">
                      {order.assignedRouteName ? (
                        <span className="inline-flex items-center gap-1 text-[11px] text-blue-600 font-semibold">
                          <Navigation className="w-3 h-3" />
                          {order.assignedRouteName}
                        </span>
                      ) : (
                        <span className="text-slate-400 text-[11px]">Unassigned</span>
                      )}
                    </td>

                    <td className="py-3.5 px-4 text-right space-x-2">
                      <button
                        onClick={() => handleOpenEdit(order)}
                        className="text-slate-400 hover:text-blue-600 p-1 rounded"
                        title="Edit Order"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDelete(order.id)}
                        className="text-slate-400 hover:text-red-600 p-1 rounded"
                        title="Delete Order"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        <div className="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
          <div>
            Showing <span className="font-semibold text-slate-700">{deliveries.length}</span> of{' '}
            <span className="font-semibold text-slate-700">{totalElements}</span> orders
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1 bg-white border border-slate-200 rounded-lg font-medium hover:bg-slate-50 disabled:opacity-40"
            >
              Previous
            </button>
            <span className="font-semibold text-slate-700">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 bg-white border border-slate-200 rounded-lg font-medium hover:bg-slate-50 disabled:opacity-40"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Add / Edit Delivery Order Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-slate-900">
              {editingOrder ? 'Edit Delivery Order' : 'Create New Delivery Order'}
            </h3>

            {formError && (
              <div className="p-3 rounded-lg bg-red-50 text-red-700 text-xs border border-red-200">
                {formError}
              </div>
            )}

            <form className="space-y-3.5" onSubmit={handleSaveOrder}>
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                  Customer / Facility Name
                </label>
                <input
                  type="text"
                  required
                  value={formData.customerName}
                  onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                  placeholder="e.g. Blue Bottle Coffee Roastery"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                  Delivery Address
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    required
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    placeholder="e.g. 315 Linden St, San Francisco, CA"
                    className="flex-1 px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  />
                  <button
                    type="button"
                    onClick={handleGeocodeAddress}
                    disabled={geocoding}
                    className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg shrink-0 transition-colors"
                  >
                    {geocoding ? 'Locating...' : 'Geocode'}
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">Latitude</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formData.latitude}
                    onChange={(e) => setFormData({ ...formData, latitude: parseFloat(e.target.value) })}
                    className="w-full px-3 py-1.5 border border-slate-300 rounded-lg text-xs text-slate-900"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-500 mb-1">Longitude</label>
                  <input
                    type="number"
                    step="any"
                    required
                    value={formData.longitude}
                    onChange={(e) => setFormData({ ...formData, longitude: parseFloat(e.target.value) })}
                    className="w-full px-3 py-1.5 border border-slate-300 rounded-lg text-xs text-slate-900"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                    Contact Phone
                  </label>
                  <input
                    type="text"
                    value={formData.contactPhone}
                    onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                    placeholder="+1-415-555-0142"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                    Priority
                  </label>
                  <select
                    value={formData.priority}
                    onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
                  Dispatch Notes / Delivery Instructions
                </label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  placeholder="Gate code, loading dock instructions, refrigeration..."
                  rows={2}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold shadow-sm transition-colors"
                >
                  {editingOrder ? 'Save Changes' : 'Create Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
