import { useState, useEffect } from "react";
import {
  FaChartPie,
  FaReceipt,
  FaWallet,
  FaChartLine,
  FaSliders,
  FaRobot,
  FaBullseye,
  FaFire,
  FaArrowRightFromBracket,
  FaChevronLeft,
  FaCreditCard,
} from "react-icons/fa6";
import { NavLink } from "react-router-dom";
import AiRobot from "../../ui/AiRobot";
import api from "../../../api/axios";
import styles from "./Sidebar.module.css";

function Sidebar() {
  const [predictionDate, setPredictionDate] = useState("Runway Safe ✨");
  const [collapsed, setCollapsed] = useState(false);
  const fullName = localStorage.getItem("fullName") || "Keerthana";

  useEffect(() => {
    let mounted = true;
    api
      .get("/prediction")
      .then((res) => {
        if (!mounted || !res.data) return;
        const avg = Number(res.data.averageExpense || res.data.averageDailyExpense || 0);
        if (avg <= 0) {
          setPredictionDate("Runway Safe ✨");
          return;
        }
        const dateStr = res.data.brokeDate || res.data.predictedDate;
        if (dateStr) {
          const d = new Date(`${dateStr}T00:00:00`);
          if (!isNaN(d.getTime())) {
            setPredictionDate(
              d.toLocaleDateString("en-IN", {
                day: "numeric",
                month: "short",
                year: "numeric",
              })
            );
          }
        }
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, []);

  const logout = () => {
    localStorage.clear();
    window.location.href = "/login";
  };

  const navClass = ({ isActive }) =>
    `${styles.navItem} ${isActive ? styles.active : ""}`;

  return (
    <aside className={`${styles.sidebar} ${collapsed ? styles.collapsed : ""}`}>
      {/* Top Logo */}
      <div className={styles.logoRow}>
        <div className={styles.logoBrand}>
          <div className={styles.logoBadge}>
            <FaChartLine />
          </div>
          {!collapsed && (
            <div className={styles.brandText}>
              <span className={styles.brandTitle}>Expense</span>
              <span className={styles.brandSub}>AI Tracker</span>
            </div>
          )}
        </div>
        <button
          className={styles.collapseBtn}
          onClick={() => setCollapsed(!collapsed)}
          title="Toggle Sidebar"
          aria-label="Toggle Sidebar"
        >
          <FaChevronLeft style={{ transform: collapsed ? "rotate(180deg)" : "none", transition: "transform 0.2s" }} />
        </button>
      </div>

      {/* Nav links */}
      <nav className={styles.nav}>
        <NavLink to="/" end className={navClass} title="Dashboard">
          <FaChartPie className={styles.navIcon} />
          {!collapsed && <span>Dashboard</span>}
        </NavLink>

        <NavLink to="/transactions" className={navClass} title="Transactions">
          <FaReceipt className={styles.navIcon} />
          {!collapsed && <span>Transactions</span>}
        </NavLink>

        <NavLink to="/accounts" className={navClass} title="Budgets & Wallets">
          <FaWallet className={styles.navIcon} />
          {!collapsed && <span>Budgets</span>}
        </NavLink>

        <NavLink to="/analytics" className={navClass} title="Analytics">
          <FaChartLine className={styles.navIcon} />
          {!collapsed && <span>Analytics</span>}
        </NavLink>

        <NavLink to="/insights" className={navClass} title="AI Insights">
          <FaRobot className={styles.navIcon} />
          {!collapsed && <span>AI Insights</span>}
        </NavLink>

        <NavLink to="/accounts" className={navClass} title="Wallets & Accounts">
          <FaCreditCard className={styles.navIcon} />
          {!collapsed && <span>Wallets</span>}
        </NavLink>

        <a
          href="#goals"
          className={styles.navItem}
          title="Goals"
          onClick={(e) => {
            e.preventDefault();
            alert("🎯 Savings Goals feature is active! Emergency Fund target: ₹50,000.");
          }}
        >
          <FaBullseye className={styles.navIcon} />
          {!collapsed && <span>Goals</span>}
        </a>

        <NavLink to="/insights" className={navClass} title="Roastmode 🤯">
          <FaFire className={styles.navIcon} style={{ color: "#F97316" }} />
          {!collapsed && (
            <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              Roastmode <span style={{ fontSize: "14px" }}>🤯</span>
            </span>
          )}
        </NavLink>

        <NavLink to="/settings" className={navClass} title="Settings">
          <FaSliders className={styles.navIcon} />
          {!collapsed && <span>Settings</span>}
        </NavLink>
      </nav>

      {/* Sidebar Prediction Card */}
      {!collapsed && (
        <div className={styles.predictionCard}>
          <div className={styles.predictionContent}>
            <span className={styles.predMeta}>Next prediction</span>
            <span className={styles.predSub}>Broke forecast</span>
            <strong className={styles.predDate}>{predictionDate}</strong>
            <span className={styles.predAction}>Plan ahead!</span>
          </div>
          <div className={styles.robotGraphic}>
            <AiRobot size={58} />
          </div>
        </div>
      )}

      {/* User profile footer */}
      <div className={styles.userProfile}>
        <div className={styles.avatar}>
          <span>{fullName.charAt(0).toUpperCase()}</span>
        </div>
        {!collapsed && (
          <div className={styles.userInfo}>
            <div className={styles.userName}>{fullName}</div>
            <div className={styles.userBadge}>Premium User ✨</div>
          </div>
        )}
        <button
          className={styles.logoutBtn}
          onClick={logout}
          title="Log out"
        >
          <FaArrowRightFromBracket />
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;
