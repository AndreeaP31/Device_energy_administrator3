import { useEffect, useState } from "react";
import {
    getDevices,
    deleteDevice,
    createDevice,
    updateDevice
} from "../api";

export default function DevicesPage() {
    const [devices, setDevices] = useState([]);

    const [newDev, setNewDev] = useState({ name: "", maxCons: ""});
    const [editId, setEditId] = useState(null);
    const [editDev, setEditDev] = useState({ name: "", maxCons: 0 });

    useEffect(() => {
        async function load() {
            const devList = await getDevices();
            setDevices(devList);
        }
        load();
    }, []);

    async function handleCreate(e) {
        e.preventDefault();
        await createDevice(newDev);
        setNewDev({ name: "", maxCons: 0 });

        const updated = await getDevices();
        setDevices(updated);
    }

    async function handleSave(id) {
        await updateDevice(id, {
            name:editDev.name,
            maxCons: Number(editDev.maxCons)
        });
        setEditId(null);

        const updated = await getDevices();
        setDevices(updated);
    }

    return (
        <div className="card">
            <h2>Devices</h2>

            {/* CREATE */}
            <form onSubmit={handleCreate}>
                <label>Device Name</label>
                <input
                    value={newDev.name}
                    onChange={(e) => setNewDev({ ...newDev, name: e.target.value })}
                />

                <label>Max Consumption</label>
                <input
                    type="number"
                    value={newDev.maxCons}
                    onChange={(e) =>
                        setNewDev({ ...newDev, maxCons: Number(e.target.value) })
                    }
                />

                <button style={{ width: "100%" }}>Add Device</button>
            </form>

            <hr style={{ margin: "20px 0" }} />

            {/* LIST */}
            {devices.map((d) => (
                <div className="list-row" key={d.id}>
                    {editId === d.id ? (
                        <>
                            <input
                                value={editDev.name}
                                onChange={(e) =>
                                    setEditDev({ ...editDev, name: e.target.value })
                                }
                            />
                            <input
                                type="number"
                                value={editDev.maxCons}
                                onChange={(e) =>
                                    setEditDev({ ...editDev, maxCons: e.target.value })
                                }
                            />
                            <button onClick={() => handleSave(d.id)}>Save</button>
                            <button className="danger" onClick={() => setEditId(null)}>
                                Cancel
                            </button>
                        </>
                    ) : (
                        <>
                            <span>
                                <strong>{d.name}</strong>
                                <div className="muted">Max: {d.maxCons}</div>
                            </span>

                            <button
                                onClick={() => {
                                    setEditId(d.id);
                                    setEditDev({ name: d.name, maxCons: String(d.maxCons) });
                                }}
                            >
                                Edit
                            </button>

                            <button
                                className="danger"
                                onClick={async () => {
                                    await deleteDevice(d.id);
                                    const updated = await getDevices();
                                    setDevices(updated);
                                }}
                            >
                                Delete
                            </button>
                        </>
                    )}
                </div>
            ))}
        </div>
    );
}
