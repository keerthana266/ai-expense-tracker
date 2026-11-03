import {
  FaGaugeHigh,
  FaTriangleExclamation,
  FaCircleCheck,
  FaArrowTrendDown,
} from "react-icons/fa6";
import styles from "./PredictionCard.module.css";

function PredictionCard({ prediction, loading }) {
  const formatCurrency = (value) => {
    return `₹${Math.round(Number(value || 0)).toLocaleString("en-IN")}`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "No Spend (Clean Start)";
    const date = new Date(`${dateString}T00:00:00`);
    if (Number.isNaN(date.getTime())) return dateString;

    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  const currentBalance = Number(prediction?.currentBalance ?? 0);
  const averageDailyExpense = Number(
    prediction?.averageDailyExpense ?? prediction?.averageExpense ?? 0
  );
  const safeDailyExpense = Number(prediction?.safeDailyExpense ?? 833);
  const spendingSpeedRatio = Number(prediction?.spendingSpeedRatio ?? 0.0);
  const speedStatus = prediction?.speedStatus ?? "OPTIMAL";
  const speedAdvice = prediction?.speedAdvice ?? "";
  const daysLeft = Number(prediction?.daysLeft ?? 0);
  const brokeDate = prediction?.brokeDate ?? prediction?.predictedDate ?? null;
  const advice = prediction?.advice;

  const isSpeeding = speedStatus === "SPEEDING_DANGEROUSLY";
  const isModerate = speedStatus === "MODERATE";

  return (
    <div className={styles.card} id="broke-date-card">
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.titleWrap}>
          <div className={styles.iconBox}>
            <FaGaugeHigh />
          </div>
          <div>
            <h3 className={styles.title}>Broke Date & Speed Pacer</h3>
            <span className={styles.subtitle}>Burn velocity & safe speed runway</span>
          </div>
        </div>

        <span
          className={`${styles.statusBadge} ${
            isSpeeding
              ? styles.badgeSpeeding
              : isModerate
              ? styles.badgeModerate
              : styles.badgeSafe
          }`}
        >
          {isSpeeding ? <FaTriangleExclamation /> : <FaCircleCheck />}
          <span>
            {isSpeeding ? "Slow Down!" : isModerate ? "Moderate Speed" : "Safe Speed"}
          </span>
        </span>
      </div>

      {/* Broke Date Milestone */}
      <div className={styles.brokeRow}>
        <span className={styles.brokeLabel}>Projected Zero-Balance Date:</span>
        <strong
          className={styles.brokeDate}
          style={{ color: isSpeeding ? "#EF4444" : "var(--primary)" }}
        >
          {averageDailyExpense > 0 ? formatDate(brokeDate) : "Clean Start (No Burn)"}
        </strong>
      </div>

      {/* Runway Banner */}
      <div
        className={`${styles.runwayBanner} ${
          isSpeeding ? styles.runwayCritical : styles.runwaySafe
        }`}
      >
        <FaArrowTrendDown />
        <div>
          <strong className={styles.runwayDays}>
            {averageDailyExpense <= 0
              ? "Healthy Runway (Zero Spending Recorded)"
              : daysLeft >= 365
              ? "12+ Months (365+ days) runway remaining"
              : `${daysLeft} days runway remaining`}
          </strong>
          <span className={styles.runwaySub}>
            {isSpeeding
              ? `Spending at ${spendingSpeedRatio.toFixed(1)}x safe speed limit!`
              : averageDailyExpense > 0
              ? "Burn rate is sustainable"
              : "Add transactions to calculate live burn speed"}
          </span>
        </div>
      </div>

      {/* Speed Comparison */}
      <div className={styles.speedBox}>
        <div className={styles.speedLine}>
          <span className={styles.speedLabel}>Current Daily Burn Speed</span>
          <strong className={styles.burnValue}>
            {formatCurrency(averageDailyExpense)} / day
          </strong>
        </div>

        <div className={styles.speedLine}>
          <span className={styles.speedLabel}>Safe Daily Speed Limit</span>
          <strong className={styles.safeValue}>
            {formatCurrency(safeDailyExpense)} / day
          </strong>
        </div>

        {/* Speedometer Bar */}
        <div className={styles.barTrack}>
          <div
            className={styles.barFill}
            style={{
              width: `${Math.min(100, Math.max(averageDailyExpense > 0 ? 10 : 0, spendingSpeedRatio * 60))}%`,
              background: isSpeeding
                ? "linear-gradient(90deg, #F59E0B, #EF4444)"
                : "linear-gradient(90deg, #10B981, #34D399)",
            }}
          />
        </div>
      </div>

      {/* Advice */}
      <div className={styles.adviceBox}>
        <strong>Pacing Advice:</strong>{" "}
        {speedAdvice || advice || "Start adding expenses to activate real-time AI spending pacing."}
      </div>
    </div>
  );
}

export default PredictionCard;
