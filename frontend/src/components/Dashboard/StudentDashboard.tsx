import React, { useState, useEffect } from 'react';
import { Course, Grade, CourseRegistration, Notification } from '../../api/types';
import api from '../../api/api';
import { useAuth } from '../../contexts/AuthContext';
import './Dashboard.css';

const StudentDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [registrations, setRegistrations] = useState<CourseRegistration[]>([]);
  const [grades, setGrades] = useState<Grade[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [activeTab, setActiveTab] = useState<'courses' | 'grades' | 'notifications'>('courses');

  useEffect(() => {
    if (user?.id) {
      loadData();
    }
  }, [user]);

  const loadData = async () => {
    if (!user?.id) return;

    try {
      const [coursesData, registrationsData, gradesData, notificationsData] = await Promise.all([
        api.getCourses(),
        api.getStudentRegistrations(user.id),
        api.getGrades(user.id),
        api.getStudentNotifications('current'),
      ]);
      setCourses(coursesData);
      setRegistrations(registrationsData);
      setGrades(gradesData);
      setNotifications(notificationsData);
    } catch (error) {
      console.error('Error loading student data:', error);
    }
  };

  const handleCourseRegistration = async (courseId: string) => {
    try {
      await api.submitCourseRegistration(courseId, user!.id);
      loadData(); // Refresh data
    } catch (error) {
      console.error('Error registering for course:', error);
    }
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Student Dashboard</h1>
        <div className="header-actions">
          <div className="tab-navigation">
            <button
              className={activeTab === 'courses' ? 'active' : ''}
              onClick={() => setActiveTab('courses')}
            >
              Available Courses
            </button>
            <button
              className={activeTab === 'grades' ? 'active' : ''}
              onClick={() => setActiveTab('grades')}
            >
              My Grades
            </button>
            <button
              className={activeTab === 'notifications' ? 'active' : ''}
              onClick={() => setActiveTab('notifications')}
            >
              Notifications
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
            <div className="registered-courses">
              <h2>My Registered Courses</h2>
              <div className="courses-grid">
                {registrations.map((registration) => {
                  const course = courses.find((c) => c.id === registration.courseId);
                  return course ? (
                    <div key={course.id} className="course-card">
                      <h3>{course.name}</h3>
                      <p>Code: {course.code}</p>
                      <p>Credits: {course.credits}</p>
                      <div className={`status-badge ${registration.status}`}>
                        {registration.status.charAt(0).toUpperCase() + registration.status.slice(1)}
                      </div>
                    </div>
                  ) : null;
                })}
              </div>
            </div>

            <div className="available-courses">
              <h2>Available Courses</h2>
              <div className="courses-grid">
                {courses
                  .filter(course => !registrations.some(reg => reg.courseId === course.id))
                  .map(course => (
                    <div key={course.id} className="course-card">
                      <h3>{course.name}</h3>
                      <p>Code: {course.code}</p>
                      <p>Credits: {course.credits}</p>
                      <button 
                        className="register-button"
                        onClick={() => handleCourseRegistration(course.id)}
                      >
                        Register
                      </button>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'grades' && (
          <div className="grades-section">
            <h2>My Grades</h2>
            <div className="grades-table">
              <table>
                <thead>
                  <tr>
                    <th>Course</th>
                    <th>Grade</th>
                    <th>Semester</th>
                  </tr>
                </thead>
                <tbody>
                  {grades.map((grade) => {
                    const course = courses.find((c) => c.id === grade.courseId);
                    return (
                      <tr key={grade.id}>
                        <td>{course?.name || 'Unknown Course'}</td>
                        <td>{grade.grade}</td>
                        <td>{grade.semester}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'notifications' && (
          <div className="notifications-section">
            <h2>Notifications</h2>
            <div className="notifications-list">
              {notifications.map((notification) => (
                <div key={notification.id} className="notification-card">
                  <h3>{notification.type}</h3>
                  <p>{notification.message}</p>
                  <p>Date: {new Date(notification.createdAt).toLocaleDateString()}</p>
                  <p>Status: {notification.read ? 'Read' : 'Unread'}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentDashboard; 