import React, { useState } from 'react';
import { confirmSignIn, ConfirmSignInInput } from 'aws-amplify/auth';
import './Auth.css';

interface ChallengeResponseProps {
  challengeName: string;
  onSuccess: () => void;
  onError: (error: Error) => void;
  challengeParameters?: {
    SALT?: string;
    SECRET_BLOCK?: string;
    SRP_B?: string;
    USERNAME?: string;
    USER_ID_FOR_SRP?: string;
    requiredAttributes?: string;
    userAttributes?: string;
  };
}

const ChallengeResponse: React.FC<ChallengeResponseProps> = ({
  challengeName,
  onSuccess,
  onError,
  challengeParameters,
}) => {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      let challengeResponse: ConfirmSignInInput;

      switch (challengeName) {
        case 'NEW_PASSWORD_REQUIRED':
          if (newPassword !== confirmPassword) {
            throw new Error('Passwords do not match');
          }
          if (newPassword.length < 8) {
            throw new Error('Password must be at least 8 characters long');
          }
          if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}/.test(newPassword)) {
            throw new Error('Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character');
          }
          challengeResponse = {
            challengeResponse: newPassword,
          };
          break;

        case 'PASSWORD_VERIFIER':
        case 'SMS_MFA':
        case 'SOFTWARE_TOKEN_MFA':
          if (!code) {
            throw new Error('Please enter the verification code');
          }
          challengeResponse = {
            challengeResponse: code,
          };
          break;

        default:
          throw new Error(`Unsupported challenge: ${challengeName}`);
      }

      const result = await confirmSignIn(challengeResponse);
      
      if (result.isSignedIn) {
        onSuccess();
      } else {
        throw new Error('Authentication failed. Please try again.');
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An error occurred';
      setError(errorMessage);
      onError(error as Error);
    } finally {
      setLoading(false);
    }
  };

  const renderChallengeForm = () => {
    switch (challengeName) {
      case 'NEW_PASSWORD_REQUIRED':
        return (
          <>
            <div className="form-group">
              <label htmlFor="newPassword">New Password</label>
              <input
                id="newPassword"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                minLength={8}
                pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"
                title="Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
              />
            </div>
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
          </>
        );

      case 'PASSWORD_VERIFIER':
      case 'SMS_MFA':
      case 'SOFTWARE_TOKEN_MFA':
        return (
          <div className="form-group">
            <label htmlFor="code">Verification Code</label>
            <input
              id="code"
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              placeholder="Enter the verification code sent to your email"
            />
          </div>
        );

      default:
        return <p>Unsupported challenge type</p>;
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h2>Additional Verification Required</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          {renderChallengeForm()}
          <button type="submit" disabled={loading}>
            {loading ? 'Processing...' : 'Submit'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChallengeResponse; 