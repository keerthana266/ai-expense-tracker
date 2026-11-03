import { useState, useEffect } from "react";
import { FaFire } from "react-icons/fa6";
import AiRobot from "../../ui/AiRobot";
import api from "../../../api/axios";
import styles from "./RoastCard.module.css";

function RoastCard({ data, loading }) {
  const [roastText, setRoastText] = useState(
    "✨ Fresh start! Zero spending recorded. Your wallet is at absolute peace. Add an expense or trigger an SMS simulation to test my roasting skills! 🤖"
  );
  const [roasting, setRoasting] = useState(false);

  useEffect(() => {
    if (data?.roast) {
      setRoastText(data.roast);
    } else if (Number(data?.totalExpense || 0) > 0) {
      setRoastText("Biryani 4 times this week? 🫢\nYour wallet is crying more than your diet!");
    } else {
      setRoastText(
        "✨ Fresh start! Zero spending recorded. Your wallet is at absolute peace. Add an expense or trigger an SMS simulation to wake up the roast bot! 🤖"
      );
    }
  }, [data]);

  const handleRoastAgain = async () => {
    try {
      setRoasting(true);
      const res = await api.post("/dashboard/ai-roast", data || {});
      if (res.data) {
        setRoastText(res.data);
      }
    } catch {
      if (Number(data?.totalExpense || 0) > 0) {
        setRoastText("Ordering food again? Your savings account is sending a search and rescue team! 🚨");
      } else {
        setRoastText("Zero expenses detected! You are currently financially un-roastable. Keep it up! 🛡️");
      }
    } finally {
      setRoasting(false);
    }
  };

  const hasExpenses = Number(data?.totalExpense || 0) > 0;

  return (
    <div className={styles.card} id="ai-roast-card">
      <div className={styles.header}>
        <div className={styles.titleWrap}>
          <FaFire className={styles.fireIcon} />
          <h3 className={styles.title}>AI Roastmode</h3>
        </div>
        <span className={styles.badge}>{hasExpenses ? "Getting real! 😈" : "Zero Burn 🕊️"}</span>
      </div>

      <div className={styles.roastBody}>
        <div className={styles.speechBubble}>
          <p className={styles.quoteText}>{roastText}</p>
        </div>
        <div className={styles.robotWrap}>
          <AiRobot size={66} />
        </div>
      </div>

      <button
        className={styles.actionBtn}
        onClick={handleRoastAgain}
        disabled={roasting}
      >
        <span>{roasting ? "Generating Roast..." : "View Roast Insights →"}</span>
      </button>
    </div>
  );
}

export default RoastCard;
