import { useEffect, useState, useCallback } from "react";
import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";
import SummaryCards from "../../components/dashboard/SummaryCards/SummaryCards";
import ExpenseChart from "../../components/dashboard/ExpenseChart/ExpenseChart";
import UpcomingBills from "../../components/dashboard/UpcomingBills/UpcomingBills";
import BudgetCard from "../../components/dashboard/BudgetCard/BudgetCard";
import CategoryChart from "../../components/dashboard/CategoryChart/CategoryChart";
import RoastCard from "../../components/dashboard/RoastCard/RoastCard";
import TransactionTable from "../../components/dashboard/TransactionTable/TransactionTable";
import PredictionCard from "../../components/dashboard/PredictionCard/PredictionCard";
import AiQuickActions from "../../components/dashboard/AiQuickActions/AiQuickActions";
import api from "../../api/axios";
import styles from "./Dashboard.module.css";

function Dashboard() {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDashboard = useCallback(async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        window.location.href = "/login";
        return;
      }

      const response = await api.get("/dashboard", { timeout: 15000 });
      if (response.data) {
        setDashboardData(response.data);
      }
      setErrorMessage("");
    } catch (error) {
      console.warn("Dashboard data request note:", error?.response?.status || error.message);
      // Fall back gracefully so dashboard never crashes or kicks the user out
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  return (
    <div className={styles.container}>
      {/* Sidebar with Image 2 Logo, Nav, Robot Broke Prediction, Profile */}
      <Sidebar />

      {/* Main Content Area */}
      <main className={styles.main}>
        {/* Navbar with Greeting, Search, Bell, Theme Toggle, + Add Expense */}
        <Navbar onAddExpenseClick={() => (window.location.href = "/transactions")} />

        {/* Top 4 Summary Cards */}
        <SummaryCards data={dashboardData} loading={loading} />

        {/* 3-COLUMN MAIN GRID (Exact Replica of Image 2) */}
        <div className={styles.mainGrid}>
          {/* LEFT COLUMN: Expenses Overview + (Upcoming Bills & Budget Status) */}
          <div className={styles.colLeft}>
            <ExpenseChart data={dashboardData} loading={loading} />

            <div className={styles.subCardRow}>
              <div className={styles.subCardItem}>
                <UpcomingBills />
              </div>
              <div className={styles.subCardItem}>
                <BudgetCard
                  budget={dashboardData?.budget}
                  loading={loading}
                  onBudgetUpdated={fetchDashboard}
                />
              </div>
            </div>
          </div>

          {/* MIDDLE COLUMN: Expense by Category Donut Chart */}
          <div className={styles.colMiddle}>
            <CategoryChart data={dashboardData} />
          </div>

          {/* RIGHT COLUMN: AI Roastmode + Recent Transactions */}
          <div className={styles.colRight}>
            <RoastCard data={dashboardData} loading={loading} />
            <TransactionTable />
          </div>
        </div>

        {/* LOWER SECTION: Broke Date & Speed Pacer + Zero Manual Entry Quick Actions */}
        <div className={styles.lowerSection} id="ai-insights-section">
          <div className={styles.pacerContainer}>
            <PredictionCard
              prediction={dashboardData?.prediction}
              loading={loading}
            />
          </div>
          <div className={styles.actionsContainer}>
            <AiQuickActions onActionComplete={fetchDashboard} />
          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;
