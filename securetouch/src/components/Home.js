import React from "react";
import { Link } from "react-router-dom";

/**
 * Component to display the home page of the application.
 * This is the first page the user sees when they visit the application.
 * It contains links to the register and login pages.
 * @returns {JSX.Element}
 */
function Home() {
    return (
        <div className="flex flex-col justify-center items-center h-screen bg-gradient-to-r from-blue-400 to-purple-600">
            <h2 className="text-4xl font-bold text-white mb-8">Welcome to SecureTouch</h2>
            <p className="text-lg text-white mb-8">I hope you like the prototype!</p>
            <div className="space-x-4">
                <button className="bg-rose-500 hover:bg-rose-600 text-white py-2 px-4 rounded">
                    <Link to="/register">Register</Link>
                </button>
                <button className="bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded">
                    <Link to="/login">Login</Link>
                </button>
            </div>
        </div>
    );
}

export default Home;