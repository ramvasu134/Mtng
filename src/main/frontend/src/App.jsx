import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import StudentsPage from './pages/StudentsPage';
import CreateStudentPage from './pages/CreateStudentPage';
import ChatPage from './pages/ChatPage';
import RecordingsPage from './pages/RecordingsPage';
import MeetingRoomPage from './pages/MeetingRoomPage';
import DocumentationPage from './pages/DocumentationPage';
import UserGuidePage from './pages/UserGuidePage';
import AccessDeniedPage from './pages/AccessDeniedPage';

export default function App() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="app-loading">
        <div className="loading-spinner" />
        <p>Loading MTNG…</p>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/access-denied" element={<AccessDeniedPage />} />
      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<DashboardPage />} />
        <Route path="students" element={<StudentsPage />} />
        <Route path="create-student" element={
          <ProtectedRoute requiredRole="ADMIN"><CreateStudentPage /></ProtectedRoute>
        } />
        <Route path="chat" element={<ChatPage />} />
        <Route path="recordings" element={<RecordingsPage />} />
        <Route path="docs" element={<DocumentationPage />} />
        <Route path="userguide" element={<UserGuidePage />} />
      </Route>
      {/* Meeting room with room name parameter */}
      <Route path="/meeting-room/:roomName" element={
        <ProtectedRoute><MeetingRoomPage /></ProtectedRoute>
      } />
      {/* Backward compat: /meeting-room without roomName redirects to dashboard */}
      <Route path="/meeting-room" element={
        <ProtectedRoute><MeetingRoomPage /></ProtectedRoute>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
