import { useState, useEffect } from "react";
import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";
import {
  FaUser,
  FaPalette,
  FaBell,
  FaShieldHalved,
  FaGaugeHigh,
  FaWhatsapp,
  FaCheck,
  FaSun,
  FaMoon,
  FaLaptop,
} from "react-icons/fa6";
import api from "../../api/axios";
import styles from "./Settings.module.css";

function Settings() {
  const [profile, setProfile] = useState(() => ({
    name: localStorage.getItem("fullName") || "Keerthana",
    email: localStorage.getItem("email") || "keerthana@gmail.com",
    phone: localStorage.getItem("expenseAI.phone") || "+91 98765 43210",
  }));

  const [appearance, setAppearance] = useState(
    () => localStorage.getItem("expenseAI.appearance") || "light"
  );

  const [currency, setCurrency] = useState(
    () => localStorage.getItem("expenseAI.currency") || "INR"
  );

  const [budgetLimit, setBudgetLimit] = useState(
    () => localStorage.getItem("expenseAI.budgetLimit") || "26000"
  );

  const [roastMode, setRoastMode] = useState(
    () => localStorage.getItem("expenseAI.roastMode") || "brutal"
  );

  const [notifications, setNotifications] = useState(() => {
    const saved = localStorage.getItem("expenseAI.notifications");
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {}
    }
    return {
      spendingAlerts: true,
      brokeDateWarnings: true,
      whatsappGuiltTrip: true,
      weeklySummary: true,
    };
  });

  const [savedSuccess, setSavedSuccess] = useState(false);

  const applyTheme = (theme) => {
    let active = theme;
    if (theme === "system") {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      active = prefersDark ? "dark" : "light";
    }
    document.documentElement.setAttribute("data-theme", active);
    window.dispatchEvent(new Event("themeChanged"));
  };

  const handleThemeChange = (newTheme) => {
    setAppearance(newTheme);
    localStorage.setItem("expenseAI.appearance", newTheme);
    applyTheme(newTheme);
  };

  const toggleNotification = (key) => {
    setNotifications((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const handleSave = async (e) => {
    if (e) e.preventDefault();

    localStorage.setItem("fullName", profile.name);
    localStorage.setItem("email", profile.email);
    localStorage.setItem("expenseAI.phone", profile.phone);
    localStorage.setItem("expenseAI.appearance", appearance);
    localStorage.setItem("expenseAI.currency", currency);
    localStorage.setItem("expenseAI.budgetLimit", budgetLimit);
    localStorage.setItem("expenseAI.roastMode", roastMode);
    localStorage.setItem("expenseAI.notifications", JSON.stringify(notifications));

    applyTheme(appearance);

    // Sync budget to backend
    try {
      const num = Number(budgetLimit);
      if (num > 0) {
        await api.post("/budget", {
          amount: num,
          category: "Overall Monthly",
          period: "MONTHLY",
        });
      }
    } catch {}

    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3500);
  };

  return (
    <div className={styles.container}>
      <Sidebar />

      <main className={styles.main}>
        <Navbar />

        <div className={styles.contentWrap}>
          {/* Header */}
          <div className={styles.pageHeader}>
            <div>
              <h1 className={styles.pageTitle}>Account Settings</h1>
              <p className={styles.pageSub}>
                Customize your ExpenseAI preferences, theme, and AI pacing controls.
              </p>
            </div>
            <button className={styles.saveHeaderBtn} onClick={handleSave}>
              <FaCheck />
              <span>Save Changes</span>
            </button>
          </div>

          {savedSuccess && (
            <div className={styles.successToast}>
              <FaCheck />
              <span>All preferences and settings have been saved successfully!</span>
            </div>
          )}

          <div className={styles.settingsGrid}>
            {/* 1. PROFILE SECTION */}
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconBox}>
                  <FaUser />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>Profile Information</h3>
                  <span className={styles.cardSub}>Your personal account details</span>
                </div>
              </div>

              <div className={styles.profileRow}>
                <div className={styles.avatarLarge}>
                  <span>{profile.name.charAt(0).toUpperCase()}</span>
                </div>
                <div className={styles.avatarMeta}>
                  <strong className={styles.profileName}>{profile.name}</strong>
                  <span className={styles.badgePremium}>Premium AI User ✨</span>
                </div>
              </div>

              <div className={styles.inputGrid}>
                <div className={styles.field}>
                  <label>Full Name</label>
                  <input
                    type="text"
                    value={profile.name}
                    onChange={(e) => setProfile({ ...profile, name: e.target.value })}
                    placeholder="Your Name"
                  />
                </div>

                <div className={styles.field}>
                  <label>Email Address</label>
                  <input
                    type="email"
                    value={profile.email}
                    onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                    placeholder="your.email@example.com"
                  />
                </div>

                <div className={styles.field}>
                  <label>WhatsApp Alert Number</label>
                  <input
                    type="text"
                    value={profile.phone}
                    onChange={(e) => setProfile({ ...profile, phone: e.target.value })}
                    placeholder="+91 XXXXX XXXXX"
                  />
                </div>
              </div>
            </section>

            {/* 2. THEME & APPEARANCE */}
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconBox}>
                  <FaPalette />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>Theme & Appearance</h3>
                  <span className={styles.cardSub}>Choose your visual style</span>
                </div>
              </div>

              <div className={styles.themeOptions}>
                <div
                  className={`${styles.themeCard} ${appearance === "light" ? styles.themeActive : ""}`}
                  onClick={() => handleThemeChange("light")}
                >
                  <div className={styles.themePreviewLight}>
                    <FaSun className={styles.sunPreviewIcon} />
                  </div>
                  <div className={styles.themeText}>
                    <strong>Light Mode</strong>
                    <span>Clean & Crisp</span>
                  </div>
                </div>

                <div
                  className={`${styles.themeCard} ${appearance === "dark" ? styles.themeActive : ""}`}
                  onClick={() => handleThemeChange("dark")}
                >
                  <div className={styles.themePreviewDark}>
                    <FaMoon className={styles.moonPreviewIcon} />
                  </div>
                  <div className={styles.themeText}>
                    <strong>Dark Mode</strong>
                    <span>Midnight Navy</span>
                  </div>
                </div>

                <div
                  className={`${styles.themeCard} ${appearance === "system" ? styles.themeActive : ""}`}
                  onClick={() => handleThemeChange("system")}
                >
                  <div className={styles.themePreviewSystem}>
                    <FaLaptop />
                  </div>
                  <div className={styles.themeText}>
                    <strong>System</strong>
                    <span>Device preference</span>
                  </div>
                </div>
              </div>
            </section>

            {/* 3. BROKE DATE & FINANCIAL PACING CONTROLS */}
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconBox}>
                  <FaGaugeHigh />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>Financial Pacer & Broke Date</h3>
                  <span className={styles.cardSub}>Burn rate & safety guardrails</span>
                </div>
              </div>

              <div className={styles.inputGrid}>
                <div className={styles.field}>
                  <label>Monthly Spending Target (₹)</label>
                  <input
                    type="number"
                    value={budgetLimit}
                    onChange={(e) => setBudgetLimit(e.target.value)}
                    placeholder="26000"
                  />
                  <small className={styles.fieldHelp}>
                    Used to calculate your daily Safe Speed Limit (~₹{Math.round(Number(budgetLimit || 26000) / 30)}/day)
                  </small>
                </div>

                <div className={styles.field}>
                  <label>Currency Format</label>
                  <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
                    <option value="INR">Indian Rupee (₹ INR)</option>
                    <option value="USD">US Dollar ($ USD)</option>
                    <option value="EUR">Euro (€ EUR)</option>
                    <option value="GBP">British Pound (£ GBP)</option>
                  </select>
                </div>
              </div>
            </section>

            {/* 4. AI ROAST & WHATSAPP INTERVENTIONS */}
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconBox} style={{ color: "#F97316", background: "rgba(249, 115, 22, 0.12)" }}>
                  <FaWhatsapp />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>AI Roast & WhatsApp Alerts</h3>
                  <span className={styles.cardSub}>Guilt trip & impulsive spending guard</span>
                </div>
              </div>

              <div className={styles.field} style={{ marginBottom: "16px" }}>
                <label>AI Roast Intensity</label>
                <select value={roastMode} onChange={(e) => setRoastMode(e.target.value)}>
                  <option value="brutal">🔥 Brutally Honest (Biryani & impulsive buy callouts)</option>
                  <option value="sarcastic">😏 Sarcastic Financial Coach</option>
                  <option value="gentle">🧘 Gentle Mindful Nudges</option>
                </select>
              </div>

              <div className={styles.checkboxList}>
                <label className={styles.checkboxRow}>
                  <input
                    type="checkbox"
                    checked={notifications.whatsappGuiltTrip}
                    onChange={() => toggleNotification("whatsappGuiltTrip")}
                  />
                  <div>
                    <strong>WhatsApp Guilt-Trip Messages</strong>
                    <span>Sends instant intervention on consecutive food delivery or late-night spending</span>
                  </div>
                </label>

                <label className={styles.checkboxRow}>
                  <input
                    type="checkbox"
                    checked={notifications.brokeDateWarnings}
                    onChange={() => toggleNotification("brokeDateWarnings")}
                  />
                  <div>
                    <strong>Broke Date Runway Alerts</strong>
                    <span>Warn when spending burn rate places zero-balance date within 30 days</span>
                  </div>
                </label>

                <label className={styles.checkboxRow}>
                  <input
                    type="checkbox"
                    checked={notifications.spendingAlerts}
                    onChange={() => toggleNotification("spendingAlerts")}
                  />
                  <div>
                    <strong>Zero Manual Entry Notifications</strong>
                    <span>Auto-parse SMS & UPI notifications in real-time</span>
                  </div>
                </label>
              </div>
            </section>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Settings;
