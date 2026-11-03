import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  FaBagShopping,
  FaUtensils,
  FaCar,
  FaVault,
  FaReceipt,
  FaCreditCard,
  FaPlus,
} from "react-icons/fa6";
import api from "../../../api/axios";
import styles from "./TransactionTable.module.css";

function getCategoryIcon(category, type) {
  if (type === "INCOME") return <FaVault />;
  const cat = (category || "").toLowerCase();
  if (cat.includes("food") || cat.includes("swiggy") || cat.includes("zomato") || cat.includes("biryani")) return <FaUtensils />;
  if (cat.includes("shop") || cat.includes("amazon") || cat.includes("flipkart")) return <FaBagShopping />;
  if (cat.includes("transport") || cat.includes("uber") || cat.includes("ola")) return <FaCar />;
  if (cat.includes("cash") || cat.includes("atm")) return <FaCreditCard />;
  return <FaReceipt />;
}

function getIconClass(category, type) {
  if (type === "INCOME") return styles.iconSalary;
  const cat = (category || "").toLowerCase();
  if (cat.includes("food") || cat.includes("swiggy") || cat.includes("zomato") || cat.includes("biryani")) return styles.iconSwiggy;
  if (cat.includes("shop") || cat.includes("amazon")) return styles.iconAmazon;
  if (cat.includes("transport") || cat.includes("uber")) return styles.iconUber;
  return styles.iconDefault;
}

function formatRelativeDate(dateStr) {
  if (!dateStr) return "Today";
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const now = new Date();
    const diffDays = Math.floor((now - d) / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return "Today";
    if (diffDays === 1) return "Yesterday";
    return d.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
  } catch {
    return dateStr;
  }
}

function TransactionTable() {
  const [transactions, setTransactions] = useState([]);
  const [expanded, setExpanded] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    api
      .get("/transactions")
      .then((res) => {
        if (!mounted) return;
        if (Array.isArray(res.data)) {
          const mapped = res.data.map((t) => {
            const isIncome = t.transactionType === "INCOME";
            const amount = Number(t.amount) || 0;
            return {
              id: t.id,
              merchant: t.merchant?.name || t.merchant || (isIncome ? "Salary / Credit" : "Expense"),
              category: t.category?.name || t.category || (isIncome ? "Income" : "General"),
              date: formatRelativeDate(t.transactionTime),
              amount: isIncome ? amount : -amount,
              type: t.transactionType || "EXPENSE",
              icon: getCategoryIcon(t.category?.name || t.category, t.transactionType),
              iconClass: getIconClass(t.category?.name || t.category, t.transactionType),
            };
          });
          setTransactions(mapped);
        } else {
          setTransactions([]);
        }
      })
      .catch(() => {
        if (mounted) setTransactions([]);
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, []);

  const displayedList = expanded ? transactions : transactions.slice(0, 4);

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <h3 className={styles.title}>Recent Transactions</h3>
        <Link to="/transactions" className={styles.viewAllLink}>
          View All
        </Link>
      </div>

      <div className={styles.txList}>
        {displayedList.length > 0 ? (
          displayedList.map((tx) => {
            const isPositive = tx.amount > 0;
            return (
              <div key={tx.id} className={styles.txRow}>
                <div className={styles.left}>
                  <div className={`${styles.iconWrap} ${tx.iconClass}`}>
                    {tx.icon}
                  </div>
                  <div className={styles.info}>
                    <strong className={styles.merchant}>{tx.merchant}</strong>
                    <span className={styles.meta}>
                      {tx.category} • {tx.date}
                    </span>
                  </div>
                </div>
                <strong
                  className={`${styles.amount} ${
                    isPositive ? styles.amountPositive : styles.amountNegative
                  }`}
                >
                  {isPositive ? "+" : "-"}₹
                  {Math.abs(tx.amount).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}
                </strong>
              </div>
            );
          })
        ) : (
          <div style={{ textAlign: "center", padding: "20px 0", color: "var(--text-muted)", fontSize: "12.5px" }}>
            <p style={{ margin: "0 0 10px" }}>No transactions yet.</p>
            <Link
              to="/transactions"
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: "6px",
                background: "var(--primary-light)",
                color: "var(--primary)",
                padding: "6px 12px",
                borderRadius: "8px",
                fontWeight: "600",
                fontSize: "11.5px",
              }}
            >
              <FaPlus /> Add Your First Transaction
            </Link>
          </div>
        )}
      </div>

      {transactions.length > 4 && (
        <button
          className={styles.expandBtn}
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? "Show less ↑" : `View all ${transactions.length} transactions ↓`}
        </button>
      )}
    </div>
  );
}

export default TransactionTable;
