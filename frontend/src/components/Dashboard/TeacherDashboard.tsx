import React, { useState, useEffect } from 'react';
import { Course, CourseRegistration, Grade } from '../../api/types';
import api from '../../api/api';
import { useAuth } from '../../contexts/AuthContext';
import './Dashboard.css';

const TeacherDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [pendingRegistrations, setPendingRegistrations] = useState<CourseRegistration[]>([]);
  const [activeTab, setActiveTab] = useState<'courses' | 'registrations' | 'grades'>('courses');

  useEffect(() => {
    if (user?.id) {
      loadData();
    }
  }, [user]);

  const loadData = async () => {
    try {
      const [coursesData, registrationsData] = await Promise.all([
        api.getTeacherCourses(user!.id),
        api.getPendingRegistrations(user!.id),
      ]);
      setCourses(coursesData);
      setPendingRegistrations(registrationsData);
    } catch (error) {
      console.error('Error loading teacher data:', error);
    }
  };

  const handleRegistrationUpdate = async (
    registrationId: string,
    status: 'approved' | 'rejected'
  ) => {
    try {
      await api.updateRegistrationStatus(registrationId, status);
      loadData(); // Refresh data
    } catch (error) {
      console.error('Error updating registration:', error);
    }
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Teacher Dashboard</h1>
        <div className="header-actions">
          <div className="tab-navigation">
            <button
              className={activeTab === 'courses' ? 'active' : ''}
              onClick={() => setActiveTab('courses')}
            >
              My Courses
            </button>
            <button
              className={activeTab === 'registrations' ? 'active' : ''}
              onClick={() => setActiveTab('registrations')}
            >
              Pending Registrations
            </button>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      <div className="dashboard-content">
        {activeTab === 'courses' && (
          <div className="courses-section">
            <h2>My Courses</h2>
            <div className="courses-grid">
              {courses.map((course) => (
                <div key={course.id} className="course-card">
                  <h3>{course.name}</h3>
                  <p>Code: {course.code}</p>
                  <p>Semester: {course.semester}</p>
                  <p>Credits: {course.credits}</p>
                  <button onClick={() => setActiveTab('grades')}>
                    Manage Grades
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'registrations' && (
          <div className="registrations-section">
            <h2>Pending Course Registrations</h2>
            <div className="registrations-list">
              {pendingRegistrations.map((registration) => (
                <div key={registration.id} className="registration-card">
                  <p>Student ID: {registration.studentId}</p>
                  <p>Course ID: {registration.courseId}</p>
                  <p>Requested: {new Date(registration.requestedAt).toLocaleDateString()}</p>
                  <div className="card-actions">
                    <button
                      className="success"
                      onClick={() => handleRegistrationUpdate(registration.id, 'approved')}
                    >
                      Approve
                    </button>
                    <button
                      className="danger"
                      onClick={() => handleRegistrationUpdate(registration.id, 'rejected')}
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TeacherDashboard; 