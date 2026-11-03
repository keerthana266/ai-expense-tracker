import {
  FaWallet,
  FaArrowTrendDown,
  FaArrowTrendUp,
  FaVault,
} from "react-icons/fa6";
import styles from "./SummaryCards.module.css";

function SummaryCards({ data, loading }) {
  const formatCurrency = (value) => {
    const num = Number(value || 0);
    return `₹${num.toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  // Real values from backend - starts at 0 for a clean working model
  const totalBalance = Number(data?.totalBalance ?? 0);
  const totalExpense = Number(data?.totalExpense ?? 0);
  const totalIncome = Number(data?.totalIncome ?? 0);
  const savings = Number(data?.savings ?? (totalIncome - totalExpense));

  // Dynamic savings rate
  const savingsRate =
    totalIncome > 0
      ? Math.min(100, Math.max(0, Math.round((savings / totalIncome) * 100)))
      : 0;

  // SVG circular progress calculation
  const radius = 17;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (savingsRate / 100) * circumference;

  return (
    <div className={styles.grid}>
      {/* CARD 1: Total Balance */}
      <div className={styles.card}>
        <div className={styles.cardBody}>
          <span className={styles.cardTitle}>Total Balance</span>
          <h2 className={styles.cardValue}>{formatCurrency(totalBalance)}</h2>
          <div className={`${styles.trend} ${totalBalance >= 0 ? styles.trendUp : styles.trendDown}`}>
            {totalBalance >= 0 ? <FaArrowTrendUp /> : <FaArrowTrendDown />}
            <span>{totalBalance > 0 ? "Liquid funds available" : "Account balance at ₹0"}</span>
          </div>
        </div>
        <div className={`${styles.iconBox} ${styles.iconPurple}`}>
          <FaWallet />
        </div>
      </div>

      {/* CARD 2: Total Expenses */}
      <div className={styles.card}>
        <div className={styles.cardBody}>
          <span className={styles.cardTitle}>Total Expenses</span>
          <h2 className={styles.cardValue}>{formatCurrency(totalExpense)}</h2>
          <div className={`${styles.trend} ${styles.trendDown}`}>
            <FaArrowTrendDown />
            <span>{totalExpense > 0 ? "Recorded spending" : "No expenses yet"}</span>
          </div>
        </div>
        <div className={`${styles.iconBox} ${styles.iconRed}`}>
          <FaArrowTrendDown />
        </div>
      </div>

      {/* CARD 3: Total Income */}
      <div className={styles.card}>
        <div className={styles.cardBody}>
          <span className={styles.cardTitle}>Total Income</span>
          <h2 className={styles.cardValue}>{formatCurrency(totalIncome)}</h2>
          <div className={`${styles.trend} ${styles.trendUp}`}>
            <FaArrowTrendUp />
            <span>{totalIncome > 0 ? "Recorded credits" : "No credits yet"}</span>
          </div>
        </div>
        <div className={`${styles.iconBox} ${styles.iconGreen}`}>
          <FaVault />
        </div>
      </div>

      {/* CARD 4: This Month Savings with Circular Ring */}
      <div className={styles.card}>
        <div className={styles.cardBody}>
          <span className={styles.cardTitle}>This Month Savings</span>
          <h2 className={styles.cardValue}>{formatCurrency(savings)}</h2>
          <div className={styles.savingsMeta}>
            <span>{totalIncome > 0 ? `Target: 25% | Actual: ${savingsRate}%` : "Add income to start saving"}</span>
          </div>
        </div>
        <div className={styles.circularGauge}>
          <svg width="48" height="48" viewBox="0 0 44 44">
            <circle
              className={styles.circleBg}
              cx="22"
              cy="22"
              r={radius}
              strokeWidth="4"
            />
            <circle
              className={styles.circleFill}
              cx="22"
              cy="22"
              r={radius}
              strokeWidth="4"
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              strokeLinecap="round"
            />
          </svg>
          <span className={styles.gaugeText}>{savingsRate}%</span>
        </div>
      </div>
    </div>
  );
}

export default SummaryCards;
