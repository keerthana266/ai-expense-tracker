import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaChartLine, FaEnvelope, FaLock, FaArrowRight, FaWandMagicSparkles } from "react-icons/fa6";
import api from "../../api/axios";
import styles from "./Login.module.css";

function Login({ setToken }) {
  const navigate = useNavigate();

  const [email, setEmail] = useState("demo@expenseai.com");
  const [password, setPassword] = useState("password123");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const cleanEmail = email.trim().toLowerCase();
      const response = await api.post("/auth/login", {
        email: cleanEmail,
        password,
      });

      const token = response.data?.token;
      if (!token) {
        setError("Login successful, but no JWT token was received.");
        return;
      }

      localStorage.setItem("token", token);
      localStorage.setItem("email", cleanEmail);
      if (!localStorage.getItem("fullName")) {
        const defaultName = cleanEmail.split("@")[0];
        const capitalized = defaultName.charAt(0).toUpperCase() + defaultName.slice(1);
        localStorage.setItem("fullName", capitalized || "User");
      }

      if (setToken) {
        setToken(token);
      }

      navigate("/");
    } catch (err) {
      if (err.response) {
        setError(
          err.response.data?.message ||
          err.response.data?.error ||
          `Invalid credentials (${err.response.status}). Use password: password123`
        );
      } else {
        setError("Server not reachable. Please start the Spring Boot backend on port 8080.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleQuickDemo = () => {
    setEmail("demo@expenseai.com");
    setPassword("password123");
    // Trigger login with demo credentials
    setTimeout(() => {
      api.post("/auth/login", {
        email: "demo@expenseai.com",
        password: "password123",
      }).then((res) => {
        if (res.data?.token) {
          localStorage.setItem("token", res.data.token);
          localStorage.setItem("email", "demo@expenseai.com");
          localStorage.setItem("fullName", "Demo User");
          if (setToken) setToken(res.data.token);
          navigate("/");
        }
      }).catch((err) => {
        setError("Make sure backend is running: " + (err.response?.data?.message || err.message));
      });
    }, 100);
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        {/* Brand Header */}
        <div className={styles.brandRow}>
          <div className={styles.logoBadge}>
            <FaChartLine />
          </div>
          <div className={styles.brandText}>
            <h2>ExpenseAI</h2>
            <span>Smart Finance</span>
          </div>
        </div>

        <h1 className={styles.title}>Welcome Back</h1>
        <p className={styles.subtitle}>
          Sign in to view your spending velocity, broke date & AI roasts.
        </p>

        {error && <div className={styles.errorBox}>{error}</div>}

        {/* Quick Demo Login Pill */}
        <button
          type="button"
          onClick={handleQuickDemo}
          className={styles.demoBtn}
        >
          <FaWandMagicSparkles style={{ color: "#F59E0B" }} />
          <span>Quick 1-Click Demo Login</span>
        </button>

        <div className={styles.divider}>
          <span>OR SIGN IN WITH EMAIL</span>
        </div>

        <form onSubmit={handleLogin} className={styles.form}>
          <div className={styles.field}>
            <label>Email Address</label>
            <div className={styles.inputWrap}>
              <FaEnvelope className={styles.fieldIcon} />
              <input
                type="email"
                placeholder="demo@expenseai.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className={styles.field}>
            <label>Password</label>
            <div className={styles.inputWrap}>
              <FaLock className={styles.fieldIcon} />
              <input
                type="password"
                placeholder="password123"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className={styles.submitBtn}>
            <span>{loading ? "Signing In..." : "Sign In to Dashboard"}</span>
            <FaArrowRight />
          </button>
        </form>

        <div className={styles.footer}>
          <span>Don't have an account? </span>
          <Link to="/signup" className={styles.link}>
            Register Now
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
