import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createUserWithEmailAndPassword, getAuth } from 'firebase/auth';
import { doc, setDoc, serverTimestamp } from 'firebase/firestore';
import { db } from '../firebase';
import wordlist from '../wordlist.txt';

/**
 * This component is a form for user registration.
 * It collects the user's email and password, and then creates a new user account.
 * Backup codes are generated and hashed with salts before being stored in Firestore.
 * If the account is created successfully, the user is navigated to the backup codes page.
 * @returns {React.JSX}
 */
function RegistrationForm() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordStrength, setPasswordStrength] = useState(0);
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    /**
     * Function to hash a word with a salt, and then return the hashed word and the salt used.
     * First the salt is generated using the current timestamp.
     * Then the word is concatenated with the salt.
     * Next we encode the word into a Uint8Array and hash it using SHA-256.
     * Finally we convert the hash to hexadecimal format and return it along with the salt.
     * @param {*} word 
     * @returns - { hash: string, salt: string }
     */
    const hashWord = async (word) => {
        const salt = new Date().getTime().toString();
        const saltedWord = word + salt;
        const messageUint8 = new TextEncoder().encode(saltedWord);
        const hashBuffer = await crypto.subtle.digest('SHA-256', messageUint8);
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
        return { hash: hashHex, salt };
    };

    /**
     * This function generates 12 backup codes for the user.
     * It fetches a list of words from a text file and randomly selects 12 words.
     * For the wordlist I used a online dictionary and filtered out words that were too long or too short using a python script.
     * Unfortunately, there are some seedy words in the list, but I didn't want to spend too much time filtering those out for the demo.
     * 
     * see wordlist.txt for the list of words used.
     * 
     * @returns - {Array<string>} (12 backup codes in an array)
     */
    const generateBackupCodes = async () => {
        const response = await fetch(wordlist);
        const text = await response.text();
        const words = text.split('\n');
        let codes = [];
        for (let i = 0; i < 12; i++) {
            // helps me randomly select words from the list, until I have 12 words
            const randomIndex = Math.floor(Math.random() * words.length);
            codes.push(words[randomIndex]);
        }
        return codes;
    }

    /**
     * This function is resonsible for handling new user registration.
     * It includes some basic checks for password strength and matching passwords.
     * If the checks pass, the user is created in Firebase Auth and a document is created in Firestore.
     * user documents contain the email, timeCreated, and hashed backup codes.
     * hashed backup codes are generated from the helper function generateBackupCodes.
     * @returns 
     */
    const handleRegistration = async () => {
        
        // Check if passwords match
        if (password !== confirmPassword) {
            setErrorMessage('Passwords do not match.');
            return;
        }

        // Check password strength
        const strength = calculatePasswordStrength(password);
        if (strength < 3) {
            setErrorMessage('Password is too weak.');
            return;
        }

        try {
            const authentication = getAuth();
            const userCredential = await createUserWithEmailAndPassword(authentication, email, password);
            const user = userCredential.user;
            
            // Generate backup codes and hash them with salts
            const backupCodes = await generateBackupCodes();
            const hashedCodesWithSalts = await Promise.all(backupCodes.map(async (code) => await hashWord(code)));
    
            // Firestore will now store the user document with the email, timeCreated, and hashed backup codes
            await setDoc(doc(db, 'users', user.uid), {
                email: user.email,
                timeCreated: serverTimestamp(),
                backupCodes: hashedCodesWithSalts
            });
            // Navigate to the backup codes page, where the user can view their backup codes and will be instructed to store them.
            navigate('/backup-codes', { state: { backupCodes } });
        } catch (error) {
            console.error('Error registering user:', error);
            setErrorMessage('Error during registration. Please try again.');
        }
    };

    // Below are some helper functions I added from one of my other projects to help with password strength calculation.
    // Not strictly necessary for the demo, but I thought it would be a nice addition.

    /**
     * Function to handle basic password strength calculation.
     * It checks the length of the password and if it contains lowercase, uppercase, digit, and special characters.
     * @param {*} password 
     * @returns - {number} (score out of 5)
     */
    const calculatePasswordStrength = (password) => {
        let score = 0;
        if (password.length >= 8) { score += 1; } // 8 characters or more
        if (/[a-z]/.test(password)) { score += 1; } // at least one lowercase letter
        if (/[A-Z]/.test(password)) { score += 1; } // at least one uppercase letter
        if (/\d/.test(password)) { score += 1; } // at least one digit
        if (/[^a-zA-Z0-9]/.test(password)) { score += 1; } // at least one special character
        return score;
    };

    /**
     * Function to handle the password change events (essentially when the user types in the password field).
     * It also calls the calculatePasswordStrength function to update the password strength meter, as the user types.
     * Providing some live feedback to the user about the strength of their password.
     * @param {*} - password change event
     */
    const handlePasswordChange = (passwordChangeEvent) => {
        const newPassword = passwordChangeEvent.target.value;
        setPassword(newPassword);
        setPasswordStrength(calculatePasswordStrength(newPassword));
    };

    /**
     * Helper function to get the color of the strength meter based on the score from the calculatePasswordStrength function.
     * It returns red for weak, yellow for fair, and green for strong.
     * @param {*} strength 
     * @returns - {string} (tailwindcss color class) (red is bad, yellow is okay, green is good)
     */
    const getStrengthMeterColor = (strength) => {
        if (strength < 2) {
            return 'bg-red-500'; // Weak
        } else if (strength < 4) {
            return 'bg-yellow-500'; // Fair
        } else {
            return 'bg-green-500'; // Strong
        }
    };

    /**
     * This helper function calculates the width of the stength meter, 
     * based on the score returned from the calculatePasswordStrength function.
     * The width is calculated as a percentage of the maximum width (100%) where each score is 20%.
     * @param {*} strength 
     * @returns 
     */
    const getStrengthMeterWidth = (strength) => {
        const maxWidth = 100;
        const widthUnit = maxWidth / 5;
        return `${strength * widthUnit}%`; 
    };

    return (
        <div className="flex justify-center items-center h-screen bg-gradient-to-r from-blue-400 to-purple-600">
            <div className="max-w-md bg-white rounded p-8 shadow-lg">
                <h2 className="text-2xl font-semibold mb-4">Registration</h2>
                <input
                    id="email"
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full p-2 mb-4 border border-gray-300 rounded"
                />
                <input
                    id="password"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={handlePasswordChange}
                    className="w-full p-2 mb-4 border border-gray-300 rounded"
                />
                <input
                    id="confirmPassword"
                    type="password"
                    placeholder="Confirm Password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full p-2 mb-4 border border-gray-300 rounded"
                />
                <div className="w-full bg-gray-200 rounded h-2 mb-4">
                    <div
                        className={`${getStrengthMeterColor(passwordStrength)} h-2 rounded`}
                        style={{ width: getStrengthMeterWidth(passwordStrength) }}
                    ></div>
                </div>
                {errorMessage && <div className="text-red-500 text-center mb-4">{errorMessage}</div>}
                <button onClick={handleRegistration} className="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded">
                    Register
                </button>
            </div>
        </div>
    );
}

export default RegistrationForm;