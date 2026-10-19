import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { StatusBadge, PriorityBadge } from '../components/common/Badge';

describe('StatusBadge Component', () => {
  it('renders Pending status correctly', () => {
    render(<StatusBadge status="PENDING" />);
    expect(screen.getByText('Pending')).toBeInTheDocument();
  });

  it('renders Delivered status correctly', () => {
    render(<StatusBadge status="DELIVERED" />);
    expect(screen.getByText('Delivered')).toBeInTheDocument();
  });
});

describe('PriorityBadge Component', () => {
  it('renders Urgent priority with high emphasis', () => {
    render(<PriorityBadge priority="URGENT" />);
    expect(screen.getByText('URGENT')).toBeInTheDocument();
  });

  it('renders Medium priority', () => {
    render(<PriorityBadge priority="MEDIUM" />);
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });
});
