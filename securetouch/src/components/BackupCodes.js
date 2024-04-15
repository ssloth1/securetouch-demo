import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * component to display the backup codes to the user in case they need to recover their account
 * this is the only fallback if the user loses their device, or if biometric authentication fails.
 * @returns 
 */
function BackupCodes() {
    const navigate = useNavigate();
    const location = useLocation();
    // get the backup codes from the location state
    const backupCodes = location.state ? location.state.backupCodes : [];

    // Navigate to the welcome page after the user has recorded the backup codes
    const handleConfirmation = async () => { navigate('/login') };

    return (
        <div className="flex flex-col justify-center items-center h-screen bg-gradient-to-r from-blue-400 to-purple-600 px-4 py-6">
            <h2 className="text-3xl text-white font-bold mb-2 text-center">Your Backup Codes</h2>
            <p className="text-2xl mb-1 text-white text-center">Please write down or print these codes and keep them in a safe place.</p>
            <p className="text-xl text-white text-center">You will need them to access your account if you lose your device,</p>
            <p className="text-xl text-white mb-12 text-center">and so you can set up your android authentication application.</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {backupCodes.map((code, index) => (
                    <div key={index} className="flex items-center justify-center bg-white p-6 shadow-lg rounded-lg">
                        <span className="text-lg font-mono text-gray-800">{index + 1}. {code}</span>
                    </div>
                ))}
            </div>
            <div className="mt-8 flex justify-center">
                <button onClick={handleConfirmation} className="bg-emerald-500 hover:bg-emerald-600 text-white font-bold py-3 px-6 rounded-lg shadow-lg">
                    I have recorded these codes
                </button>
            </div>
        </div>
    );
}

export default BackupCodes;