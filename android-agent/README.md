# ExpenseAI Android Notification Listener Agent 📱

This Android application acts as the background agent for **ExpenseAI (Zero Manual Entry)**.

## How It Works
1. Runs a background `NotificationListenerService` that intercepts status bar notifications from UPI and banking apps (Google Pay, PhonePe, Paytm, CRED, Bank SMS).
2. Filters out payment alerts, transfers, and ATM withdrawals using regex.
3. Forwards notification payload (`notificationText`, `packageName`, `timestamp`) to the Spring Boot backend (`/api/notifications`), which produces to **Kafka** and parses using **Gemini LLM**.

## Setup & Running
1. Open the `android-agent` folder in **Android Studio**.
2. Run on an emulator (Android 8.0+) or your physical Android device.
3. On app launch, click **"Grant Notification Access"** and toggle on `ExpenseAI Agent` in Android Notification Access settings.
4. Set your Backend URL:
   - For Android Emulator: `http://10.0.2.2:8080`
   - For Physical Device: `http://<your-computer-ip>:8080`
5. Click **"Save Configuration"**.
6. Tap **"Simulate Test Payment"** to verify connection!
