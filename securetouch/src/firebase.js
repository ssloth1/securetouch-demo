// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyBUfYp88w1YUR-uIW9KVUJT481TW6rZXEs",
  authDomain: "info-assurance-project.firebaseapp.com",
  projectId: "info-assurance-project",
  storageBucket: "info-assurance-project.appspot.com",
  messagingSenderId: "138842350769",
  appId: "1:138842350769:web:05f59c6c81055d8d34ae90",
  measurementId: "G-K0HFC8XZRF"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const db = getFirestore();
const auth = getAuth();

export { db, auth, firebaseConfig, app, analytics};