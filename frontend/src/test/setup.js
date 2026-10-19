import '@testing-library/jest-dom';

// Mock Leaflet as it requires a real DOM canvas/SVG environment
global.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
};
