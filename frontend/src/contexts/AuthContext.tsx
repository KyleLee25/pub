import React, { createContext, useContext, useState, useEffect } from 'react';
import { 
  getCurrentUser, 
  fetchUserAttributes, 
  AuthUser, 
  signOut,
  signIn,
  confirmSignIn,
  SignInInput,
  ConfirmSignInInput
} from 'aws-amplify/auth';
import { UserAttributeKey } from '@aws-amplify/auth';

interface ExtendedAuthUser extends AuthUser {
  id: string;
  role: 'admin' | 'teacher' | 'student';
}

type UserAttributes = Partial<Record<UserAttributeKey, string>>;

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: ExtendedAuthUser | null;
  userAttributes: UserAttributes | null;
  checkAuth: () => Promise<void>;
  logout: () => Promise<void>;
  signIn: (username: string, password: string) => Promise<void>;
  handleChallenge: (challengeName: string, challengeResponse: ConfirmSignInInput) => Promise<void>;
  currentChallenge: string | null;
  error: string | null;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  userAttributes: null,
  checkAuth: async () => {},
  logout: async () => {},
  signIn: async () => {},
  handleChallenge: async () => {},
  currentChallenge: null,
  error: null
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<ExtendedAuthUser | null>(null);
  const [userAttributes, setUserAttributes] = useState<UserAttributes | null>(null);
  const [currentChallenge, setCurrentChallenge] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const checkAuth = async () => {
    try {
      const currentUser = await getCurrentUser();
      const attributes = await fetchUserAttributes();
      
      const userId = 
        attributes['custom:studentId'] || 
        attributes['custom:teacherId'] || 
        attributes.sub || 
        'unknown';

      const extendedUser: ExtendedAuthUser = {
        ...currentUser,
        id: userId,
        role: (attributes['custom:role'] as 'admin' | 'teacher' | 'student') || 'student'
      };

      setUser(extendedUser);
      setUserAttributes(attributes);
      setIsAuthenticated(true);
      setCurrentChallenge(null);
      setError(null);
    } catch (error) {
      setUser(null);
      setUserAttributes(null);
      setIsAuthenticated(false);
      setCurrentChallenge(null);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSignIn = async (username: string, password: string) => {
    try {
      setError(null);
      const signInInput: SignInInput = {
        username,
        password,
      };
      const result = await signIn(signInInput);
      
      if (result.isSignedIn) {
        await checkAuth();
      } else {
        switch (result.nextStep.signInStep) {
          case 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED':
            setCurrentChallenge('NEW_PASSWORD_REQUIRED');
            break;
          case 'CONFIRM_SIGN_IN_WITH_SMS_CODE':
            setCurrentChallenge('SMS_MFA');
            break;
          case 'CONFIRM_SIGN_IN_WITH_TOTP_CODE':
            setCurrentChallenge('SOFTWARE_TOKEN_MFA');
            break;
          case 'CONFIRM_SIGN_IN_WITH_CUSTOM_CHALLENGE':
            setCurrentChallenge('CUSTOM_CHALLENGE');
            break;
          default:
            setError('Unexpected sign-in step: ' + result.nextStep.signInStep);
        }
      }
    } catch (error: any) {
      if (error.name === 'UserNotConfirmedException') {
        setError('Please confirm your account before signing in.');
      } else if (error.name === 'NotAuthorizedException') {
        setError('Incorrect username or password.');
      } else if (error.name === 'UserNotFoundException') {
        setError('User not found.');
      } else {
        setError(error.message || 'An error occurred during sign in.');
      }
      throw error;
    }
  };

  const handleChallenge = async (challengeName: string, challengeResponse: ConfirmSignInInput) => {
    try {
      setError(null);
      const result = await confirmSignIn(challengeResponse);
      
      if (result.isSignedIn) {
        // Update auth state immediately
        const currentUser = await getCurrentUser();
        const attributes = await fetchUserAttributes();
        
        const userId = 
          attributes['custom:studentId'] || 
          attributes['custom:teacherId'] || 
          attributes.sub || 
          'unknown';

        const extendedUser: ExtendedAuthUser = {
          ...currentUser,
          id: userId,
          role: (attributes['custom:role'] as 'admin' | 'teacher' | 'student') || 'student'
        };

        setUser(extendedUser);
        setUserAttributes(attributes);
        setIsAuthenticated(true);
        setCurrentChallenge(null);
        setError(null);
      } else {
        switch (result.nextStep.signInStep) {
          case 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED':
            setCurrentChallenge('NEW_PASSWORD_REQUIRED');
            break;
          case 'CONFIRM_SIGN_IN_WITH_SMS_CODE':
            setCurrentChallenge('SMS_MFA');
            break;
          case 'CONFIRM_SIGN_IN_WITH_TOTP_CODE':
            setCurrentChallenge('SOFTWARE_TOKEN_MFA');
            break;
          case 'CONFIRM_SIGN_IN_WITH_CUSTOM_CHALLENGE':
            setCurrentChallenge('CUSTOM_CHALLENGE');
            break;
          default:
            setError('Unexpected sign-in step: ' + result.nextStep.signInStep);
        }
      }
    } catch (error: any) {
      setError(error.message || 'Failed to complete challenge');
      throw error;
    }
  };

  const handleLogout = async () => {
    try {
      await signOut();
      setUser(null);
      setUserAttributes(null);
      setIsAuthenticated(false);
      setCurrentChallenge(null);
      setError(null);
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  useEffect(() => {
    checkAuth();
  }, []);

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      isLoading,
      user,
      userAttributes,
      checkAuth,
      logout: handleLogout,
      signIn: handleSignIn,
      handleChallenge,
      currentChallenge,
      error
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext); 