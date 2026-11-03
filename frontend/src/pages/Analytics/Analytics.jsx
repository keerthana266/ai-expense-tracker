import { useEffect, useMemo, useState } from "react";

import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";

import api from "../../api/axios";

import styles from "./Analytics.module.css";

function Analytics() {
  const [accounts, setAccounts] = useState([]);
  const [accountSummary, setAccountSummary] = useState([]);
  const [categorySummary, setCategorySummary] = useState([]);
  const [monthlySummary, setMonthlySummary] = useState([]);

  const [selectedAccount, setSelectedAccount] = useState("ALL");

  const [loading, setLoading] = useState(true);
  const [aiLoading, setAiLoading] = useState(false);

  const [aiInsights, setAiInsights] = useState([]);

  // ============================================================
  // CURRENCY FORMATTERS
  // IMPORTANT:
  // These MUST be declared before any useMemo/useEffect that
  // uses them.
  // ============================================================

  const formatCurrency = (amount) => {
    return `₹${Number(amount || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 0,
    })}`;
  };

  const formatShortCurrency = (amount) => {
    const value = Number(amount || 0);

    if (value >= 100000) {
      return `₹${(value / 100000).toFixed(1)}L`;
    }

    if (value >= 1000) {
      return `₹${(value / 1000).toFixed(1)}K`;
    }

    return `₹${Math.round(value)}`;
  };

  // ============================================================
  // LOAD ANALYTICS DATA
  // ============================================================

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        setLoading(true);

        const [
          accountsResponse,
          summaryResponse,
          categoryResponse,
          monthlyResponse,
        ] = await Promise.all([
          api.get("/accounts"),
          api.get("/dashboard/account-summary"),
          api.get("/dashboard/account-category-summary"),
          api.get("/dashboard/account-monthly-expenses"),
        ]);

        console.log(
          "Analytics accounts:",
          accountsResponse.data
        );

        console.log(
          "Account-wise summary:",
          summaryResponse.data
        );

        console.log(
          "Account category summary:",
          categoryResponse.data
        );

        console.log(
          "Account monthly summary:",
          monthlyResponse.data
        );

        setAccounts(accountsResponse.data || []);
        setAccountSummary(summaryResponse.data || []);
        setCategorySummary(categoryResponse.data || []);
        setMonthlySummary(monthlyResponse.data || []);
      } catch (error) {
        console.error(
          "Failed to load analytics:",
          error
        );
      } finally {
        setLoading(false);
      }
    };

    loadAnalytics();
  }, []);

  // ============================================================
  // ACCOUNT-WISE STATISTICS
  // ============================================================

  const analytics = useMemo(() => {
    let data = accountSummary;

    if (selectedAccount !== "ALL") {
      data = accountSummary.filter(
        (account) =>
          String(account.accountId) ===
          String(selectedAccount)
      );
    }

    const income = data.reduce(
      (total, account) =>
        total + Number(account.income || 0),
      0
    );

    const expense = data.reduce(
      (total, account) =>
        total + Number(account.expense || 0),
      0
    );

    const withdrawal = data.reduce(
      (total, account) =>
        total + Number(account.withdrawal || 0),
      0
    );

    const balance = data.reduce(
      (total, account) =>
        total + Number(account.currentBalance || 0),
      0
    );

    return {
      income,
      expense,
      withdrawal,
      balance,
      accountCount: data.length,
    };
  }, [accountSummary, selectedAccount]);

  // ============================================================
  // CATEGORY ANALYTICS
  // ============================================================

  const categories = useMemo(() => {
    let data = categorySummary;

    if (selectedAccount !== "ALL") {
      data = categorySummary.filter(
        (item) =>
          String(item.accountId) ===
          String(selectedAccount)
      );
    }

    const grouped = {};

    data.forEach((item) => {
      const category =
        item.category || "Others";

      if (!grouped[category]) {
        grouped[category] = 0;
      }

      grouped[category] += Number(
        item.amount || 0
      );
    });

    const total = Object.values(grouped).reduce(
      (sum, amount) =>
        sum + amount,
      0
    );

    return Object.entries(grouped)
      .map(([name, amount]) => ({
        name,
        amount,
        percent:
          total > 0
            ? Math.round(
                (amount / total) * 100
              )
            : 0,
      }))
      .sort(
        (a, b) =>
          b.amount - a.amount
      );
  }, [categorySummary, selectedAccount]);

  // ============================================================
  // DONUT CHART
  // ============================================================

  const donutColors = [
    "#6c5ce7",
    "#f59e0b",
    "#22c55e",
    "#ef4444",
    "#94a3b8",
    "#06b6d4",
    "#ec4899",
  ];

  const donutGradient = useMemo(() => {
    if (categories.length === 0) {
      return "#e5e7eb 0 100%";
    }

    const total = categories.reduce(
      (sum, category) =>
        sum + Number(category.amount || 0),
      0
    );

    if (total === 0) {
      return "#e5e7eb 0 100%";
    }

    let current = 0;

    return categories
      .map((category, index) => {
        const start = current;

        current +=
          (Number(category.amount) /
            total) *
          100;

        const color =
          donutColors[
            index % donutColors.length
          ];

        return `${color} ${start}% ${current}%`;
      })
      .join(", ");
  }, [categories]);

  // ============================================================
  // MONTHLY SPENDING
  // ============================================================

  const months = useMemo(() => {
    const monthOrder = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
    ];

    let data = monthlySummary;

    if (selectedAccount !== "ALL") {
      data = monthlySummary.filter(
        (item) =>
          String(item.accountId) ===
          String(selectedAccount)
      );
    }

    const grouped = {};

    data.forEach((item) => {
      const month =
        item.month || "Unknown";

      if (!grouped[month]) {
        grouped[month] = 0;
      }

      grouped[month] += Number(
        item.amount || 0
      );
    });

    return monthOrder.map((month) => ({
      month,
      amount: grouped[month] || 0,
    }));
  }, [monthlySummary, selectedAccount]);

  // ============================================================
  // MAX MONTHLY EXPENSE
  // ============================================================

  const maxMonthlyExpense = useMemo(() => {
    const maximum = Math.max(
      ...months.map(
        (item) =>
          Number(item.amount || 0)
      )
    );

    return maximum > 0
      ? maximum
      : 1;
  }, [months]);

  // ============================================================
  // SELECTED ACCOUNT
  // ============================================================

  const selectedAccountName = useMemo(() => {
    if (selectedAccount === "ALL") {
      return "All Accounts";
    }

    const account = accounts.find(
      (item) =>
        String(item.id) ===
        String(selectedAccount)
    );

    if (!account) {
      return "Selected Account";
    }

    return account.accountName;
  }, [accounts, selectedAccount]);

  // ============================================================
  // GEMINI AI INSIGHTS
  // ============================================================

  useEffect(() => {
    if (
      loading ||
      accountSummary.length === 0
    ) {
      return;
    }

    let cancelled = false;

    const generateAIInsights = async () => {
      try {
        setAiLoading(true);
        setAiInsights([]);

        // Filter account-wise data
        let selectedAccountData =
          accountSummary;

        if (selectedAccount !== "ALL") {
          selectedAccountData =
            accountSummary.filter(
              (account) =>
                String(account.accountId) ===
                String(selectedAccount)
            );
        }

        // Prepare clean data for Gemini
        const analyticsData = {
          account: selectedAccountName,

          totals: {
            income: analytics.income,
            expense: analytics.expense,
            withdrawal: analytics.withdrawal,
            balance: analytics.balance,
          },

          categories: categories.map(
            (category) => ({
              category: category.name,
              amount: category.amount,
              percentage:
                category.percent,
            })
          ),

          monthlyExpenses: months
            .filter(
              (month) =>
                Number(month.amount || 0) > 0
            )
            .map((month) => ({
              month: month.month,
              amount: month.amount,
            })),

          accounts:
            selectedAccountData.map(
              (account) => ({
                accountId:
                  account.accountId,
                accountName:
                  account.accountName,
                bankName:
                  account.bankName,
                accountType:
                  account.accountType,
                currentBalance:
                  account.currentBalance,
                income:
                  account.income,
                expense:
                  account.expense,
                withdrawal:
                  account.withdrawal,
              })
            ),
        };

        console.log(
          "Sending analytics to Gemini:",
          analyticsData
        );

        const response =
          await api.post(
            "/dashboard/ai-insights",
            analyticsData
          );

        console.log(
          "Gemini raw response:",
          response.data
        );

        if (cancelled) {
          return;
        }

        let rawResponse =
          response.data;

        if (
          typeof rawResponse !==
          "string"
        ) {
          rawResponse =
            JSON.stringify(
              rawResponse
            );
        }

        // Remove markdown code fences
        let cleanedResponse =
          rawResponse
            .replace(
              /```json/gi,
              ""
            )
            .replace(
              /```/g,
              ""
            )
            .trim();

        // Find JSON object if Gemini
        // returned additional text
        const firstBrace =
          cleanedResponse.indexOf(
            "{"
          );

        const lastBrace =
          cleanedResponse.lastIndexOf(
            "}"
          );

        if (
          firstBrace !== -1 &&
          lastBrace !== -1 &&
          lastBrace > firstBrace
        ) {
          cleanedResponse =
            cleanedResponse.slice(
              firstBrace,
              lastBrace + 1
            );
        }

        try {
          const parsed =
            JSON.parse(
              cleanedResponse
            );

          if (
            parsed &&
            Array.isArray(
              parsed.insights
            )
          ) {
            const cleanedInsights =
              parsed.insights
                .slice(0, 3)
                .map(
                  (
                    insight,
                    index
                  ) => ({
                    icon:
                      insight.icon ||
                      [
                        "🍔",
                        "📈",
                        "💰",
                      ][index] ||
                      "🤖",

                    title:
                      insight.title ||
                      "SpendWise AI Insight",

                    message:
                      insight.message ||
                      "Review your spending carefully.",
                  })
                );

            setAiInsights(
              cleanedInsights
            );
          } else {
            setAiInsights([
              {
                icon: "🤖",
                title:
                  "SpendWise AI",
                message:
                  "Gemini returned an unexpected response format.",
              },
            ]);
          }
        } catch (parseError) {
          console.error(
            "Failed to parse Gemini JSON:",
            parseError
          );

          // Gemini fallback response
          setAiInsights([
            {
              icon: "🤖",
              title:
                "SpendWise AI",
              message:
                rawResponse ||
                "AI insights are temporarily unavailable.",
            },
          ]);
        }
      } catch (error) {
        if (!cancelled) {
          console.error(
            "Failed to generate AI insights:",
            error
          );

          setAiInsights([
            {
              icon: "🤖",
              title:
                "AI insights unavailable",
              message:
                "SpendWise could not connect to Gemini right now. Your analytics data is still available above.",
            },
          ]);
        }
      } finally {
        if (!cancelled) {
          setAiLoading(false);
        }
      }
    };

    generateAIInsights();

    return () => {
      cancelled = true;
    };
  }, [
    loading,
    accountSummary,
    categorySummary,
    monthlySummary,
    selectedAccount,
    selectedAccountName,
    analytics,
    categories,
    months,
  ]);

  // ============================================================
  // UI
  // ============================================================

  return (
    <div className={styles.container}>
      <Sidebar />

      <main className={styles.main}>
        <Navbar />

        {/* HEADER */}

        <div className={styles.header}>
          <div>
            <h1>Analytics</h1>

            <p>
              Understand your spending habits
              and financial trends.
            </p>
          </div>

          <div
            style={{
              display: "flex",
              gap: "12px",
            }}
          >
            {/* ACCOUNT SELECTOR */}

            <select
              className={styles.period}
              value={selectedAccount}
              onChange={(e) =>
                setSelectedAccount(
                  e.target.value
                )
              }
            >
              <option value="ALL">
                All Accounts
              </option>

              {accounts.map(
                (account) => (
                  <option
                    key={account.id}
                    value={account.id}
                  >
                    {account.accountName} -{" "}
                    {account.bankName}
                  </option>
                )
              )}
            </select>

            {/* PERIOD */}

            <select
              className={styles.period}
              defaultValue="This Month"
            >
              <option>
                This Month
              </option>

              <option>
                Last Month
              </option>

              <option>
                Last 6 Months
              </option>

              <option>
                This Year
              </option>
            </select>
          </div>
        </div>

        {/* LOADING */}

        {loading && (
          <div
            style={{
              padding: "20px 0",
            }}
          >
            Loading analytics...
          </div>
        )}

        {/* SELECTED ACCOUNT */}

        {!loading &&
          selectedAccount !== "ALL" && (
            <div
              style={{
                marginBottom: "20px",
                padding: "12px 16px",
                borderRadius: "10px",
                background:
                  "rgba(255,255,255,0.04)",
                fontSize: "14px",
              }}
            >
              Showing analytics for{" "}
              <strong>
                {selectedAccountName}
              </strong>
            </div>
          )}

        {/* STATISTICS */}

        <section className={styles.stats}>
          <div
            className={styles.statCard}
          >
            <span>
              Total Spending
            </span>

            <strong>
              {formatCurrency(
                analytics.expense
              )}
            </strong>

            <small
              className={styles.green}
            >
              Account-wise expense
            </small>
          </div>

          <div
            className={styles.statCard}
          >
            <span>
              Total Income
            </span>

            <strong>
              {formatCurrency(
                analytics.income
              )}
            </strong>

            <small>
              Money received
            </small>
          </div>

          <div
            className={styles.statCard}
          >
            <span>
              Withdrawals
            </span>

            <strong>
              {formatCurrency(
                analytics.withdrawal
              )}
            </strong>

            <small>
              Cash withdrawn
            </small>
          </div>

          <div
            className={styles.statCard}
          >
            <span>
              Current Balance
            </span>

            <strong>
              {formatCurrency(
                analytics.balance
              )}
            </strong>

            <small>
              Available balance
            </small>
          </div>
        </section>

        {/* MAIN GRID */}

        <section className={styles.grid}>
          {/* SPENDING TREND */}

          <div
            className={styles.chartCard}
          >
            <div
              className={
                styles.cardHeader
              }
            >
              <div>
                <h2>
                  Spending Trend
                </h2>

                <p>
                  Monthly expense movement
                </p>
              </div>

              <span
                className={
                  styles.badge
                }
              >
                2026
              </span>
            </div>

            <div
              className={styles.chart}
            >
              <div
                className={
                  styles.yAxis
                }
              >
                <span>
                  {formatShortCurrency(
                    maxMonthlyExpense
                  )}
                </span>

                <span>
                  {formatShortCurrency(
                    maxMonthlyExpense *
                      0.75
                  )}
                </span>

                <span>
                  {formatShortCurrency(
                    maxMonthlyExpense *
                      0.5
                  )}
                </span>

                <span>
                  {formatShortCurrency(
                    maxMonthlyExpense *
                      0.25
                  )}
                </span>

                <span>
                  ₹0
                </span>
              </div>

              <div
                className={
                  styles.chartArea
                }
              >
                <div
                  className={
                    styles.gridLine
                  }
                />

                <div
                  className={
                    styles.gridLine
                  }
                />

                <div
                  className={
                    styles.gridLine
                  }
                />

                <div
                  className={
                    styles.gridLine
                  }
                />

                <div
                  className={styles.bars}
                >
                  {months.map(
                    (item) => {
                      const height =
                        item.amount > 0
                          ? Math.max(
                              8,
                              (item.amount /
                                maxMonthlyExpense) *
                                100
                            )
                          : 0;

                      return (
                        <div
                          className={
                            styles.barGroup
                          }
                          key={
                            item.month
                          }
                        >
                          <div
                            className={
                              styles.bar
                            }
                            style={{
                              height: `${height}%`,
                            }}
                            title={`${item.month}: ${formatCurrency(
                              item.amount
                            )}`}
                          />

                          <span>
                            {
                              item.month
                            }
                          </span>
                        </div>
                      );
                    }
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* CATEGORY BREAKDOWN */}

          <div
            className={
              styles.categoryCard
            }
          >
            <div
              className={
                styles.cardHeader
              }
            >
              <div>
                <h2>
                  Spending by Category
                </h2>

                <p>
                  Where your money goes
                </p>
              </div>
            </div>

            <div
              className={
                styles.donutArea
              }
            >
              <div
                className={
                  styles.donut
                }
                style={{
                  background: `conic-gradient(${donutGradient})`,
                }}
              >
                <div>
                  <strong>
                    {formatCurrency(
                      analytics.expense
                    )}
                  </strong>

                  <span>
                    Total
                  </span>
                </div>
              </div>
            </div>

            <div
              className={
                styles.categories
              }
            >
              {categories.length ===
              0 ? (
                <div
                  style={{
                    textAlign:
                      "center",
                    padding:
                      "15px",
                    color:
                      "#929bad",
                    fontSize:
                      "12px",
                  }}
                >
                  No expense data
                  available
                </div>
              ) : (
                categories.map(
                  (category) => (
                    <div
                      className={
                        styles.category
                      }
                      key={
                        category.name
                      }
                    >
                      <div>
                        <span
                          className={
                            styles.dot
                          }
                        />

                        <span>
                          {
                            category.name
                          }
                        </span>
                      </div>

                      <strong>
                        {formatCurrency(
                          category.amount
                        )}
                      </strong>

                      <small>
                        {
                          category.percent
                        }
                        %
                      </small>
                    </div>
                  )
                )
              )}
            </div>
          </div>
        </section>

        {/* AI INSIGHTS */}

        <section
          className={styles.insights}
        >
          <div
            className={
              styles.insightHeader
            }
          >
            <div>
              <h2>
                AI Spending Insights
              </h2>

              <p>
                Personalized observations
                from SpendWise AI
              </p>
            </div>

            <span
              className={
                styles.aiBadge
              }
            >
              AI
            </span>
          </div>

          <div
            className={
              styles.insightGrid
            }
          >
            {aiLoading ? (
              <div
                style={{
                  gridColumn:
                    "1 / -1",
                  padding: "20px",
                  textAlign:
                    "center",
                  opacity: 0.7,
                }}
              >
                🤖 SpendWise AI is
                analysing your spending...
              </div>
            ) : aiInsights.length ===
              0 ? (
              <div
                style={{
                  gridColumn:
                    "1 / -1",
                  padding: "20px",
                  textAlign:
                    "center",
                  opacity: 0.7,
                }}
              >
                AI insights will appear
                here after your analytics
                are loaded.
              </div>
            ) : (
              aiInsights.map(
                (insight, index) => (
                  <div
                    className={
                      styles.insight
                    }
                    key={index}
                  >
                    <div
                      className={
                        styles.insightIcon
                      }
                    >
                      {insight.icon}
                    </div>

                    <div>
                      <h3>
                        {
                          insight.title
                        }
                      </h3>

                      <p>
                        {
                          insight.message
                        }
                      </p>
                    </div>
                  </div>
                )
              )
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default Analytics;