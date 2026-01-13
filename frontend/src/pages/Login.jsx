import { useState } from "react";
import { login } from "../api";

export default function Login({ onLogin }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");

        try {
            const data = await login(username, password);
            localStorage.setItem("token", data.token);
            onLogin(data);
        } catch (err) {
            setError("Login failed. Check username/password.");
            console.error(err);
        }
    }

    return (
        <div>
            <h2>Login</h2>
            {error && <div className="alert error">{error}</div>}
            <form onSubmit={handleSubmit}>
                <label>Username</label>
                <input
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="johndoe"
                />

                <label>Password</label>
                <input
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    type="password"
                />

                <button type="submit" style={{ marginTop: 8, width: "100%" }}>
                    Login
                </button>
            </form>
        </div>
    );
}
