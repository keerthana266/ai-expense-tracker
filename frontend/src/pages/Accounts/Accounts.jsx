import { useEffect, useState } from "react";
import { FaPlus, FaUniversity, FaWallet, FaTimes } from "react-icons/fa";

import Sidebar from "../../components/common/Sidebar/Sidebar";
import Navbar from "../../components/common/Navbar/Navbar";
import api from "../../api/axios";

import styles from "./Accounts.module.css";

function Accounts() {
  const [accounts, setAccounts] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({
    accountName: "",
    bankName: "",
    upiId: "",
    currentBalance: "",
    accountType: "Savings Account",
    primaryAccount: false,
  });

  // ==============================
  // FETCH ACCOUNTS FROM BACKEND
  // ==============================
  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      setLoading(true);

      const response = await api.get("/accounts");

      console.log("Accounts received:", response.data);

      setAccounts(response.data);
    } catch (error) {
      console.error("Failed to fetch accounts:", error);

      if (error.response) {
        console.error("Status:", error.response.status);
        console.error("Data:", error.response.data);
      }
    } finally {
      setLoading(false);
    }
  };

  // ==============================
  // FORM INPUT CHANGE
  // ==============================
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  // ==============================
  // ADD ACCOUNT
  // ==============================
  const handleAddAccount = async (e) => {
    e.preventDefault();

    if (!form.accountName.trim()) {
      alert("Please enter account name");
      return;
    }

    if (!form.bankName.trim()) {
      alert("Please enter bank/provider name");
      return;
    }

    if (!form.currentBalance || Number(form.currentBalance) < 0) {
      alert("Please enter a valid balance");
      return;
    }

    try {
      const payload = {
        accountName: form.accountName,
        bankName: form.bankName,
        upiId: form.upiId.trim() || "N/A",
        currentBalance: Number(form.currentBalance),
        accountType: form.accountType,
        primaryAccount: form.primaryAccount,
      };

      console.log("Sending account:", payload);

      const response = await api.post("/accounts", payload);

      console.log("Account created:", response.data);

      // If this account is primary, backend may have
      // changed the previous primary account.
      // Therefore fetch the complete list again.
      await fetchAccounts();

      // Reset form
      setForm({
        accountName: "",
        bankName: "",
        upiId: "",
        currentBalance: "",
        accountType: "Savings Account",
        primaryAccount: false,
      });

      setShowModal(false);

      alert("Account added successfully!");
    } catch (error) {
      console.error("Failed to add account:", error);

      if (error.response) {
        console.error("Status:", error.response.status);
        console.error("Data:", error.response.data);
        alert(
          `Failed to add account: ${
            error.response.data?.message || "Server error"
          }`
        );
      } else {
        alert("Failed to add account. Check whether the backend is running.");
      }
    }
  };

  // ==============================
  // TOTAL BALANCE
  // ==============================
  const totalBalance = accounts.reduce(
    (total, account) => total + Number(account.currentBalance || 0),
    0
  );

  // ==============================
  // FORMAT MONEY
  // ==============================
  const formatMoney = (amount) => {
    return `₹${Number(amount || 0).toLocaleString("en-IN")}`;
  };

  // ==============================
  // ACCOUNT ICON
  // ==============================
  const getAccountIcon = (accountType) => {
    const type = accountType?.toLowerCase() || "";

    if (type.includes("cash")) {
      return <FaWallet />;
    }

    return <FaUniversity />;
  };

  return (
    <div className={styles.container}>
      <Sidebar />

      <main className={styles.main}>
        <Navbar />

        {/* ==============================
            HEADER
        ============================== */}
        <div className={styles.header}>
          <div>
            <h1>Accounts</h1>
            <p>Manage your bank accounts, UPI accounts and cash.</p>
          </div>

          <button
            className={styles.addButton}
            onClick={() => setShowModal(true)}
          >
            <FaPlus />
            Add Account
          </button>
        </div>

        {/* ==============================
            TOTAL BALANCE
        ============================== */}
        <div className={styles.totalCard}>
          <div>
            <p>Total Balance</p>

            <h2>{formatMoney(totalBalance)}</h2>
          </div>
        </div>

        {/* ==============================
            ACCOUNTS
        ============================== */}
        <div className={styles.accountsSection}>
          <div className={styles.sectionHeader}>
            <h2>Your Accounts</h2>

            <span>
              {accounts.length}{" "}
              {accounts.length === 1 ? "Account" : "Accounts"}
            </span>
          </div>

          {loading ? (
            <div className={styles.emptyState}>
              <p>Loading accounts...</p>
            </div>
          ) : accounts.length === 0 ? (
            <div className={styles.emptyState}>
              <FaUniversity />

              <h3>No accounts found</h3>

              <p>Add your first bank, UPI or cash account.</p>

              <button
                className={styles.addButton}
                onClick={() => setShowModal(true)}
              >
                <FaPlus />
                Add Account
              </button>
            </div>
          ) : (
            <div className={styles.accountGrid}>
              {accounts.map((account) => (
                <div className={styles.accountCard} key={account.id}>
                  {/* ==============================
                      CARD TOP
                  ============================== */}
                  <div className={styles.cardTop}>
                    <div className={styles.accountIcon}>
                      {getAccountIcon(account.accountType)}
                    </div>

                    {account.primaryAccount && (
                      <span className={styles.primaryBadge}>
                        Primary
                      </span>
                    )}
                  </div>

                  {/* ==============================
                      ACCOUNT DETAILS
                  ============================== */}
                  <div className={styles.accountDetails}>
                    <h3>{account.accountName}</h3>

                    <p className={styles.bankName}>
                      {account.bankName}
                    </p>

                    <p className={styles.accountType}>
                      {account.accountType}
                    </p>

                    <h2>
                      {formatMoney(account.currentBalance)}
                    </h2>

                    <p className={styles.accountNumber}>
                      {account.upiId || "N/A"}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ==============================
            ADD ACCOUNT MODAL
        ============================== */}
        {showModal && (
          <div
            className={styles.modalOverlay}
            onClick={() => setShowModal(false)}
          >
            <div
              className={styles.modal}
              onClick={(e) => e.stopPropagation()}
            >
              {/* MODAL HEADER */}
              <div className={styles.modalHeader}>
                <div>
                  <h2>Add Account</h2>

                  <p>
                    Add a bank, UPI or cash account.
                  </p>
                </div>

                <button
                  className={styles.closeButton}
                  onClick={() => setShowModal(false)}
                  type="button"
                >
                  <FaTimes />
                </button>
              </div>

              {/* FORM */}
              <form
                className={styles.form}
                onSubmit={handleAddAccount}
              >
                {/* ACCOUNT NAME */}
                <div className={styles.formGroup}>
                  <label htmlFor="accountName">
                    Account Name
                  </label>

                  <input
                    id="accountName"
                    name="accountName"
                    type="text"
                    placeholder="e.g. Main Account"
                    value={form.accountName}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* BANK / PROVIDER */}
                <div className={styles.formGroup}>
                  <label htmlFor="bankName">
                    Bank / Provider
                  </label>

                  <input
                    id="bankName"
                    name="bankName"
                    type="text"
                    placeholder="e.g. SBI, HDFC, Google Pay"
                    value={form.bankName}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* ACCOUNT TYPE */}
                <div className={styles.formGroup}>
                  <label htmlFor="accountType">
                    Account Type
                  </label>

                  <select
                    id="accountType"
                    name="accountType"
                    value={form.accountType}
                    onChange={handleChange}
                  >
                    <option value="Savings Account">
                      Savings Account
                    </option>

                    <option value="Current Account">
                      Current Account
                    </option>

                    <option value="UPI">
                      UPI
                    </option>

                    <option value="Cash">
                      Cash
                    </option>

                    <option value="Credit Card">
                      Credit Card
                    </option>
                  </select>
                </div>

                {/* UPI ID / ACCOUNT NUMBER */}
                <div className={styles.formGroup}>
                  <label htmlFor="upiId">
                    UPI ID / Account Number
                  </label>

                  <input
                    id="upiId"
                    name="upiId"
                    type="text"
                    placeholder="e.g. keerthana@upi"
                    value={form.upiId}
                    onChange={handleChange}
                  />
                </div>

                {/* BALANCE */}
                <div className={styles.formGroup}>
                  <label htmlFor="currentBalance">
                    Current Balance
                  </label>

                  <input
                    id="currentBalance"
                    name="currentBalance"
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="e.g. 25000"
                    value={form.currentBalance}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* PRIMARY ACCOUNT */}
                <label className={styles.checkboxRow}>
                  <input
                    type="checkbox"
                    name="primaryAccount"
                    checked={form.primaryAccount}
                    onChange={handleChange}
                  />

                  <span>
                    Set as primary account
                  </span>
                </label>

                {/* BUTTONS */}
                <div className={styles.modalActions}>
                  <button
                    type="button"
                    className={styles.cancelButton}
                    onClick={() => setShowModal(false)}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className={styles.saveButton}
                  >
                    <FaPlus />
                    Add Account
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default Accounts;