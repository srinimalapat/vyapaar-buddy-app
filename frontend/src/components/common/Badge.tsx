import React from 'react';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'orange';
  className?: string;
}

const variants = {
  green:  'bg-green-100 text-green-700',
  red:    'bg-red-100 text-red-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  blue:   'bg-blue-100 text-blue-700',
  gray:   'bg-gray-100 text-gray-600',
  orange: 'bg-orange-100 text-orange-700',
};

const Badge: React.FC<BadgeProps> = ({ children, variant = 'gray', className = '' }) => (
  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-semibold ${variants[variant]} ${className}`}>
    {children}
  </span>
);

export default Badge;
