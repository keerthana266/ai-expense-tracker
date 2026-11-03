import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaChartLine, FaUser, FaEnvelope, FaLock, FaArrowRight } from "react-icons/fa6";
import api from "../../api/axios";
import styles from "./Signup.module.css";

function Signup({ setToken }) {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSignup = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await api.post("/auth/signup", {
        fullName: fullName.trim(),
        email: email.trim().toLowerCase(),
        password: password,
      });

      const token = response.data?.token;
      if (!token) {
        setError("Account created, but no token returned. Please log in.");
        return;
      }

      localStorage.setItem("token", token);
      localStorage.setItem("email", email.trim().toLowerCase());
      localStorage.setItem("fullName", fullName.trim());

      if (setToken) {
        setToken(token);
      }

      navigate("/");
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Registration failed. Please make sure backend is running."
      );
    } finally {
      setLoading(false);
    }
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

        <h1 className={styles.title}>Create Account</h1>
        <p className={styles.subtitle}>
          Start tracking expenses with zero manual entry & AI roasts.
        </p>

        {error && <div className={styles.errorBox}>{error}</div>}

        <form onSubmit={handleSignup} className={styles.form}>
          <div className={styles.field}>
            <label>Full Name</label>
            <div className={styles.inputWrap}>
              <FaUser className={styles.fieldIcon} />
              <input
                type="text"
                placeholder="Alex Morgan"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>
          </div>

          <div className={styles.field}>
            <label>Email Address</label>
            <div className={styles.inputWrap}>
              <FaEnvelope className={styles.fieldIcon} />
              <input
                type="email"
                placeholder="alex@example.com"
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
                placeholder="Create a password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className={styles.submitBtn}>
            <span>{loading ? "Creating Account..." : "Create Account"}</span>
            <FaArrowRight />
          </button>
        </form>

        <div className={styles.footer}>
          <span>Already have an account? </span>
          <Link to="/login" className={styles.link}>
            Log In
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Signup;
