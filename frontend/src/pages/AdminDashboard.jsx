import UsersPage from "./UsersPage";
import DevicesPage from "./DevicesPage";
import AssignPage from "./AssignPage";
import { useState } from "react";

export default function AdminDashboard() {
    const [tab, setTab] = useState("users");

    return (
        <div style={{ padding: 20 }}>
            <h1>Admin Dashboard</h1>

            <div style={{ display: "flex", gap: 10 }}>
                <button onClick={() => setTab("users")}>Users</button>
                <button onClick={() => setTab("devices")}>Devices</button>
                <button onClick={() => setTab("assign")}>Assign Devices</button>
            </div>

            {tab === "users" && <UsersPage />}
            {tab === "devices" && <DevicesPage />}
            {tab === "assign" && <AssignPage />}
        </div>
    );
}
