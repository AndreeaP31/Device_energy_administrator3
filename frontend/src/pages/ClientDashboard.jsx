// frontend/src/pages/ClientDashboard.jsx
import { useEffect, useState } from "react";
import { getDevicesForUser, getConsumption } from "../api";
import ChartComponent from "../components/ChartComponent";
import SockJS from 'sockjs-client';
import { over } from 'stompjs';

export default function ClientDashboard({ user }) {
    const [devices, setDevices] = useState([]);
    const [selectedDevice, setSelectedDevice] = useState(null);
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]); // Default Azi (format YYYY-MM-DD)
    const [chartData, setChartData] = useState([]);
    useEffect(() => {
        let stompClient = null;

        if (user && user.userId) {
            // Conexiunea se face prin Gateway (8080) către endpoint-ul definit în microserviciu
            const socket = new SockJS('http://localhost/ws-message');
            stompClient = over(socket);

            // Dezactivează log-urile debug dacă sunt prea multe
            stompClient.debug = null;

            stompClient.connect({}, () => {
                console.log('Connected to WebSocket via Gateway');

                // Subscriere la topic-ul de notificări pentru user-ul logat
                stompClient.subscribe(`/topic/notifications/${user.userId}`, (payload) => {
                    const notification = JSON.parse(payload.body);
                    window.alert(`ALERTĂ CONSUM: Dispozitivul ${notification.deviceId} a depășit limita!`);
                });
            }, (error) => {
                console.error('WebSocket Error: ', error);
            });
        }

        // Cleanup: închidem conexiunea când user-ul pleacă de pe pagină
        return () => {
            if (stompClient) stompClient.disconnect();
        };
    }, [user]);

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
        <div className="container">
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
        </div>
    );
}