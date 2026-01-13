import { useState } from "react";
import { registerUser } from "../api";

export default function Register() {
    const [form, setForm] = useState({
        name: "",
        username: "",
        password: "",
        role: "CLIENT",
    });

    const [msg, setMsg] = useState("");

    function update(field, value) {
        setForm({ ...form, [field]: value });
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setMsg("");

        try {
            await registerUser(form);
            setMsg("Registration successful! You can now log in.");
        } catch (err) {
            setMsg("Registration failed.");
            console.error(err);
        }
    }

    return (
        <div>
            <h2>Register</h2>
            {msg && <div className="alert success">{msg}</div>}

            <form onSubmit={handleSubmit}>
                <label>Name</label>
                <input
                    value={form.name}
                    onChange={(e) => update("name", e.target.value)}
                    placeholder="John Doe"
                />

                <label>Username</label>
                <input
                    value={form.username}
                    onChange={(e) => update("username", e.target.value)}
                    placeholder="johndoe"
                />

                <label>Password</label>
                <input
                    type="password"
                    value={form.password}
                    onChange={(e) => update("password", e.target.value)}
                    placeholder="•••••••"
                />

                <label>Role</label>
                <select
                    value={form.role}
                    onChange={(e) => update("role", e.target.value)}
                >
                    <option value="CLIENT">CLIENT</option>
                    <option value="ADMIN">ADMIN</option>
                </select>

                <button type="submit" style={{ width: "100%" }}>
                    Register
                </button>
            </form>
        </div>
    );
}
