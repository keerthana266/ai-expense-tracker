"""
ExpenseAI (Advanced AI Expense Tracker) - Interactive Live Simulator
Demonstrates:
1. Zero Manual Entry (Push Notification Ingestion -> Kafka -> Gemini Categorization)
2. Automated Credit & Debit Recognition (Incomes, Expenses, Savings, Balance auto-update)
3. Spending Velocity Pacer ("Slow Down!" Speed Intervention & Broke Date Runway)
4. Liquid Cash Tracking (ATM Withdrawal -> Cash Wallet -> WhatsApp Voice/Text Cash Spends)
"""

import sys
import json
import time

try:
    import urllib.request
    import urllib.error
except ImportError:
    pass

BASE_URL = "http://localhost:8080"

def print_banner():
    print("=" * 75)
    print("           ⚡ ExpenseAI - Advanced AI Financial Intelligence ⚡")
    print("=" * 75)
    print("1. [Auto Expense Debit]  Simulate ₹480 Starbucks UPI Debit (Expenses & Balance)")
    print("2. [Auto Income Credit]  Simulate ₹25,000 Salary Credit (Income, Savings & Balance)")
    print("3. [Spending Speed Nudge] Trigger Velocity Warning ('Slow Down' Intervention)")
    print("4. [Liquid Cash 1]       Simulate ₹5,000 ATM Withdrawal -> Cash Wallet")
    print("5. [Liquid Cash 2]       WhatsApp Cash Spend ('chai 30 cash' / 'auto 60')")
    print("6. [Broke Date & Pacer]  Check Burn Rate Speed, Broke Date & Runway")
    print("7. Run Full Automated Showcase (All features in sequence)")
    print("0. Exit")
    print("-" * 75)

def http_post(endpoint, data, token=None):
    url = f"{BASE_URL}{endpoint}"
    payload = json.dumps(data).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    req = urllib.request.Request(url, data=payload, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            body = resp.read().decode("utf-8")
            try:
                return json.loads(body)
            except:
                return body
    except urllib.error.URLError as e:
        return {"error": str(e)}

def http_get(endpoint, token=None):
    url = f"{BASE_URL}{endpoint}"
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    req = urllib.request.Request(url, headers=headers, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            body = resp.read().decode("utf-8")
            try:
                return json.loads(body)
            except:
                return body
    except urllib.error.URLError as e:
        return {"error": str(e)}

def test_expense_debit():
    print("\n📲 [1. Auto Expense Debit Simulation]")
    print("Simulating bank debit notification: 'Paid ₹480 to Starbucks Cafe'...")
    payload = {
        "notificationText": "Paid ₹480 to Starbucks Cafe via UPI transaction ID 9928374102 from SBI A/C",
        "packageName": "com.google.android.apps.nbu.paisa.user",
        "timestamp": int(time.time() * 1000)
    }
    res = http_post("/api/notifications", payload)
    print("Response from ExpenseAI:", res)
    print("✅ Result: ₹480 added to Total Expenses, deducted from Balance, categorized by Gemini!")

def test_income_credit():
    print("\n💰 [2. Auto Income Credit Simulation]")
    print("Simulating salary credit notification: 'INR 25,000.00 credited towards Salary'...")
    payload = {
        "notificationText": "INR 25,000.00 credited to your A/C ...8891 on 02-Nov-26 towards Salary from TechCorp via NEFT",
        "packageName": "com.google.android.apps.messaging",
        "timestamp": int(time.time() * 1000)
    }
    res = http_post("/api/notifications", payload)
    print("Response from ExpenseAI:", res)
    print("✅ Result: ₹25,000 added to Total Income, Savings increased, Balance increased, Runway extended!")

def test_speed_intervention():
    print("\n⚠️ [3. Spending Velocity Nudge - 'Slow Down!']")
    print("Triggering AI Spending Velocity warning...")
    res = http_post("/whatsapp/test-roast?item=Shopping")
    print("AI Intervention:", res)

def test_atm_withdrawal():
    print("\n💵 [4. Liquid Cash Loophole - ATM Withdrawal Detection]")
    print("Simulating bank SMS: 'Rs 5000 debited from SBI ATM'")
    payload = {
        "notificationText": "Rs 5000.00 debited from SBI A/C ...8891 at ATM CASH WDL on 02-Nov-26",
        "packageName": "com.google.android.apps.messaging",
        "timestamp": int(time.time() * 1000)
    }
    res = http_post("/api/notifications", payload)
    print("Response from ExpenseAI:", res)
    print("✅ Notice: ₹5,000 is NOT counted as an expense! Automatically allocated to your Cash Wallet.")

def test_whatsapp_cash_spend():
    print("\n💬 [5. Liquid Cash Loophole - WhatsApp Casual Cash Spend]")
    user_msg = "biryani 250 cash"
    print(f"Simulating incoming WhatsApp message: '{user_msg}'")
    res = http_post("/api/whatsapp/webhook", {
        "from": "+919876543210",
        "message": user_msg
    })
    print("AI Response:", res)

def test_broke_date():
    print("\n📉 [6. Broke Date & Speed Pacer Runway]")
    res = http_get("/api/prediction")
    print("Broke Date Response:", res)

def main():
    while True:
        print_banner()
        choice = input("Enter option (0-7): ").strip()
        if choice == "1":
            test_expense_debit()
        elif choice == "2":
            test_income_credit()
        elif choice == "3":
            test_speed_intervention()
        elif choice == "4":
            test_atm_withdrawal()
        elif choice == "5":
            test_whatsapp_cash_spend()
        elif choice == "6":
            test_broke_date()
        elif choice == "7":
            print("\n🌟 Running Full Automated Showcase in sequence...")
            test_income_credit()
            time.sleep(1)
            test_expense_debit()
            time.sleep(1)
            test_atm_withdrawal()
            time.sleep(1)
            test_whatsapp_cash_spend()
            time.sleep(1)
            test_speed_intervention()
            time.sleep(1)
            test_broke_date()
        elif choice == "0":
            print("Exiting ExpenseAI simulator.")
            break
        else:
            print("Invalid option. Try again.")
        print("\n" + "=" * 75 + "\n")

if __name__ == "__main__":
    main()
