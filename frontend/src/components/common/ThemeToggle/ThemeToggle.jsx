import { useState, useEffect } from "react";
import { FaSun, FaMoon } from "react-icons/fa6";
import styles from "./ThemeToggle.module.css";

function ThemeToggle() {
  const [theme, setTheme] = useState(() => {
    return (
      document.documentElement.getAttribute("data-theme") ||
      localStorage.getItem("expenseAI.appearance") ||
      "light"
    );
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("expenseAI.appearance", theme);
    window.dispatchEvent(new Event("themeChanged"));
  }, [theme]);

  const toggle = () => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  };

  const isDark = theme === "dark";

  return (
    <button
      className={styles.toggleBtn}
      onClick={toggle}
      title={`Switch to ${isDark ? "Light" : "Dark"} Mode`}
      aria-label="Toggle theme"
    >
      {isDark ? (
        <FaSun className={styles.sunIcon} />
      ) : (
        <FaMoon className={styles.moonIcon} />
      )}
    </button>
  );
}

export default ThemeToggle;
