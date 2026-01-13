
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function ChartComponent({ data }) {
    const formattedData = data.map(item => {
        const date = new Date(item.timestamp);
        return {
            hour: date.getHours() + ":00",
            energy: item.totalConsumption
        };
    });

    return (
        <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
                <BarChart data={formattedData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="hour" />
                    <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
                    <Tooltip />
                    <Bar dataKey="energy" fill="#8884d8" />
                </BarChart>
            </ResponsiveContainer>
        </div>
    );
}