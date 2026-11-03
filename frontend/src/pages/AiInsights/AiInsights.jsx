import { useState, useEffect } from "react";
import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";
import AiRobot from "../../components/ui/AiRobot";
import {
  FaFire,
  FaGaugeHigh,
  FaBolt,
  FaWhatsapp,
  FaUtensils,
  FaBagShopping,
  FaVault,
  FaMoneyBillWave,
  FaCheck,
  FaPaperPlane,
} from "react-icons/fa6";
import api from "../../api/axios";
import styles from "./AiInsights.module.css";

function AiInsights() {
  const [dashboardData, setDashboardData] = useState(null);
  const [roast, setRoast] = useState(
    "Biryani 4 times this week? 🫢 Your wallet is crying more than your diet!"
  );
  const [roasting, setRoasting] = useState(false);
  const [whatsappToast, setWhatsappToast] = useState(null);

  // Simulator state
  const [burnSlider, setBurnSlider] = useState(450);
  const [customSms, setCustomSms] = useState("");
  const [simLoading, setSimLoading] = useState(false);
  const [simResult, setSimResult] = useState(null);

  const fetchDashboard = async () => {
    try {
      const res = await api.get("/dashboard");
      if (res.data) {
        setDashboardData(res.data);
        if (res.data.roast) setRoast(res.data.roast);
      }
    } catch {}
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const totalBalance = Number(dashboardData?.totalBalance ?? 0);
  const totalExpense = Number(dashboardData?.totalExpense ?? 0);

  // Handle live roast generation
  const handleGenerateRoast = async () => {
    try {
      setRoasting(true);
      const res = await api.post("/dashboard/ai-roast", dashboardData || {});
      if (res.data) setRoast(res.data);
    } catch {
      if (totalExpense > 0) {
        setRoast("Ordering takeout again? Your bank balance is about to file a missing persons report! 🚨");
      } else {
        setRoast("Zero expenses detected! You are financially bulletproof today. 🛡️");
      }
    } finally {
      setRoasting(false);
    }
  };

  // Simulate WhatsApp Guilt-Trip
  const triggerWhatsAppGuiltTrip = () => {
    const text = roast || "Biryani 4 times this week? 🫢 Your wallet is crying more than your diet!";
    setWhatsappToast({
      to: "+91 98765 43210 (Keerthana)",
      message: `🤖 ExpenseAI Roast Alert:\n\n"${text}"\n\nTake a breath before ordering again!`,
    });
    setTimeout(() => setWhatsappToast(null), 6000);
  };

  // Zero Manual Entry simulation
  const handleSimulatePayment = async (text, pkg = "com.google.android.apps.nbu.paisa.user") => {
    try {
      setSimLoading(true);
      setSimResult(null);
      const res = await api.post("/notifications", {
        notificationText: text,
        packageName: pkg,
        timestamp: Date.now(),
      });
      setSimResult({
        type: "success",
        message: res.data || "Notification processed by Gemini AI!",
      });
      fetchDashboard();
    } catch (err) {
      setSimResult({
        type: "error",
        message: err.message || "Failed to process notification.",
      });
    } finally {
      setSimLoading(false);
    }
  };

  // Dynamic runway calculation based on slider
  const balanceForSim = totalBalance > 0 ? totalBalance : 25000;
  const simDays = burnSlider > 0 ? Math.floor(balanceForSim / burnSlider) : 365;
  const simDate = new Date();
  simDate.setDate(simDate.getDate() + Math.min(simDays, 365));

  return (
    <div className={styles.container}>
      <Sidebar />

      <main className={styles.main}>
        <Navbar />

        <div className={styles.contentWrap}>
          {/* Header */}
          <div className={styles.pageHeader}>
            <div>
              <h1 className={styles.pageTitle}>AI Insights & Roastmode 🤯</h1>
              <p className={styles.pageSub}>
                Zero Manual Entry engine, burn velocity pacer, and ruthless guilt-trip bot.
              </p>
            </div>
            <button
              className={styles.roastNowBtn}
              onClick={handleGenerateRoast}
              disabled={roasting}
            >
              <FaFire />
              <span>{roasting ? "Roasting..." : "Generate Fresh Roast 🔥"}</span>
            </button>
          </div>

          {/* WhatsApp Toast Simulation */}
          {whatsappToast && (
            <div className={styles.whatsappCard}>
              <div className={styles.waHeader}>
                <FaWhatsapp className={styles.waIcon} />
                <strong>WhatsApp Guilt-Trip Delivered</strong>
                <span className={styles.waTime}>Just now</span>
              </div>
              <p className={styles.waBody}>{whatsappToast.message}</p>
              <span className={styles.waRecipient}>Recipient: {whatsappToast.to}</span>
            </div>
          )}

          <div className={styles.grid}>
            {/* 1. AI ROASTMODE ARENA */}
            <div className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconFire}>
                  <FaFire />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>AI Roastmode Arena</h3>
                  <span className={styles.cardSub}>Impulsive spend detection & guilt trips</span>
                </div>
              </div>

              <div className={styles.arenaBody}>
                <div className={styles.robotContainer}>
                  <AiRobot size={88} />
                </div>
                <div className={styles.quoteBox}>
                  <p className={styles.roastParagraph}>{roast}</p>
                </div>
              </div>

              <div className={styles.arenaActions}>
                <button
                  className={styles.actionBtnPrimary}
                  onClick={handleGenerateRoast}
                  disabled={roasting}
                >
                  <FaFire />
                  <span>Roast My Spending</span>
                </button>
                <button
                  className={styles.actionBtnWa}
                  onClick={triggerWhatsAppGuiltTrip}
                >
                  <FaWhatsapp />
                  <span>Send WhatsApp Guilt-Trip</span>
                </button>
              </div>
            </div>

            {/* 2. BROKE DATE & RUNWAY SIMULATOR */}
            <div className={styles.card}>
              <div className={styles.cardHeader}>
                <div className={styles.iconPacer}>
                  <FaGaugeHigh />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>Broke Date Pacer Simulator</h3>
                  <span className={styles.cardSub}>Drag daily burn speed to project zero-balance date</span>
                </div>
              </div>

              <div className={styles.pacerSimulation}>
                <div className={styles.pacerDisplay}>
                  <span className={styles.pacerLabel}>Simulated Broke Date:</span>
                  <strong className={styles.pacerDate}>
                    {simDate.toLocaleDateString("en-IN", {
                      day: "numeric",
                      month: "short",
                      year: "numeric",
                    })}
                  </strong>
                  <span className={styles.pacerDays}>
                    ({simDays >= 365 ? "365+ days (1+ Year)" : `${simDays} days`} runway left)
                  </span>
                </div>

                <div className={styles.sliderWrap}>
                  <div className={styles.sliderLabelRow}>
                    <span>Simulated Daily Burn:</span>
                    <strong>₹{burnSlider} / day</strong>
                  </div>
                  <input
                    type="range"
                    min="50"
                    max="3000"
                    step="50"
                    value={burnSlider}
                    onChange={(e) => setBurnSlider(Number(e.target.value))}
                    className={styles.slider}
                  />
                  <div className={styles.sliderTicks}>
                    <span>₹50 (Frugal)</span>
                    <span>₹1,500 (Moderate)</span>
                    <span>₹3,000 (Dangerous)</span>
                  </div>
                </div>
              </div>
            </div>

            {/* 3. ZERO MANUAL ENTRY HUB */}
            <div className={`${styles.card} ${styles.fullWidth}`}>
              <div className={styles.cardHeader}>
                <div className={styles.iconBolt}>
                  <FaBolt />
                </div>
                <div>
                  <h3 className={styles.cardTitle}>Zero Manual Entry: Payment Simulator</h3>
                  <span className={styles.cardSub}>
                    Simulate real-time bank notifications & watch Gemini auto-categorize instantly
                  </span>
                </div>
              </div>

              {simResult && (
                <div
                  className={
                    simResult.type === "success" ? styles.resultSuccess : styles.resultError
                  }
                >
                  <FaCheck /> {simResult.message}
                </div>
              )}

              <div className={styles.simButtonsRow}>
                <button
                  className={styles.simPresetBtn}
                  disabled={simLoading}
                  onClick={() =>
                    handleSimulatePayment(
                      "Paid ₹480 to Meghana Foods for Chicken Biryani via UPI txn 982348 SBI"
                    )
                  }
                >
                  <FaUtensils style={{ color: "#F97316" }} />
                  <span>🍛 Swiggy Biryani ₹480</span>
                </button>

                <button
                  className={styles.simPresetBtn}
                  disabled={simLoading}
                  onClick={() =>
                    handleSimulatePayment(
                      "Paid ₹1,299 to Amazon India for Wireless Earbuds via UPI txn 482912 SBI"
                    )
                  }
                >
                  <FaBagShopping style={{ color: "#CA8A04" }} />
                  <span>🛒 Amazon Order ₹1,299</span>
                </button>

                <button
                  className={styles.simPresetBtn}
                  disabled={simLoading}
                  onClick={() =>
                    handleSimulatePayment(
                      "INR 35,000.00 credited to A/C ...8891 on 03-Nov towards Monthly Salary via NEFT"
                    )
                  }
                >
                  <FaVault style={{ color: "#10B981" }} />
                  <span>💼 Salary Credit +₹35,000</span>
                </button>

                <button
                  className={styles.simPresetBtn}
                  disabled={simLoading}
                  onClick={() =>
                    handleSimulatePayment(
                      "₹2,000 withdrawn from SBI ATM at MG Road. Avl Bal ₹33,000."
                    )
                  }
                >
                  <FaMoneyBillWave style={{ color: "#6C5DD3" }} />
                  <span>🏧 ATM Cash ₹2,000</span>
                </button>
              </div>

              {/* Custom SMS Input */}
              <div className={styles.customSmsBox}>
                <label className={styles.customSmsLabel}>
                  Or Type Any Custom Payment SMS / Notification:
                </label>
                <div className={styles.customInputRow}>
                  <input
                    type="text"
                    value={customSms}
                    onChange={(e) => setCustomSms(e.target.value)}
                    placeholder="e.g. Paid ₹120 to Starbucks Cafe via PhonePe"
                    className={styles.smsInput}
                  />
                  <button
                    className={styles.sendSmsBtn}
                    disabled={simLoading || !customSms.trim()}
                    onClick={() => {
                      handleSimulatePayment(customSms);
                      setCustomSms("");
                    }}
                  >
                    <FaPaperPlane />
                    <span>{simLoading ? "Processing..." : "Process with Gemini"}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default AiInsights;
