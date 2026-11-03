import { useState, useId } from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { FaArrowTrendDown, FaChevronDown } from "react-icons/fa6";
import styles from "./ExpenseChart.module.css";

function CustomTooltip({ active, payload, label }) {
  if (active && payload && payload.length) {
    return (
      <div className={styles.tooltipBox}>
        <span className={styles.tooltipLabel}>{label}</span>
        <strong className={styles.tooltipValue}>
          ₹{Number(payload[0].value).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
        </strong>
      </div>
    );
  }
  return null;
}

function ExpenseChart({ data, loading }) {
  const [period, setPeriod] = useState("This Month");
  const gradientId = useId();

  const totalExpense = Number(data?.totalExpense ?? 0);

  // Parse backend monthly expenses or generate clean timeline
  const rawMonthly = Array.isArray(data?.monthlyExpenses) ? data.monthlyExpenses : [];

  let chartData = [];
  if (rawMonthly.length > 0) {
    chartData = rawMonthly.map((item) => ({
      day: String(item[0]),
      amount: Number(item[1]) || 0,
    }));
  } else if (totalExpense > 0) {
    // Distribute current expense across recent dates
    const today = new Date();
    const dayStr = today.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
    chartData = [
      { day: "Start", amount: 0 },
      { day: dayStr, amount: totalExpense },
    ];
  } else {
    // Clean starting state at 0
    chartData = [
      { day: "1st", amount: 0 },
      { day: "10th", amount: 0 },
      { day: "20th", amount: 0 },
      { day: "Today", amount: 0 },
    ];
  }

  return (
    <div className={styles.card}>
      {/* Header */}
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Expenses Overview</h2>
          <div className={styles.statRow}>
            <span className={styles.amount}>
              ₹{totalExpense.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </span>
            <div className={styles.trendDown}>
              <FaArrowTrendDown />
              <span>{totalExpense > 0 ? "Active spending" : "Zero expenses recorded"}</span>
            </div>
          </div>
        </div>

        {/* Period Selector */}
        <div className={styles.periodPill}>
          <span>{period}</span>
          <FaChevronDown style={{ fontSize: "10px" }} />
        </div>
      </div>

      {/* Chart Canvas */}
      <div className={styles.chartWrapper}>
        <ResponsiveContainer width="100%" height={210}>
          <AreaChart data={chartData} margin={{ top: 15, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#8B5CF6" stopOpacity={0.32} />
                <stop offset="100%" stopColor="#8B5CF6" stopOpacity={0.0} />
              </linearGradient>
            </defs>

            <XAxis
              dataKey="day"
              axisLine={false}
              tickLine={false}
              tick={{ fill: "var(--text-muted)", fontSize: 11 }}
              dy={8}
            />
            <YAxis hide domain={["auto", "auto"]} />

            <Tooltip content={<CustomTooltip />} />

            <Area
              type="natural"
              dataKey="amount"
              stroke="#8B5CF6"
              strokeWidth={3}
              fill={`url(#${gradientId})`}
              dot={{
                r: 4.5,
                fill: "#FFFFFF",
                stroke: "#8B5CF6",
                strokeWidth: 2.5,
              }}
              activeDot={{
                r: 7,
                fill: "#8B5CF6",
                stroke: "#FFFFFF",
                strokeWidth: 2,
              }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default ExpenseChart;
