# SecureTouch - A Fingerprint Authenticator for Web App Logins

## Overview
SecureTouch is a prototype of a fingerprint-based biometric authentication system designed to fortify the security of web app logins. Leveraging Android's support for handling biometrics, SecureTouch offers a user-friendly alternative to conventional OTP and rolling code mobile authenticator applications.

## Features
SecureTouch utilizes a multistage setup process for users, designed to ensure maximum security and future ease of use:

- **Initial Setup with Login Credentials**: Users start by entering their standard login credentials (email and password). This step verifies the user's identity and grants initial access to the SecureTouch setup process.
  
- **Passphrase Verification**: Following successful initial authentication, users are prompted to enter a 12-word passphrase. This passphrase, generated during the web application's account setup, adds an extra layer of security, linking the user's web application account with their SecureTouch authenticator app.
  
- **Fingerprint Enrollment**: Upon successful passphrase verification, users are guided to register their fingerprint on their Android device. SecureTouch stores the fingerprint data securely, leveraging the device's built-in security features to ensure that biometric data remains private and protected.
  
- **PIN Setup**: The final stage of the setup process involves creating a 6-digit PIN. This PIN will be required to access the SecureTouch app, providing a quick and secure method for users to authenticate themselves in future sessions.

After the initial setup, accessing the SecureTouch app requires only the user's PIN and fingerprint, streamlining the authentication process for web app logins. This approach minimizes the need for cumbersome authentication steps after the initial setup.
