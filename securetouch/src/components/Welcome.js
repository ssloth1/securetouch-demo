import React from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signOut } from 'firebase/auth';
import predicamentImage from '../images/predicament.jpeg';

/**
 * Component to display a welcome message to the user after they have successfully logged in.
 * This is the final page the user sees after authenticating with the android application.
 */
function Welcome() {
  const navigate = useNavigate();
  const auth = getAuth();

  // handles logout action in the application and firebase
  const handleLogout = () => {
    signOut(auth).then(() => {
      navigate('/');
    }).catch((error) => {
      console.error("Logout Error:", error);
    });
  };

  return (
    <div className="flex flex-col justify-center items-center h-screen space-y-4 bg-gradient-to-r from-blue-400 to-purple-600">
      <div className="p-1 mb-12 shadow-xl">
        <img src={predicamentImage} alt="My trapcard activated" className="max-w-xs md:max-w-sm lg:max-w-md xl:max-w-lg"/>
      </div>
      <button onClick={handleLogout} className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
        Logout
      </button>
    </div>
  );
}

export default Welcome;