export default function Navbar({ user, logout }) {
    return (
        <nav
            style={{
                background: "#273c75",
                padding: "14px 0",
                color: "white",
                marginBottom: "10px",
            }}
        >
            <div
                className="container"
                style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                }}
            >
                <div style={{ fontWeight: 600, fontSize: 20 }}>Energy Management</div>

                {user ? (
                    <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                        <div style={{ fontSize: 14 }}>
                            {user.username} <span style={{ opacity: 0.8 }}>({user.role})</span>
                        </div>
                        <button className="danger" onClick={logout}>
                            Logout
                        </button>
                    </div>
                ) : (
                    <div style={{ fontSize: 14, opacity: 0.9 }}>
                        Please log in or register
                    </div>
                )}
            </div>
        </nav>
    );
}
