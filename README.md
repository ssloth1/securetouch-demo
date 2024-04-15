# SecureTouch Demo - Authentication System

## Introduction
SecureTouch is a prototype mobile authenticator application developed for Android, designed to offer secure fingerprint and PIN authentication for web application sign-ins. This project also includes a primary web interface built with React and Firebase, enabling users to register and log in, serving as a platform to test the SecureTouch application.

## Authentication Flow
### Registering Your Authenticator Application Against Your Account
- **Something You Know**: Users begin the setup by entering their email and password associated with the web application, forming the first layer of authentication based on knowledge-based credentials.
- **Something You Have**: After initial login, users are prompted to enter 12 backup phrases that are generated during web registration. These phrases, which must be securely stored, act as a physical token in the authentication process.
 
### Setting Up Authentication Mechanisms for Future Use
- **Something You Are**: The setup continues with users registering their fingerprints on their Android device. This biometric data is stored locally on the device, adding a biometric layer to the security framework.
- **Something You Know**: Finally, users set up a 6-digit PIN, which serves as a quick and secure method to access the app, ensuring it remains secure even if the device is compromised.

## Usage
Each time a user logs into the web application, they are prompted to authenticate using two factors via the Android app: their PIN and fingerprint. Successful authentication allows access to the web application. In cases of device loss or functionality issues, users can restart the setup process.

## Web Application
The web interface acts as a simulation environment for web logins, integrating with the Android app via Firebase.

## Getting Started
- [SecureTouch Web Portal](https://jbebarski.com/)
- [SecureTouch Android Application](https://github.com/ssloth1/securetouch-demo/blob/main/android/app/release/app-release.apk)

## Web Interface Preview
<p align="center">
  <img src="/web-preview/Screenshot (58).png" alt="Welcome Screen" width="200"/>
  <img src="/web-preview/Screenshot (60).png" alt="Registration" width="200"/>
  <img src="/web-preview/Screenshot (63).png" alt="Backup Codes" width="200"/>
  <img src="/web-preview/Screenshot (64).png" alt="Login" width="200"/>
</p>



