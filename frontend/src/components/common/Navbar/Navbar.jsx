import { useState, useEffect } from "react";
import { FaBell, FaMagnifyingGlass, FaPlus } from "react-icons/fa6";
import ThemeToggle from "../ThemeToggle/ThemeToggle";
import styles from "./Navbar.module.css";

function Navbar({ onAddExpenseClick }) {
  const [userName, setUserName] = useState("Keerthana");
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    const storedName = localStorage.getItem("fullName");
    if (storedName) {
      setUserName(storedName.split(" ")[0]);
    }

    const checkTheme = () => {
      const theme = document.documentElement.getAttribute("data-theme");
      setIsDark(theme === "dark");
    };

    checkTheme();
    window.addEventListener("themeChanged", checkTheme);
    return () => window.removeEventListener("themeChanged", checkTheme);
  }, []);

  return (
    <header className={styles.navbar}>
      {/* Left: Greeting matching Image 2 */}
      <div className={styles.greeting}>
        <h1>
          {isDark ? `Good evening, ${userName}! 🌙` : `Good morning, ${userName}! 👋`}
        </h1>
        <p>Here's what's happening with your money today.</p>
      </div>

      {/* Right: Search, Bell, Theme Toggle, + Add Expense */}
      <div className={styles.actions}>
        <div className={styles.search}>
          <FaMagnifyingGlass className={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search anything..."
            aria-label="Search"
          />
        </div>

        <button className={styles.notificationBtn} title="Notifications">
          <FaBell />
          <span className={styles.dot} />
        </button>

        <ThemeToggle />

        <button
          className={styles.addExpenseBtn}
          onClick={() => {
            if (onAddExpenseClick) {
              onAddExpenseClick();
            } else {
              window.location.href = "/transactions";
            }
          }}
        >
          <FaPlus />
          <span>Add Expense</span>
        </button>
      </div>
    </header>
  );
}

export default Navbar;
