import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import studentApi from '../../api/studentApi';
import './Auth.css';

const SignUp: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [major, setMajor] = useState('');
  const [enrollmentYear, setEnrollmentYear] = useState<number>(new Date().getFullYear());
  const [graduationYear, setGraduationYear] = useState<number>(new Date().getFullYear() + 4);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const studentData = {
        name,
        email,
        password,
        major,
        enrollmentYear,
        graduationYear
      };

      console.log('Attempting to create student:', {
        ...studentData,
        password: '***'
      });

      const studentResponse = await studentApi.createStudent(studentData);
      console.log('Student created successfully:', studentResponse);

      navigate('/login');
    } catch (err: any) {
      console.error('Error during signup:', err);
      setError(err.message || 'An error occurred during signup');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h2>Student Sign Up</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Full Name</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={isLoading}
              minLength={8}
            />
          </div>
          <div className="form-group">
            <label htmlFor="major">Major</label>
            <input
              type="text"
              id="major"
              value={major}
              onChange={(e) => setMajor(e.target.value)}
              required
              disabled={isLoading}
            />
          </div>
          <div className="form-group">
            <label htmlFor="enrollmentYear">Enrollment Year</label>
            <input
              type="number"
              id="enrollmentYear"
              value={enrollmentYear}
              onChange={(e) => setEnrollmentYear(parseInt(e.target.value))}
              required
              disabled={isLoading}
            />
          </div>
          <div className="form-group">
            <label htmlFor="graduationYear">Expected Graduation Year</label>
            <input
              type="number"
              id="graduationYear"
              value={graduationYear}
              onChange={(e) => setGraduationYear(parseInt(e.target.value))}
              required
              disabled={isLoading}
            />
          </div>
          <button 
            type="submit" 
            className="signup-button"
            disabled={isLoading}
          >
            {isLoading ? 'Signing up...' : 'Sign Up'}
          </button>
        </form>
        <div className="auth-footer">
          Already have an account?{' '}
          <button 
            className="link-button"
            onClick={() => navigate('/login')}
            disabled={isLoading}
          >
            Login
          </button>
        </div>
      </div>
    </div>
  );
};

export default SignUp;  