import { useEffect, useState } from "react";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";
import AdminDashboard from "./pages/AdminDashboard";
import ClientDashboard from "./pages/ClientDashboard";
import { validateToken } from "./api";

export default function App() {
    const [user, setUser] = useState(null);
    const [activeTab, setActiveTab] = useState("login");

    useEffect(() => {
        async function checkToken() {
            const token = localStorage.getItem("token");
            if (!token) return;

            try {
                const data = await validateToken();
                setUser({
                    userId: data.userId,
                    username: data.username,
                    role: data.role,
                });
            } catch {
                localStorage.removeItem("token");
            }
        }

        checkToken();
    }, []);

    function logout() {
        localStorage.removeItem("token");
        setUser(null);
    }

    return (
        <>
            <Navbar user={user} logout={logout} />

            {!user ? (
                <div className="container auth-layout-single">

                    <div className="tabs">
                        <button
                            className={activeTab === "login" ? "active" : ""}
                            onClick={() => setActiveTab("login")}
                        >
                            Login
                        </button>

                        <button
                            className={activeTab === "register" ? "active" : ""}
                            onClick={() => setActiveTab("register")}
                        >
                            Register
                        </button>
                    </div>

                    <div className="card">
                        {activeTab === "login" ? (
                            <Login onLogin={setUser} />
                        ) : (
                            <Register />
                        )}
                    </div>

                </div>
            ) : user.role === "ADMIN" ? (
                <div className="container">
                    <AdminDashboard />
                </div>
            ) : (
                <div className="container">
                    <ClientDashboard user={user} />
                </div>
            )}

        </>
    );
}
