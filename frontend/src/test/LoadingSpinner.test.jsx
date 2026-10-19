import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { LoadingSpinner, CardSkeleton } from '../components/common/LoadingSpinner';

describe('LoadingSpinner Component', () => {
  it('renders custom loading message', () => {
    render(<LoadingSpinner text="Optimizing routes..." />);
    expect(screen.getByText('Optimizing routes...')).toBeInTheDocument();
  });
});

describe('CardSkeleton Component', () => {
  it('renders skeleton container', () => {
    const { container } = render(<CardSkeleton />);
    expect(container.firstChild).toHaveClass('animate-pulse');
  });
});
