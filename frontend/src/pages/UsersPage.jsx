import { useEffect, useState } from "react";
import { getUsers, deleteUser, updateUser, registerUser } from "../api";

export default function UsersPage() {
    const [users, setUsers] = useState([]);

    const [editId, setEditId] = useState(null);
    const [editName, setEditName] = useState("");

    const [newUser, setNewUser] = useState({
        name: "",
        username: "",
        password: ""
    });

    async function load() {
        const data = await getUsers();
        setUsers(data);
    }

    async function handleDelete(id) {
        await deleteUser(id);
        load();
    }

    async function handleSave(id) {
        await updateUser(id, { name: editName });
        setEditId(null);
        load();
    }

    async function handleCreate(e) {
        e.preventDefault();

        await registerUser({
            ...newUser,
            role: "CLIENT"
        });

        alert("User created!");

        setNewUser({ name: "", username: "", password: "" });

        load();
    }

    useEffect(() => {
        load();
    }, []);

    return (
        <div className="card">
            <h2>Users</h2>

            {/* CREATE USER  */}
            <form onSubmit={handleCreate} className="list-row" style={{ gap: "10px" }}>
                <input
                    placeholder="Name"
                    value={newUser.name}
                    onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                    required
                />

                <input
                    placeholder="Username"
                    value={newUser.username}
                    onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    required
                />

                <button style={{ whiteSpace: "nowrap" }}>
                    Create
                </button>
            </form>

            <hr />

            {/* Users list */}
            {users.map((u) => (
                <div className="list-row" key={u.id}>
                    {editId === u.id ? (
                        <>
                            <input
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                            />
                            <button onClick={() => handleSave(u.id)}>Save</button>
                            <button className="danger" onClick={() => setEditId(null)}>
                                Cancel
                            </button>
                        </>
                    ) : (
                        <>
                            <span>
                                <strong>{u.name}</strong>
                                <div className="muted">{u.username}</div>
                            </span>

                            <button
                                onClick={() => {
                                    setEditId(u.id);
                                    setEditName(u.name);
                                }}
                            >
                                Edit
                            </button>

                            <button className="danger" onClick={() => handleDelete(u.id)}>
                                Delete
                            </button>
                        </>
                    )}
                </div>
            ))}
        </div>
    );
}
