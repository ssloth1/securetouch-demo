import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';
import { getFirestore, setDoc, doc } from 'firebase/firestore';

/**
 * Component to display the login form to the user.
 * The user can enter their email and password to authenticate with the application.
 * If the login is successful, the user is redirected to the authentication request page.
 * If the login fails, an error message is displayed to the user.
 * 
 * @returns {JSX.Element}
 */
function LoginForm() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState(''); 
    const navigate = useNavigate();

    const handleLogin = async () => {
        const authentication = getAuth();
        try {
            await signInWithEmailAndPassword(authentication, email, password)
            .then(async (userCredential) => {

                // successful sign in.
                const userId = userCredential.user.uid;

                // generate a new session id/token
                const sessionId = `${userId}_${new Date().getTime()}`;
                const firestore = getFirestore();

                // create a new login_attempt document in the apps firestore database (with associated userId, status (starts as pending), timestamp)
                await setDoc(doc(firestore, 'login_attempts', sessionId), { userId: userId, status: 'pending', timestamp: new Date()});
                
                // navigate to the AuthRequest component with the sessionId
                // AuthenticationRequest needs the sessionId to listen for changes to the login_attempt document ('authenticated' status).
                navigate('/authrequest', { state: { sessionId: sessionId } });
            })
            .catch((error) => {
                var errorCode = error.code;
                var errorMessage = error.message;
                if (errorCode === 'authentication/user-not-found') {
                    setErrorMessage('No user found with this email.');
                } else if (errorCode === 'authentication/wrong-password') {
                    setErrorMessage('Incorrect password');
                } else if (errorCode === 'authentication/too-many-requests') {
                    setErrorMessage('Too many requests. Try again later.');
                } else {
                    setErrorMessage('Login failed. Please try again later.');
                }
                console.error("Error: ", errorMessage);
            });
        } catch (error) {
            console.error("Error: ", error);
            setErrorMessage('Login failed. Please try again later.');
        }
    };

        return (
            <div className="flex justify-center items-center h-screen bg-gradient-to-r from-blue-400 to-purple-600">
                <div className="max-w-md bg-white rounded p-8 shadow-lg">
                    <h2 className="text-2xl font-semibold mb-4">Login</h2>
                    {errorMessage && <div className="mb-4 text-red-500">{errorMessage}</div>}
                    <input
                        label="email"
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full p-2 mb-4 border border-gray-300 rounded"
                    />
                    <input
                        label="password"
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full p-2 mb-4 border border-gray-300 rounded"
                    />
                    <button onClick={handleLogin} className="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded">
                        Login
                    </button>
                </div>
            </div>
        );
    }

    export default LoginForm;