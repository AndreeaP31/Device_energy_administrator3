// frontend/src/pages/ClientDashboard.jsx
import { useEffect, useState , useRef} from "react";
import { getDevicesForUser, getConsumption } from "../api";
import ChartComponent from "../components/ChartComponent";
import SockJS from 'sockjs-client';
import { over } from 'stompjs';

export default function ClientDashboard({ user }) {
    const [devices, setDevices] = useState([]);
    const [selectedDevice, setSelectedDevice] = useState(null);
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]); // Default Azi (format YYYY-MM-DD)
    const [chartData, setChartData] = useState([]);
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [stompClient, setStompClient] = useState(null);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);
    useEffect(() => {
        let stompClient = null;
        const token = localStorage.getItem('token');
        if (user && user.userId) {
            // LOG DE DEBUG: Verifică ce ID are user-ul în browser
            console.log("Subscribing to User ID:", user.userId);

            const socket = new SockJS('http://localhost/ws-message');
            stompClient = over(socket);
            stompClient.debug = null;

            stompClient.connect({'Authorization': 'Bearer ' + token}, () => {
                console.log('Connected. Subscribing to: /topic/notifications/' + user.userId);
                setStompClient(stompClient);
                // Folosește user.userId (care trebuie să fie UUID-ul b09f...)
                stompClient.subscribe(`/topic/notifications/${user.userId}`, (payload) => {
                    console.log("MESAJ PRIMIT PE WEBSOCKET:", payload.body);
                    const notification = JSON.parse(payload.body);
                    window.alert(`ALERTĂ CONSUM: Dispozitivul ${notification.deviceId} a depășit limita!`);
                });
                stompClient.subscribe('/topic/public', (payload) => {
                    const msg = JSON.parse(payload.body);
                    setMessages(prev => [...prev, msg]);
                });
                stompClient.subscribe(`/topic/chat/${user.userId}`, (payload) => {
                    const msg = JSON.parse(payload.body);
                    setMessages(prev => [...prev, msg]);
                });
            }, (error) => {
                console.error('WebSocket Error: ', error);
            });
        }

        return () => {
            if (stompClient) stompClient.disconnect();
        };

    }, [user]);
    const handleSendMessage = () => {
        if (inputValue.trim() && stompClient && stompClient.connected) {
            const chatMsg = {
                sender: user.name || "Client",
                content: inputValue,
                senderId: user.userId, // Foarte important: Adminul are nevoie de acest ID
                timestamp: new Date().toISOString()
            };

            // Trimitem către metoda de mai sus
            stompClient.send("/app/chat.send", {}, JSON.stringify(chatMsg));
            setInputValue("");
        }
    };

    // Restul logic-ului pentru dispozitive și consum rămâne neschimbat
    useEffect(() => {
        if(user && user.userId) {
            getDevicesForUser(user.userId).then(setDevices);
        }
    }, [user]);

    useEffect(() => {
        if(user && user.userId) {
            getDevicesForUser(user.userId).then(setDevices);
        }
    }, [user]);

    useEffect(() => {
        if (selectedDevice && selectedDate) {
            const timestamp = new Date(selectedDate).getTime();
            getConsumption(selectedDevice, timestamp).then(data => {
                if(data) setChartData(data);
                else setChartData([]);
            });
        }
    }, [selectedDevice, selectedDate]);

    return (
        <div className="container" style={{ display: 'flex', gap: '20px', padding: '20px' }}>
            <div className="card">
                <h2>Your Devices</h2>

                {/* LISTA DEVICE-URI*/}
                <div style={{ marginBottom: 20 }}>
                    {devices.map(d => (
                        <button
                            key={d.id}
                            onClick={() => setSelectedDevice(d.id)}
                            style={{
                                margin: "5px",
                                background: selectedDevice === d.id ? "#273c75" : "#eee",
                                color: selectedDevice === d.id ? "white" : "black"
                            }}
                        >
                            {d.name}
                        </button>
                    ))}
                </div>

                {/* CALENDAR SI GRAFIC*/}
                {selectedDevice && (
                    <div>
                        <h3>Select Date</h3>
                        <input
                            type="date"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                            style={{ padding: 8, marginBottom: 20 }}
                        />

                        <h3>Hourly Consumption</h3>
                        {chartData.length > 0 ? (
                            <ChartComponent data={chartData} />
                        ) : (
                            <p>No data for this day.</p>
                        )}
                    </div>
                )}
            </div>
        {/* COLOANA DREAPTĂ: CHATBOT (Rule-based) */}
        <div className="card" style={{ flex: 1, minWidth: '350px', border: '2px solid #273c75', height: 'fit-content' }}>
            <h3 style={{ color: '#273c75' }}>Energy Support Bot</h3>
            <div style={{
                height: '350px',
                overflowY: 'auto',
                background: '#f9f9f9',
                padding: '10px',
                borderRadius: '5px',
                marginBottom: '10px',
                border: '1px solid #ddd'
            }}>
                {messages.map((m, i) => {
                    const isMe=m.sender===user.name;
                    return (
                        <div key={i} style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: isMe ? 'flex-end' : 'flex-start', // Dreapta pentru noi, stânga pentru restul
                            marginBottom: '10px'
                        }}>
                            <small style={{
                                fontWeight: 'bold',
                                color: '#555',
                                marginBottom: '2px',
                                marginRight: isMe ? '5px' : '0',
                                marginLeft: isMe ? '0' : '5px'
                            }}>
                                {m.sender}
                            </small>
                            <div style={{
                                background: isMe ? '#273c75' : '#e3f2fd', // Albastru închis pentru noi, deschis pentru bot
                                color: isMe ? 'white' : '#0d47a1',
                                padding: '10px 15px',
                                borderRadius: isMe ? '15px 15px 0 15px' : '15px 15px 15px 0', // Bule de chat stilizate
                                display: 'inline-block',
                                maxWidth: '85%',
                                fontSize: '14px',
                                boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                                wordBreak: 'break-word'
                            }}>
                                {m.content}
                            </div>
                        </div>
                    );
                })}
            </div>
            <div style={{ display: 'flex', gap: '5px' }}>
                <input
                    style={{ flex: 1, padding: '10px' }}
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    placeholder="Type 'ajutor'..."
                    onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                />
                <button onClick={handleSendMessage}>Send</button>
            </div>
        </div>
        </div>
    );
}