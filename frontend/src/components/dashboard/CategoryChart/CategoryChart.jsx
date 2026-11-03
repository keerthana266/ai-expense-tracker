import { useEffect, useState } from "react";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";
import { Link } from "react-router-dom";
import api from "../../../api/axios";
import styles from "./CategoryChart.module.css";

const CATEGORY_COLORS = {
  "Food & Dining": "#6C5DD3",
  Food: "#6C5DD3",
  Shopping: "#EC4899",
  Transport: "#F97316",
  "Bills & Utilities": "#06B6D4",
  Bills: "#06B6D4",
  Others: "#94A3B8",
};

const PALETTE = ["#6C5DD3", "#EC4899", "#F97316", "#06B6D4", "#94A3B8", "#10B981"];

function CategoryChart({ data: dashboardData }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    api
      .get("/dashboard/category-summary")
      .then((res) => {
        if (!mounted) return;
        if (Array.isArray(res.data) && res.data.length > 0) {
          const total = res.data.reduce((sum, item) => sum + (Number(item[1]) || 0), 0);
          if (total > 0) {
            const parsed = res.data.map((item, idx) => {
              const val = Number(item[1]) || 0;
              return {
                name: String(item[0]),
                value: val,
                color: CATEGORY_COLORS[item[0]] || PALETTE[idx % PALETTE.length],
                percentage: `${((val / total) * 100).toFixed(1)}%`,
              };
            });
            setCategories(parsed);
            setLoading(false);
            return;
          }
        }
        setCategories([]);
        setLoading(false);
      })
      .catch(() => {
        if (mounted) {
          setCategories([]);
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, [dashboardData]);

  const totalValue = categories.reduce((sum, item) => sum + item.value, 0);

  // If no spending yet, show clean zero-state donut
  const chartData =
    categories.length > 0
      ? categories
      : [{ name: "No Expenses Yet", value: 1, color: "var(--border)" }];

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <h3 className={styles.title}>Expense by Category</h3>
      </div>

      {/* Donut Chart with center label */}
      <div className={styles.chartContainer}>
        <ResponsiveContainer width="100%" height={170}>
          <PieChart>
            {categories.length > 0 && (
              <Tooltip
                formatter={(val) => [
                  `₹${Number(val).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`,
                  "Spent",
                ]}
              />
            )}
            <Pie
              data={chartData}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={50}
              outerRadius={75}
              stroke="var(--card)"
              strokeWidth={3}
              paddingAngle={categories.length > 1 ? 2 : 0}
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>

        {/* Center Text inside Donut Hole */}
        <div className={styles.donutCenter}>
          <span className={styles.centerLabel}>Total</span>
          <strong className={styles.centerValue}>
            ₹{Math.round(totalValue).toLocaleString("en-IN")}
          </strong>
        </div>
      </div>

      {/* Category List */}
      <div className={styles.legendList}>
        {categories.length > 0 ? (
          categories.map((cat, idx) => (
            <div key={idx} className={styles.legendItem}>
              <div className={styles.legendLeft}>
                <span className={styles.colorDot} style={{ background: cat.color }} />
                <span className={styles.catName}>{cat.name}</span>
              </div>
              <div className={styles.legendRight}>
                <span className={styles.catAmount}>
                  ₹{cat.value.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                </span>
                <span className={styles.catPercent}>{cat.percentage}</span>
              </div>
            </div>
          ))
        ) : (
          <div style={{ textAlign: "center", padding: "16px 0", color: "var(--text-muted)", fontSize: "12px" }}>
            No category expenses recorded yet.
            <br />
            Add an expense to view breakdown!
          </div>
        )}
      </div>

      {/* Footer Link */}
      <div className={styles.footerLinkWrap}>
        <Link to="/analytics" className={styles.footerLink}>
          View All Analytics →
        </Link>
      </div>
    </div>
  );
}

export default CategoryChart;
