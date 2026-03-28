import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * ProtectedRoute – guards routes by auth & role.
 *   <ProtectedRoute>             → must be logged in
 *   <ProtectedRoute requiredRole="ADMIN"> → must be ADMIN
 */
export default function ProtectedRoute({ children, requiredRole }) {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;
  if (requiredRole && user.role !== requiredRole) return <Navigate to="/access-denied" replace />;

  return children;
}

