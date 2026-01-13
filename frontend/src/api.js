const API_BASE = "http://localhost/api";

export async function apiRequest(method, path, body = null) {
    const token = localStorage.getItem("token");

    const res = await fetch(API_BASE + path, {
        method,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body ? JSON.stringify(body) : null,
    });

    const text = await res.text();

    if (!res.ok) {
        // încercăm să dăm un mesaj ok
        throw new Error(text || `HTTP ${res.status}`);
    }

    if (!text) return null;

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

export function login(username, password) {
    return apiRequest("POST", "/auth/login", { username, password });
}

export function registerUser(data) {
    // { name, username, password, role }
    return apiRequest("POST", "/auth/register", data);
}

export function getConsumption(deviceId, dateTimestamp) {
    // dateTimestamp trebuie să fie un long (ex: Date.now())
    return apiRequest("GET", `/monitoring/${deviceId}/history?date=${dateTimestamp}`);
}

export function validateToken() {
    return apiRequest("GET", "/auth/validate");
}

export function getUsers() {
    return apiRequest("GET", "/users");
}

export function updateUser(id, data) {
    // data: { name: "..." }
    return apiRequest("PUT", `/users/${id}`, data);
}

export function deleteUser(id) {
    return apiRequest("DELETE", `/users/${id}`);
}


export function getDevices() {
    return apiRequest("GET", "/device");
}

export function createDevice(data) {
    // { name, maxCons }
    return apiRequest("POST", "/device", data);
}
export function getRelations() {
    return apiRequest("GET", "/device/relations");
}

export function updateDevice(id, data) {
    return apiRequest("PUT", `/device/${id}`, data);
}

export function deleteDevice(id) {
    return apiRequest("DELETE", `/device/${id}`);
}

export function assignDevice(userId, deviceId) {
    return apiRequest("POST", "/device/assign", { userId, deviceId });
}

export function getDevicesForUser(userId) {
    // endpoint pentru client: /device/{userId}/for-user/devices
    return apiRequest("GET", `/device/${userId}/for-user/devices`);
}
