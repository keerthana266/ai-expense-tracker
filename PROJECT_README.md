# ExpenseAI (Advanced AI Expense Tracker) ⚡

An autonomous personal finance intelligence system powered by Gemini AI, Apache Kafka stream processing, and financial pacing psychology.

---

## 🌟 The Core Autonomous Features

### 1. 📲 100% Zero Manual Entry (Auto-Reads ALL Financial Messages)
- **Universal Financial Message Ingestion**: The background Android agent reads both bank SMS and push notifications from any UPI or banking app (Google Pay, PhonePe, Paytm, CRED, HDFC, SBI, ICICI, etc.).
- **Automatic Credit vs. Debit Recognition**:
  - **Incomes / Credits** (`Salary`, `UPI received`, `Cashback`, `Refunds`, `Bank Interest`): Automatically credited!
    $$\text{Total Income} \uparrow, \quad \text{Savings} \uparrow, \quad \text{Total Balance} \uparrow$$
  - **Expenses / Debits** (`UPI sent`, `Card swipe`, `Shopping`, `Dining`, `Bills`):
    $$\text{Total Expenses} \uparrow, \quad \text{Savings} \downarrow, \quad \text{Total Balance} \downarrow$$
  - **ATM Withdrawals**: Automatically converted to physical cash without distorting expense metrics.

### 2. 🚗 The "Speed Slowly" Pacing Engine (Spending Velocity Controller)
- Calculates your **Safe Daily Speed Limit**:
  $$\text{Safe Daily Speed} = \frac{\text{Current Liquid Balance}}{\text{Days Remaining in Month}}$$
- Measures your **Current Daily Burn Velocity**:
  $$\text{Burn Velocity} = \frac{\text{Recent 30-Day Expenses}}{\text{Days Counted}}$$
- **Dynamic Speed Warnings**:
  - If $\text{Burn Velocity} > 1.5 \times \text{Safe Limit}$: 
    > *"🚨 SLOW DOWN! You are burning money at 2.4x your safe speed limit! Reduce your daily spend to ₹450/day so you don't hit ₹0 before month-end!"*
  - If $\text{Burn Velocity} \le \text{Safe Limit}$:
    > *"🟢 Cruise Control: You are cruising at a safe speed within your daily limit. Safe runway!"*

### 3. 📉 Broke Date Prediction (Runway Burn Engine)
- Dynamically forecasts the exact calendar date your funds will hit ₹0:
  $$\text{Days Remaining} = \frac{\text{Total Bank Funds} + \text{Cash Wallet Funds}}{\text{Average Daily Burn Rate}}$$
- Displays actionable runway alerts and spending slowdown recommendations.

### 4. 🧠 Generalized AI Roast & Intervention Mode
- Not limited to any single food item! The AI dynamically analyzes your transaction history across **all categories** (Shopping, Dining, Cabs, Subscriptions, Entertainment).
- When a user repeats discretionary spending $\ge 3$ times in 7 days or exceeds their daily speed limit, Gemini generates a savage, personalized reality check delivered directly to **WhatsApp**:
  > *"🚨 Slow down! That's your 4th impulse shopping spend this week (₹1,200 at Zara). At this burn velocity, your broke date is coming 12 days early. Put the card away! 🛑💸"*

### 5. 💡 The Liquid Cash Loophole — Solved!
- **ATM Withdrawal Detection**: Automatically transfers funds from your Bank Account to your **Cash Wallet** ($\text{Bank} \rightarrow \text{Cash}$), protecting your Broke Date.
- **WhatsApp Casual Voice / Text Logging**: Type or voice-note casual physical cash spends (e.g., *"chai 30 cash"*, *"auto 60"*, *"groceries 200"*), which Gemini parses and deducts directly from your Cash Wallet.

---

## 🛠️ Tech Stack Flex

- **Frontend**: Next.js / React (Vite), Tailwind/CSS Modules, Recharts, Lucide/React Icons
- **Backend**: Java 17+, Spring Boot 3, Spring Data JPA, Spring Security (JWT)
- **Message Broker**: Apache Kafka + Zookeeper (High-throughput stream processing)
- **Database**: PostgreSQL
- **AI Brain**: Google Gemini API (`gemini-flash-latest`)
- **Mobile Agent**: Android Notification Listener Service (Kotlin, OkHttp)
- **Intervention Delivery**: WhatsApp Bot (Twilio / Meta Cloud API Webhook)

---

## 🚀 Running the Project

1. **Docker Infrastructure**:
   ```bash
   cd D:\ai-expense-tracker
   docker compose up -d
   ```
2. **Spring Boot Backend**:
   ```bash
   cd D:\ai-expense-tracker\backend
   .\mvnw.cmd spring-boot:run
   ```
3. **Frontend Dashboard**:
   ```bash
   cd D:\ai-expense-tracker\frontend
   npm run dev
   ```
   Open `http://localhost:5173` in your browser.
4. **Live Interactive Showcase / Simulator**:
   ```bash
   cd D:\ai-expense-tracker
   python simulate_expense_ai.py
   ```
   Or double-click `run_demo.bat`!
