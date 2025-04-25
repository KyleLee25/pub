import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import ChallengeResponse from './ChallengeResponse';
import './Auth.css';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { signIn, currentChallenge, handleChallenge, error, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && user) {
      // Redirect based on user role
      switch (user.role) {
        case 'admin':
          navigate('/admin');
          break;
        case 'teacher':
          navigate('/teacher');
          break;
        case 'student':
          navigate('/dashboard');
          break;
        default:
          navigate('/dashboard');
      }
    }
  }, [isAuthenticated, user, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await signIn(username, password);
      // Navigation will be handled by the useEffect when isAuthenticated changes
    } catch (error) {
      console.error('Login error:', error);
      // Error is already set in the AuthContext
    } finally {
      setLoading(false);
    }
  };

  const handleChallengeSuccess = () => {
    // Navigation will be handled by the useEffect when isAuthenticated changes
  };

  const handleChallengeError = (error: Error) => {
    console.error('Challenge error:', error);
  };

  if (currentChallenge) {
    return (
      <ChallengeResponse
        challengeName={currentChallenge}
        onSuccess={handleChallengeSuccess}
        onError={handleChallengeError}
      />
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h2>Login</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        <div className="auth-footer">
          Don't have an account?{' '}
          <button 
            className="link-button"
            onClick={() => navigate('/signup')}
            disabled={loading}
          >
            Sign Up
          </button>
        </div>
      </div>
    </div>
  );
};

export default Login; 