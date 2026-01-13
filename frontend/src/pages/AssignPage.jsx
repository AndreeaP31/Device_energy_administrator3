import { useEffect, useState } from "react";
import { getUsers, getDevices, assignDevice, getRelations } from "../api";

export default function AssignPage() {
    const [users, setUsers] = useState([]);
    const [devices, setDevices] = useState([]);
    const [relations, setRelations] = useState([]);
    const [selected, setSelected] = useState({ userId: "", deviceId: "" });

    function loadRelations() {
        getRelations().then(setRelations);
    }
    useEffect(() => {
        getUsers().then(setUsers);
        getDevices().then(setDevices);
        loadRelations();
    }, []);



    async function assign() {
        await assignDevice(selected.userId, selected.deviceId);
        alert("Assigned!");
        loadRelations();
    }

    return (
        <div>
            <h2>Assign Device</h2>

            {/* USER SELECT */}
            <select
                onChange={e =>
                    setSelected({ ...selected, userId: e.target.value })
                }
                value={selected.userId}
            >
                <option value="">Select user</option>
                {users.map(u => (
                    <option key={u.id} value={u.id}>
                        {u.name}
                    </option>
                ))}
            </select>

            {/* DEVICE SELECT */}
            <select
                onChange={e =>
                    setSelected({ ...selected, deviceId: e.target.value })
                }
                value={selected.deviceId}
            >
                <option value="">Select device</option>
                {devices.map(d => (
                    <option key={d.id} value={d.id}>
                        {d.name}
                    </option>
                ))}
            </select>

            <button onClick={assign}>Assign</button>

            <hr />

            {/*  LISTA RELAȚIILOR */}
            <h3>Assigned Relations</h3>
            {relations.length === 0 ? (
                <p>No relations yet.</p>
            ) : (
                <table border="1" cellPadding="6">
                    <thead>
                    <tr>
                        <th>User</th>
                        <th>Device</th>
                    </tr>
                    </thead>
                    <tbody>
                    {relations.map(r => (
                        <tr key={r.id}>
                            <td>{r.userName}</td>
                            <td>{r.deviceName}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
