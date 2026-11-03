import { useEffect, useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import Dashboard from "./pages/Dashboard/Dashboard";
import Transactions from "./pages/Transactions/Transactions";
import Accounts from "./pages/Accounts/Accounts";
import Analytics from "./pages/Analytics/Analytics";
import AiInsights from "./pages/AiInsights/AiInsights";
import Login from "./pages/Login/Login";
import Signup from "./pages/Signup/Signup";
import Settings from "./pages/Settings/Settings";

function App() {
  const [token, setToken] = useState(() => localStorage.getItem("token"));

  useEffect(() => {
    const savedTheme = localStorage.getItem("expenseAI.appearance") || "light";
    let active = savedTheme;
    if (savedTheme === "system") {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      active = prefersDark ? "dark" : "light";
    }
    document.documentElement.setAttribute("data-theme", active);
  }, []);

  return (
    <Routes>
      {!token ? (
        <>
          <Route path="/login" element={<Login setToken={setToken} />} />
          <Route path="/signup" element={<Signup setToken={setToken} />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </>
      ) : (
        <>
          <Route path="/" element={<Dashboard />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/accounts" element={<Accounts />} />
          <Route path="/analytics" element={<Analytics />} />
          <Route path="/insights" element={<AiInsights />} />
          <Route path="/roastmode" element={<AiInsights />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </>
      )}
    </Routes>
  );
}

export default App;
