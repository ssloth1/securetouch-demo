import './App.css';
import './index.css';
import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { Home, RegistrationForm, LoginForm, Welcome, BackupCodes, AuthRequest } from './components';

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/register" element={<RegistrationForm />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/" element={<Home />} />
          <Route path="/welcome" element={<Welcome />} />
          <Route path="/backup-codes" element={<BackupCodes />} />
          <Route path="/authrequest" element={<AuthRequest />} />
        </Routes>
      </Router>
    </div>
  );
}

export default App;
