import { Amplify } from 'aws-amplify';

Amplify.configure({
  Auth: {
    Cognito: {
      userPoolId: 'us-east-1_B3HPGgqBV',
      userPoolClientId: '2rvln7q5mcnltlle921uht5aus',
      loginWith: {
        email: true,
        username: true
      }
    }
  }
});