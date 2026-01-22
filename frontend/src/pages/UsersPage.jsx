import { useEffect, useState, useRef } from "react";
import { getUsers, deleteUser, updateUser, registerUser } from "../api";
import SockJS from 'sockjs-client'; // ADĂUGAT
import { over } from 'stompjs';
export default function UsersPage() {
    const [users, setUsers] = useState([]);

    const [editId, setEditId] = useState(null);
    const [editName, setEditName] = useState("");
    const [inputValue, setInputValue] = useState("");
    const [selectedChatUser, setSelectedChatUser] = useState(null);
    const messagesEndRef = useRef(null);
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [newUser, setNewUser] = useState({
        name: "",
        username: "",
        password: ""
    });
    const scrollToBottom = () => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
    };
    useEffect(() => {
        scrollToBottom();
    }, [messages]);
    useEffect(() => {
        async function fetchData() {
            try {
                const data = await getUsers();
                setUsers(data);
            } catch (error) {
                console.error("Failed to load users:", error);
            }
        }

        fetchData();
        const token = localStorage.getItem('token');
        const socket = new SockJS('http://localhost/ws-message');
        const client = over(socket);
        client.debug = null; // oprește log-urile agasante în consolă

        client.connect({'Authorization': 'Bearer ' + token}, () => {
            setStompClient(client);

            // Adminul se abonează la canalul unde vin mesajele cu "ajutor"
            client.subscribe('/topic/admin-messages', (payload) => {
                const msg = JSON.parse(payload.body);
                setMessages(prev => [...prev, msg]);
            });

            // Opțional: Adminul ascultă și pe propriul canal dacă primește mesaje directe
            // client.subscribe('/topic/chat/admin', ...);
        }, (err) => console.error("WS Error Admin:", err));

        return () => {
            if (client) client.disconnect();
        };
    }, []);
    async function load() {
        const data = await getUsers();
        setUsers(data);
    }
    const handleSendMessage = () => {
        if (inputValue.trim() && stompClient && stompClient.connected && selectedChatUser) {
            const chatMsg = {
                sender: "Administrator", // Numele tău
                content: inputValue,
                receiverId: selectedChatUser.id, // ID-ul user-ului pe care ai dat click în listă
                timestamp: new Date().toISOString()
            };

            // Trimitem către canalul privat al user-ului
            stompClient.send("/app/chat.private", {}, JSON.stringify(chatMsg));

            // Adăugăm mesajul în lista locală ca să apară în bulele de chat
            setMessages(prev => [...prev, chatMsg]);
            setInputValue("");
        }
    };





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

            {selectedChatUser && (
                <div className="card" style={{ flex: 1, minWidth: '350px', border: '2px solid #273c75' }}>
                    <h3>Chat cu {selectedChatUser.name}</h3>
                    <div style={{ height: '300px', overflowY: 'auto', background: '#f9f9f9', padding: '10px' }}>
                        {messages
                            .filter(m => m.sender === selectedChatUser.id || m.receiverId === selectedChatUser.id)
                            .map((m, i) => {
                                const isMe = m.sender === "Administrator";
                                return (
                                    <div key={i} style={{ textAlign: isMe ? 'right' : 'left', marginBottom: '10px' }}>
                                        <div style={{
                                            background: isMe ? '#273c75' : '#e3f2fd',
                                            color: isMe ? 'white' : 'black',
                                            padding: '8px 12px',
                                            borderRadius: '10px',
                                            display: 'inline-block'
                                        }}>
                                            {m.content}
                                        </div>
                                    </div>
                                );
                            })}
                        <div ref={messagesEndRef} />
                    </div>
                    <div style={{ display: 'flex', gap: '5px', marginTop: '10px' }}>
                        <input
                            style={{ flex: 1, padding: '10px' }}
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="Scrie un răspuns..."
                            onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                        />
                        <button onClick={handleSendMessage}>Send</button>
                    </div>
                </div>
            )}
        </div>
    );
}
