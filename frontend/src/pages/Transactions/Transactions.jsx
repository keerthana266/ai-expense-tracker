import { useEffect, useState } from "react";

import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";

import api from "../../api/axios";

import styles from "./Transactions.module.css";

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [accounts, setAccounts] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    accountId: "",
    amount: "",
    merchant: "",
    category: "",
    paymentMethod: "UPI",
    transactionType: "EXPENSE",
  });

  // =========================================================
  // LOAD TRANSACTIONS + ACCOUNTS
  // =========================================================

  useEffect(() => {
    fetchTransactions();
    fetchAccounts();
  }, []);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/transactions");

      setTransactions(response.data);
    } catch (err) {
      console.error("Failed to fetch transactions:", err);

      setError("Unable to load transactions.");
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await api.get("/accounts");

      console.log("ACCOUNTS RECEIVED:", response.data);

      setAccounts(response.data);

      // Automatically select primary account
      const primaryAccount = response.data.find(
        (account) => account.primaryAccount === true
      );

      if (primaryAccount) {
        setForm((previous) => ({
          ...previous,
          accountId: String(primaryAccount.id),
        }));
      }
    } catch (err) {
      console.error("Failed to fetch accounts:", err);
    }
  };

  // =========================================================
  // FORM CHANGE
  // =========================================================

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === "transactionType") {
      if (value === "WITHDRAWAL") {
        setForm((previous) => ({
          ...previous,
          transactionType: value,
          category: "Cash Withdrawal",
          merchant: previous.merchant || "ATM Withdrawal",
        }));
        return;
      }

      setForm((previous) => ({
        ...previous,
        transactionType: value,
        category: "",
        merchant: "",
      }));

      return;
    }

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  // =========================================================
  // ADD TRANSACTION
  // =========================================================

  const handleAddTransaction = async (e) => {
    e.preventDefault();

    if (!form.accountId) {
      alert("Please select an account.");
      return;
    }

    if (!form.amount || !form.merchant || !form.category) {
      alert("Please fill Amount, Merchant and Category.");
      return;
    }

    if (Number(form.amount) <= 0) {
      alert("Amount must be greater than 0.");
      return;
    }

    try {
      setSaving(true);
      setError("");

      const payload = {
        accountId: Number(form.accountId),
        amount: Number(form.amount),
        merchant: form.merchant,
        category: form.category,
        paymentMethod: form.paymentMethod,
        transactionType: form.transactionType,
      };

      // Withdrawal defaults
      if (form.transactionType === "WITHDRAWAL") {
        payload.merchant =
          form.merchant.trim() || "ATM Withdrawal";

        payload.category =
          form.category.trim() || "Cash Withdrawal";
      }

      console.log("ADDING TRANSACTION:", payload);

      await api.post("/transactions", payload);

      console.log("TRANSACTION ADDED SUCCESSFULLY");

      // Reset form
      const primaryAccount = accounts.find(
        (account) => account.primaryAccount === true
      );

      setForm({
        accountId: primaryAccount
          ? String(primaryAccount.id)
          : "",
        amount: "",
        merchant: "",
        category: "",
        paymentMethod: "UPI",
        transactionType: "EXPENSE",
      });

      setShowForm(false);

      await fetchTransactions();

      // Refresh accounts because balances changed
      await fetchAccounts();
    } catch (err) {
      console.error(
        "Failed to add transaction:",
        err
      );

      console.error(
        "SERVER RESPONSE:",
        err.response?.data
      );

      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Unable to add transaction."
      );
    } finally {
      setSaving(false);
    }
  };

  // =========================================================
  // SUMMARY
  // =========================================================

  const totalIncome = transactions
    .filter(
      (transaction) =>
        transaction.transactionType === "INCOME"
    )
    .reduce(
      (total, transaction) =>
        total +
        Number(transaction.amount || 0),
      0
    );

  const totalExpenses = transactions
    .filter(
      (transaction) =>
        transaction.transactionType === "EXPENSE"
    )
    .reduce(
      (total, transaction) =>
        total +
        Number(transaction.amount || 0),
      0
    );

  const totalWithdrawals = transactions
    .filter(
      (transaction) =>
        transaction.transactionType === "WITHDRAWAL"
    )
    .reduce(
      (total, transaction) =>
        total +
        Number(transaction.amount || 0),
      0
    );

  const currentMonth =
    new Date().getMonth();

  const currentYear =
    new Date().getFullYear();

  const thisMonth = transactions
    .filter((transaction) => {
      if (!transaction.transactionTime) {
        return false;
      }

      const date =
        new Date(
          transaction.transactionTime
        );

      return (
        date.getMonth() ===
          currentMonth &&
        date.getFullYear() ===
          currentYear
      );
    })
    .reduce(
      (total, transaction) =>
        total +
        Number(transaction.amount || 0),
      0
    );

  // Prevent unused-variable warning if this value
  // is not currently displayed.
  void thisMonth;

  // =========================================================
  // RENDER
  // =========================================================

  return (
    <div className={styles.container}>

      <Sidebar />

      <main className={styles.main}>

        <Navbar />

        {/* ================================================= */}
        {/* HEADER */}
        {/* ================================================= */}

        <div className={styles.header}>

          <div>

            <h1>Transactions</h1>

            <p>
              Track and manage all your money activity.
            </p>

          </div>

          <button
            className={styles.addButton}
            onClick={() =>
              setShowForm(true)
            }
          >
            + Add Transaction
          </button>

        </div>

        {/* ================================================= */}
        {/* ADD TRANSACTION FORM */}
        {/* ================================================= */}

        {showForm && (

          <section
            className={styles.tableCard}
          >

            <div
              className={styles.tableHeader}
            >

              <div>

                <h2>
                  Add Transaction
                </h2>

                <p>
                  Enter your transaction details
                </p>

              </div>

            </div>

            <form
              onSubmit={
                handleAddTransaction
              }
            >

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns:
                    "repeat(auto-fit, minmax(200px, 1fr))",
                  gap: "16px",
                  padding: "20px",
                }}
              >

                {/* ACCOUNT */}

                <select
                  name="accountId"
                  value={form.accountId}
                  onChange={handleChange}
                  required
                >

                  <option value="">
                    Select Account
                  </option>

                  {accounts.map(
                    (account) => (

                      <option
                        key={account.id}
                        value={account.id}
                      >
                        {account.accountName}
                        {" — ₹"}
                        {Number(
                          account.currentBalance || 0
                        ).toLocaleString()}
                      </option>

                    )
                  )}

                </select>

                {/* AMOUNT */}

                <input
                  type="number"
                  name="amount"
                  placeholder="Amount"
                  value={form.amount}
                  onChange={handleChange}
                  min="0.01"
                  step="0.01"
                  required
                />

                {/* MERCHANT */}

                <input
                  type="text"
                  name="merchant"
                  placeholder={
                    form.transactionType ===
                    "WITHDRAWAL"
                      ? "Merchant / ATM"
                      : "Merchant"
                  }
                  value={form.merchant}
                  onChange={handleChange}
                  required
                />

                {/* CATEGORY */}

                <input
                  type="text"
                  name="category"
                  placeholder={
                    form.transactionType ===
                    "WITHDRAWAL"
                      ? "Cash Withdrawal"
                      : "Category"
                  }
                  value={form.category}
                  onChange={handleChange}
                  required
                />

                {/* PAYMENT METHOD */}

                <select
                  name="paymentMethod"
                  value={form.paymentMethod}
                  onChange={handleChange}
                >

                  <option value="UPI">
                    UPI
                  </option>

                  <option value="CARD">
                    Card
                  </option>

                  <option value="CASH">
                    Cash
                  </option>

                  <option value="BANK_TRANSFER">
                    Bank Transfer
                  </option>

                  <option value="BANK">
                    Bank
                  </option>

                </select>

                {/* TRANSACTION TYPE */}

                <select
                  name="transactionType"
                  value={form.transactionType}
                  onChange={handleChange}
                >

                  <option value="EXPENSE">
                    Expense
                  </option>

                  <option value="INCOME">
                    Income
                  </option>

                  <option value="WITHDRAWAL">
                    Withdrawal
                  </option>

                </select>

              </div>

              {/* ================================================= */}
              {/* WITHDRAWAL INFORMATION */}
              {/* ================================================= */}

              {form.transactionType ===
                "WITHDRAWAL" && (

                <div
                  style={{
                    margin:
                      "0 20px 20px",
                    padding:
                      "14px 16px",
                    borderRadius:
                      "10px",
                    background:
                      "#fff7ed",
                    border:
                      "1px solid #fed7aa",
                    color:
                      "#9a3412",
                    fontSize:
                      "14px",
                  }}
                >

                  <strong>
                    Cash Withdrawal
                  </strong>

                  <br />

                  Money will be deducted
                  from the selected bank/UPI
                  account and added to your
                  Cash Wallet.

                </div>

              )}

              {/* ================================================= */}
              {/* CASH INFORMATION */}
              {/* ================================================= */}

              {form.paymentMethod ===
                "CASH" && (

                <div
                  style={{
                    margin:
                      "0 20px 20px",
                    padding:
                      "14px 16px",
                    borderRadius:
                      "10px",
                    background:
                      "#f0fdf4",
                    border:
                      "1px solid #bbf7d0",
                    color:
                      "#166534",
                    fontSize:
                      "14px",
                  }}
                >

                  <strong>
                    Cash Transaction
                  </strong>

                  <br />

                  This transaction will use
                  your Cash Wallet.

                </div>

              )}

              {/* ================================================= */}
              {/* BUTTONS */}
              {/* ================================================= */}

              <div
                style={{
                  display: "flex",
                  gap: "12px",
                  padding:
                    "0 20px 20px",
                }}
              >

                <button
                  type="submit"
                  className={styles.addButton}
                  disabled={saving}
                >

                  {saving
                    ? "Saving..."
                    : "Save Transaction"}

                </button>

                <button
                  type="button"
                  onClick={() =>
                    setShowForm(false)
                  }
                >
                  Cancel
                </button>

              </div>

            </form>

          </section>

        )}

        {/* ================================================= */}
        {/* SUMMARY */}
        {/* ================================================= */}

        <section
          className={styles.summary}
        >

          <div
            className={styles.summaryCard}
          >

            <span>
              Total Transactions
            </span>

            <strong>
              {transactions.length}
            </strong>

          </div>

          <div
            className={styles.summaryCard}
          >

            <span>
              Total Income
            </span>

            <strong
              className={styles.income}
            >
              ₹
              {totalIncome.toLocaleString()}
            </strong>

          </div>

          <div
            className={styles.summaryCard}
          >

            <span>
              Total Expenses
            </span>

            <strong
              className={styles.expense}
            >
              ₹
              {totalExpenses.toLocaleString()}
            </strong>

          </div>

          <div
            className={styles.summaryCard}
          >

            <span>
              Total Withdrawals
            </span>

            <strong>
              ₹
              {totalWithdrawals.toLocaleString()}
            </strong>

          </div>

        </section>

        {/* ================================================= */}
        {/* TRANSACTIONS TABLE */}
        {/* ================================================= */}

        <section
          className={styles.tableCard}
        >

          <div
            className={styles.tableHeader}
          >

            <div>

              <h2>
                Recent Transactions
              </h2>

              <p>
                Your latest money activity
              </p>

            </div>

            <div
              className={styles.filters}
            >

              <select defaultValue="all">

                <option value="all">
                  All Categories
                </option>

                <option value="food">
                  Food
                </option>

                <option value="shopping">
                  Shopping
                </option>

                <option value="travel">
                  Travel
                </option>

                <option value="income">
                  Income
                </option>

                <option value="cash withdrawal">
                  Cash Withdrawal
                </option>

              </select>

              <select defaultValue="all">

                <option value="all">
                  All Transactions
                </option>

                <option value="income">
                  Income
                </option>

                <option value="expense">
                  Expenses
                </option>

                <option value="withdrawal">
                  Withdrawals
                </option>

              </select>

            </div>

          </div>

          <div
            className={styles.tableWrapper}
          >

            {loading && (
              <p>
                Loading transactions...
              </p>
            )}

            {error && (
              <p>
                {error}
              </p>
            )}

            {!loading &&
              !error && (

              <table>

                <thead>

                  <tr>

                    <th>
                      Transaction
                    </th>

                    <th>
                      Category
                    </th>

                    <th>
                      Account
                    </th>

                    <th>
                      Date
                    </th>

                    <th>
                      Amount
                    </th>

                  </tr>

                </thead>

                <tbody>

                  {transactions.map(
                    (transaction) => {

                      const isIncome =
                        transaction.transactionType ===
                        "INCOME";

                      const isWithdrawal =
                        transaction.transactionType ===
                        "WITHDRAWAL";

                      return (

                        <tr
                          key={
                            transaction.id
                          }
                        >

                          {/* TRANSACTION */}

                          <td>

                            <div
                              className={
                                styles.transaction
                              }
                            >

                              <div
                                className={`${
                                  styles.icon
                                } ${
                                  isIncome
                                    ? styles.incomeIcon
                                    : styles.expenseIcon
                                }`}
                              >

                                {isIncome
                                  ? "↓"
                                  : isWithdrawal
                                  ? "↗"
                                  : "↑"}

                              </div>

                              <div>

                                <strong>
                                  {
                                    transaction
                                      .merchant
                                      ?.name ||
                                    "Unknown"
                                  }
                                </strong>

                                <span>
                                  {
                                    transaction
                                      .category
                                      ?.name ||
                                    "Others"
                                  }
                                </span>

                              </div>

                            </div>

                          </td>

                          {/* CATEGORY */}

                          <td>

                            {
                              transaction
                                .category
                                ?.name ||
                              "Others"
                            }

                          </td>

                          {/* ACCOUNT */}

                          <td>

                            <strong>
                              {
                                transaction
                                  .account
                                  ?.accountName ||
                                "Unknown"
                              }
                            </strong>

                            <br />

                            <span
                              style={{
                                fontSize: "12px",
                                opacity: 0.7,
                              }}
                            >
                              {
                                transaction
                                  .account
                                  ?.bankName ||
                                ""
                              }
                            </span>

                          </td>

                          {/* DATE */}

                          <td>

                            {
                              transaction
                                .transactionTime
                                ? new Date(
                                    transaction
                                      .transactionTime
                                  ).toLocaleString()
                                : "-"
                            }

                          </td>

                          {/* AMOUNT */}

                          <td
                            className={
                              isIncome
                                ? styles.income
                                : styles.expense
                            }
                          >

                            {isIncome
                              ? "+"
                              : "-"}

                            ₹
                            {Number(
                              transaction.amount
                            ).toLocaleString()}

                          </td>

                        </tr>

                      );
                    }
                  )}

                </tbody>

              </table>

            )}

            {!loading &&
              !error &&
              transactions.length ===
                0 && (

              <p>
                No transactions found.
              </p>

            )}

          </div>

        </section>

      </main>

    </div>
  );
}

export default Transactions;