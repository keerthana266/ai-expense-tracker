import { useState } from "react";
import {
  FaBolt,
  FaArrowTrendDown,
  FaArrowTrendUp,
  FaFire,
  FaMoneyBillWave,
  FaWhatsapp,
  FaChevronDown,
  FaChevronUp,
} from "react-icons/fa6";
import api from "../../../api/axios";
import styles from "./AiQuickActions.module.css";

function AiQuickActions({ onActionComplete }) {
  const [isOpen, setIsOpen] = useState(false);
  const [cashText, setCashText] = useState("");
  const [loadingAction, setLoadingAction] = useState("");
  const [alertBanner, setAlertBanner] = useState(null);

  // 1. Autonomous Zero Manual Entry: Debit / Expense Message
  const simulateExpenseDebit = async () => {
    try {
      setLoadingAction("expense");
      setAlertBanner(null);
      const randomId = Math.floor(100000 + Math.random() * 900000);
      await api.post("/notifications", {
        notificationText: `Paid ₹480 to Starbucks Cafe via UPI transaction ID ${randomId} from SBI A/C`,
        packageName: "com.google.android.apps.nbu.paisa.user",
        timestamp: Date.now(),
      });

      setAlertBanner({
        type: "success",
        title: "📲 Payment Debit Auto-Categorized!",
        message: "Gemini parsed: ₹480 at Starbucks. Balance & Expenses updated live!",
      });

      if (onActionComplete) onActionComplete();
    } catch (err) {
      setAlertBanner({
        type: "error",
        title: "Action Failed",
        message: err.message || "Failed to process expense.",
      });
    } finally {
      setLoadingAction("");
    }
  };

  // 2. Autonomous Zero Manual Entry: Credit / Income Message (Salary, Refund, Cashback)
  const simulateIncomeCredit = async () => {
    try {
      setLoadingAction("income");
      setAlertBanner(null);
      await api.post("/notifications", {
        notificationText:
          "INR 25,000.00 credited to your A/C ...8891 on 02-Nov-26 towards Salary from TechCorp via NEFT",
        packageName: "com.google.android.apps.messaging",
        timestamp: Date.now(),
      });

      setAlertBanner({
        type: "income",
        title: "💰 Income Credit Auto-Processed!",
        message: "₹25,000 Salary Credit! Income, Savings, and Runway extended!",
      });

      if (onActionComplete) onActionComplete();
    } catch (err) {
      setAlertBanner({
        type: "error",
        title: "Income Failed",
        message: err.message || "Failed to process income credit.",
      });
    } finally {
      setLoadingAction("");
    }
  };

  // 3. AI Financial Intervention: Speeding Nudge / Guilt Trip ("Slow Down!")
  const triggerSpeedIntervention = async () => {
    try {
      setLoadingAction("speed");
      setAlertBanner(null);
      const res = await api.post("/whatsapp/test-roast?item=Shopping");
      const roastText =
        res.data?.roast ||
        "🚨 Speeding Alert: You are burning cash at 2.4x your safe speed limit! Slow down your spending before you hit your broke date! 🛑📉";

      setAlertBanner({
        type: "roast",
        title: "⚠️ Financial Velocity Intervention Dispatched!",
        message: roastText,
      });

      if (onActionComplete) onActionComplete();
    } catch (err) {
      setAlertBanner({
        type: "error",
        title: "Intervention Error",
        message: err.message || "Could not dispatch intervention.",
      });
    } finally {
      setLoadingAction("");
    }
  };

  // 4. Liquid Cash: Simulate ATM Withdrawal (Bank -> Cash Wallet)
  const simulateAtmWithdrawal = async () => {
    try {
      setLoadingAction("atm");
      setAlertBanner(null);
      await api.post("/notifications", {
        notificationText:
          "Rs 5000.00 debited from SBI A/C ...8891 at ATM CASH WDL on " +
          new Date().toLocaleDateString("en-GB"),
        packageName: "com.google.android.apps.messaging",
        timestamp: Date.now(),
      });

      setAlertBanner({
        type: "cash",
        title: "💵 ATM Withdrawal Auto-Allocated!",
        message: "₹5,000 moved to Cash Wallet without distorting expenses!",
      });

      if (onActionComplete) onActionComplete();
    } catch (err) {
      setAlertBanner({
        type: "error",
        title: "ATM Error",
        message: err.message || "Could not process ATM withdrawal.",
      });
    } finally {
      setLoadingAction("");
    }
  };

  // 5. Liquid Cash: WhatsApp Casual Text / Voice Note Spend
  const handleCashSpend = async (e) => {
    e.preventDefault();
    if (!cashText.trim()) return;

    try {
      setLoadingAction("cashSpend");
      setAlertBanner(null);
      const res = await api.post("/whatsapp/webhook", {
        message: cashText.trim(),
        from: "+919876543210",
      });

      const reply =
        res.data?.reply ||
        `Logged cash spend: "${cashText}" and deducted from your Cash Wallet!`;

      setAlertBanner({
        type: "success",
        title: "💬 WhatsApp Cash Spend Logged!",
        message: reply,
      });

      setCashText("");
      if (onActionComplete) onActionComplete();
    } catch (err) {
      setAlertBanner({
        type: "error",
        title: "Cash Log Error",
        message: err.message || "Could not log cash spend.",
      });
    } finally {
      setLoadingAction("");
    }
  };

  return (
    <div className={styles.compactContainer}>
      {/* COMPACT TOGGLE BAR */}
      <div className={styles.compactHeader} onClick={() => setIsOpen(!isOpen)}>
        <div className={styles.headerLeft}>
          <div className={styles.pulseDot} />
          <span className={styles.compactBadge}>
            <FaBolt /> AI Actions & Testing Tools
          </span>
          <span className={styles.compactSubtext}>
            {isOpen ? "Click to collapse" : "Click to simulate payment alerts or log cash spends"}
          </span>
        </div>

        <button type="button" className={styles.toggleBtn}>
          {isOpen ? <FaChevronUp /> : <FaChevronDown />}
        </button>
      </div>

      {/* EXPANDABLE BODY */}
      {isOpen && (
        <div className={styles.expandedBody}>
          {alertBanner && (
            <div
              className={`${styles.alert} ${
                alertBanner.type === "roast"
                  ? styles.alertRoast
                  : alertBanner.type === "income"
                  ? styles.alertIncome
                  : alertBanner.type === "cash"
                  ? styles.alertCash
                  : alertBanner.type === "error"
                  ? styles.alertError
                  : styles.alertSuccess
              }`}
            >
              <strong>{alertBanner.title}</strong>
              <p>{alertBanner.message}</p>
            </div>
          )}

          <div className={styles.buttonsRow}>
            <button
              type="button"
              className={styles.actionBtn}
              onClick={simulateExpenseDebit}
              disabled={!!loadingAction}
            >
              <FaArrowTrendDown className={styles.iconRed} />
              <span>
                {loadingAction === "expense"
                  ? "Parsing..."
                  : "Debit: ₹480 Starbucks (UPI)"}
              </span>
            </button>

            <button
              type="button"
              className={styles.actionBtn}
              onClick={simulateIncomeCredit}
              disabled={!!loadingAction}
            >
              <FaArrowTrendUp className={styles.iconGreen} />
              <span>
                {loadingAction === "income"
                  ? "Crediting..."
                  : "Credit: +₹25,000 Salary"}
              </span>
            </button>

            <button
              type="button"
              className={styles.actionBtn}
              onClick={triggerSpeedIntervention}
              disabled={!!loadingAction}
            >
              <FaFire className={styles.iconOrange} />
              <span>
                {loadingAction === "speed"
                  ? "Analyzing..."
                  : "Speeding Warning (Slow Down!)"}
              </span>
            </button>

            <button
              type="button"
              className={styles.actionBtn}
              onClick={simulateAtmWithdrawal}
              disabled={!!loadingAction}
            >
              <FaMoneyBillWave className={styles.iconBlue} />
              <span>
                {loadingAction === "atm"
                  ? "Withdrawing..."
                  : "ATM ₹5,000 → Cash Wallet"}
              </span>
            </button>
          </div>

          {/* Quick Cash Input */}
          <form onSubmit={handleCashSpend} className={styles.cashForm}>
            <div className={styles.cashInputWrapper}>
              <FaWhatsapp className={styles.whatsappIcon} />
              <input
                type="text"
                className={styles.cashInput}
                placeholder="Log physical cash (e.g. 'chai 30 cash', 'auto 60', 'groceries 200')..."
                value={cashText}
                onChange={(e) => setCashText(e.target.value)}
                disabled={loadingAction === "cashSpend"}
              />
            </div>
            <button
              type="submit"
              className={styles.cashSubmitBtn}
              disabled={!cashText.trim() || loadingAction === "cashSpend"}
            >
              {loadingAction === "cashSpend" ? "Logging..." : "Log Cash Spend"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default AiQuickActions;
