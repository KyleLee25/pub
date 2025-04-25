import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { signOut } from 'aws-amplify/auth';
import { useNavigate } from 'react-router-dom';
import studentApi from '../../api/studentApi';
import './Dashboard.css';

const Dashboard: React.FC = () => {
  const { userAttributes } = useAuth();
  const navigate = useNavigate();
  const [studentInfo, setStudentInfo] = useState<any>(null);
  const [courses, setCourses] = useState<any[]>([]);
  const [grades, setGrades] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, [userAttributes]);

  const loadDashboardData = async () => {
    try {
      const studentId = userAttributes?.['custom:studentId'];
      if (studentId) {
        const [studentData, coursesData, gradesData] = await Promise.all([
          studentApi.getProfile(studentId),
          studentApi.getCourses(),
          studentApi.getGrades(studentId)
        ]);

        setStudentInfo(studentData);
        setCourses(coursesData);
        setGrades(gradesData);
      }
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut();
      navigate('/login');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  if (loading) {
    return <div>Loading dashboard...</div>;
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Student Dashboard</h1>
        <button onClick={handleSignOut} className="sign-out-button">
          Sign Out
        </button>
      </header>

      <div className="dashboard-content">
        <section className="student-info">
          <h2>Student Information</h2>
          {studentInfo && (
            <div>
              <p><strong>Name:</strong> {studentInfo.name}</p>
              <p><strong>Email:</strong> {studentInfo.email}</p>
              <p><strong>Major:</strong> {studentInfo.major}</p>
              <p><strong>Enrollment Year:</strong> {studentInfo.enrollmentYear}</p>
              <p><strong>Expected Graduation:</strong> {studentInfo.graduationYear}</p>
            </div>
          )}
        </section>

        <section className="courses">
          <h2>Enrolled Courses</h2>
          <div className="courses-grid">
            {courses.map(course => (
              <div key={course.id} className="course-card">
                <h3>{course.name}</h3>
                <p><strong>Code:</strong> {course.code}</p>
                <p><strong>Credits:</strong> {course.credits}</p>
                <p>{course.description}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="grades">
          <h2>Grades</h2>
          <table className="grades-table">
            <thead>
              <tr>
                <th>Course</th>
                <th>Grade</th>
                <th>Semester</th>
              </tr>
            </thead>
            <tbody>
              {grades.map(grade => (
                <tr key={grade.id}>
                  <td>{courses.find(c => c.id === grade.courseId)?.name || 'Unknown Course'}</td>
                  <td>{grade.grade}</td>
                  <td>{grade.semester}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  );
};

export default Dashboard; 