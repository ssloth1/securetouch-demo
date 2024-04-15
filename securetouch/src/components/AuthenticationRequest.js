import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { doc, onSnapshot, getFirestore } from 'firebase/firestore';
import Lottie from 'react-lottie';
import bioconfirmed from '../animations/bioconfirmed.json';

/**
 * This component is responsible for displaying the authentication request page.
 * It listens for changes to the login attempt document in the Firestore database.
 * When the status of the document changes to 'authenticated', the component plays an animation.
 * After the animation finishes, the user is redirected to the welcome page.
 * 
 * In order to successfully authenticate, the user must provide their PIN then their fingerprint,
 * this is all done through the android app, for handling the fingerprint authentication. 
 * 
 * @returns {JSX.Element}
 */
function AuthRequest() {
    const location = useLocation();
    const navigate = useNavigate();
    const { sessionId } = location.state;
    const [status, setStatus] = useState('Pending...');
    const [playAnimation, setPlayAnimation] = useState(false);

    // Lottie options, this is the animation that will play when the user is authenticated
    const defaultOptions = { loop: false, autoplay: true, animationData: bioconfirmed, rendererSettings: { preserveAspectRatio: 'xMidYMid slice' } };
    
    useEffect(() => {
        const firestore = getFirestore();
        const loginAttemptRef = doc(firestore, 'login_attempts', sessionId);

        // Listen for changes to the login attempt document,
        // thankfully firebase will only trigger the callback when the document changes
        const authenticationListener = onSnapshot(loginAttemptRef, (docSnapshot) => {
            const docData = docSnapshot.data();
            if (docData && docData.status) {
                setStatus(`Status: ${docData.status}`);
                if (docData.status === 'authenticated') {
                    setPlayAnimation(true);
                }
            }
        });
        return () => authenticationListener();
    }, [sessionId, navigate]);

    // Redirect to the welcome page after the animation finishes
    const handleAnimationFinish = () => { navigate('/welcome'); };

    return (
        <div className="flex justify-center items-center h-screen bg-gradient-to-r from-blue-400 to-purple-600">
            <div className="max-w-md bg-white rounded p-8 shadow-lg">
                <h2 className="text-2xl font-semibold mb-4">Authentication Request</h2>
                <p>{status}</p>
                {playAnimation && (
                    <Lottie options={defaultOptions} height={400} width={400} isStopped={!playAnimation} isPaused={false} eventListeners={[{ eventName: 'complete', callback: handleAnimationFinish}]}/>
                )}
            </div>
        </div>
    );
};

export default AuthRequest;