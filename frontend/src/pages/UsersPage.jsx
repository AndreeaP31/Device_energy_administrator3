import { useEffect, useState, useRef } from "react";
import { getUsers, deleteUser, updateUser, registerUser } from "../api";
import SockJS from 'sockjs-client'; // ADĂUGAT
import { over } from 'stompjs'; // ADĂUGAT
export default function UsersPage() {
    const [users, setUsers] = useState([]);

    const [editId, setEditId] = useState(null);
    const [editName, setEditName] = useState("");
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [stompClient, setStompClient] = useState(null);
    const [selectedChatUser, setSelectedChatUser] = useState(null);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);
    const [newUser, setNewUser] = useState({
        name: "",
        username: "",
        password: ""
    });
    useEffect(() => {
        const token = localStorage.getItem('token');
        const socket = new SockJS('http://localhost/ws-message');
        const client = over(socket);
        client.debug = null;

        client.connect({ 'Authorization': 'Bearer ' + token }, () => {
            console.log('Admin connected to Support System');
            setStompClient(client);

            // 1. Adminul ascultă pe un canal global pentru a primi notificări de la toți clienții
            client.subscribe('/topic/admin/messages', (payload) => {
                const msg = JSON.parse(payload.body);
                // Dacă mesajul e de la userul selectat sau e trimis de noi către el, îl afișăm
                setMessages(prev => [...prev, msg]);
            });
        });

        return () => { if (client) client.disconnect(); };
    }, []);

    // TRIMITERE RĂSPUNS CĂTRE CLIENT
    const sendAdminReply = () => {
        if (inputValue.trim() && stompClient && selectedChatUser) {
            const chatMsg = {
                sender: "Admin",
                content: inputValue,
                receiverId: selectedChatUser.id, // ID-ul clientului selectat
                timestamp: new Date().toISOString()
            };

            // Trimitem mesajul privat către client
            stompClient.send("/app/chat.private", {}, JSON.stringify(chatMsg));

            // Îl adăugăm și local în listă pentru a-l vedea în fereastră
            setInputValue("");
        }
    };
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
        <div style={{ display: 'flex', gap: '20px', padding: '20px', alignItems: 'flex-start' }}>
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
                <div className="list-row" key={u.id} style={{
                    background: selectedChatUser?.id === u.id ? '#f0f5ff' : 'transparent',
                    borderLeft: selectedChatUser?.id === u.id ? '4px solid #273c75' : '4px solid transparent'
                }}>

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
                            <div style={{ display: 'flex', gap: '5px' }}>

                                {/* --- BUTONUL DE CHAT PE CARE TREBUIE SĂ ÎL ADAUGI --- */}
                                <button
                                    onClick={() => {
                                        setSelectedChatUser(u);
                                        // Opțional: setMessages([]); // Dacă vrei să cureți ecranul la fiecare schimbare
                                    }}
                                    style={{
                                        background: selectedChatUser?.id === u.id ? '#44bd32' : '#273c75',
                                        color: 'white'
                                    }}
                                >
                                    {selectedChatUser?.id === u.id ? 'Active Chat' : 'Chat'}
                                </button>
                                {/* -------------------------------------------------- */}

                                <button onClick={() => { setEditId(u.id); setEditName(u.name); }}>
                                    Edit
                                </button>

                                <button className="danger" onClick={() => handleDelete(u.id)}>
                                    Delete
                                </button>
                            </div>

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

            {/* COLOANA DREAPTĂ: FEREASTRA DE CHAT */}
            <div className="card" style={{ flex: 1, minWidth: '350px', border: '2px solid #273c75', height: 'fit-content' }}>
                <h3 style={{ color: '#273c75' }}>Support: {selectedChatUser ? selectedChatUser.name : "Select a user"}</h3>

                <div style={{
                    height: '400px',
                    overflowY: 'auto',
                    background: '#f9f9f9',
                    padding: '15px',
                    borderRadius: '8px',
                    marginBottom: '10px',
                    border: '1px solid #ddd',
                    display: 'flex',
                    flexDirection: 'column'
                }}>
                    {messages
                        .filter(m => m.receiverId === selectedChatUser?.id || m.sender === selectedChatUser?.name)
                        .map((m, i) => {
                            const isMe = m.sender === "Admin";
                            return (
                                <div key={i} style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: isMe ? 'flex-end' : 'flex-start',
                                    marginBottom: '10px'
                                }}>
                                    <small style={{ fontWeight: 'bold', color: '#555', fontSize: '11px' }}>{m.sender}</small>
                                    <div style={{
                                        background: isMe ? '#273c75' : '#ffffff',
                                        color: isMe ? 'white' : '#333',
                                        padding: '8px 12px',
                                        borderRadius: isMe ? '15px 15px 0 15px' : '15px 15px 15px 0',
                                        maxWidth: '85%',
                                        fontSize: '14px',
                                        boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
                                    }}>
                                        {m.content}
                                    </div>
                                </div>
                            );
                        })}
                    <div ref={messagesEndRef} />
                </div>

                <div style={{ display: 'flex', gap: '5px' }}>
                    <input
                        disabled={!selectedChatUser}
                        style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        placeholder={selectedChatUser ? "Reply to user..." : "Select user first"}
                        onKeyDown={(e) => e.key === 'Enter' && sendAdminReply()}
                    />
                    <button
                        disabled={!selectedChatUser}
                        onClick={sendAdminReply}
                        style={{ padding: '10px 15px' }}
                    >
                        Send
                    </button>
                </div>
            </div>
        </div>
    );
}
