import { useState } from "react";
import { FaPenToSquare, FaCheck } from "react-icons/fa6";
import api from "../../../api/axios";
import styles from "./BudgetCard.module.css";

function BudgetCard({ budget, loading, onBudgetUpdated }) {
  const [isEditing, setIsEditing] = useState(false);
  const [customAmount, setCustomAmount] = useState("");
  const [saving, setSaving] = useState(false);

  // Spent and budget - starts at 0 for a clean working model
  const spent = Number(budget?.spentAmount ?? budget?.spent ?? 0);
  const rawBudget = Number(budget?.budgetAmount ?? budget?.budget ?? 25000);
  const budgetAmount = rawBudget > 0 ? rawBudget : 25000;
  const percentage = budgetAmount > 0 ? Math.min(100, Math.round((spent / budgetAmount) * 100)) : 0;

  const handleSaveBudget = async (e) => {
    e.preventDefault();
    const num = Number(customAmount);
    if (!num || num <= 0) return;

    try {
      setSaving(true);
      await api.post("/budget", {
        amount: num,
        category: "Overall Monthly",
        period: "MONTHLY",
      });
      setIsEditing(false);
      if (onBudgetUpdated) onBudgetUpdated();
    } catch (err) {
      console.error("Failed to update budget:", err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div className={styles.titleWrap}>
          <h3 className={styles.title}>Budget Status</h3>
          <span className={styles.subtitle}>
            {percentage > 90
              ? "Approaching limit! ⚠️"
              : percentage > 0
              ? "You're doing great! 🎉"
              : "Zero budget used ✨"}
          </span>
        </div>
        <button
          className={styles.editBtn}
          onClick={() => {
            setIsEditing(!isEditing);
            setCustomAmount(String(budgetAmount));
          }}
          title="Adjust Monthly Budget"
        >
          <FaPenToSquare />
        </button>
      </div>

      {isEditing ? (
        <form onSubmit={handleSaveBudget} className={styles.editForm}>
          <input
            type="number"
            value={customAmount}
            onChange={(e) => setCustomAmount(e.target.value)}
            placeholder="Budget limit"
            className={styles.budgetInput}
            autoFocus
          />
          <button type="submit" disabled={saving} className={styles.saveBtn}>
            <FaCheck />
          </button>
        </form>
      ) : (
        <div className={styles.contentRow}>
          <div className={styles.percentageWrap}>
            <span className={styles.largePercent}>{percentage}%</span>
            <span className={styles.percentLabel}>of budget used</span>
          </div>

          {/* Ascending Green Trend Sparkline */}
          <div className={styles.trendGraphic}>
            <svg width="84" height="42" viewBox="0 0 84 42" fill="none">
              <path
                d="M 4 36 Q 24 34 38 22 T 76 6"
                stroke="#10B981"
                strokeWidth="2.5"
                strokeLinecap="round"
                fill="none"
              />
              <polyline
                points="68,6 76,6 76,14"
                stroke="#10B981"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                fill="none"
              />
            </svg>
          </div>
        </div>
      )}
    </div>
  );
}

export default BudgetCard;
