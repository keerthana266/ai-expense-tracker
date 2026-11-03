package com.expensetracker.agent

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ExpenseNotificationListener : NotificationListenerService() {

    private val tag = "ExpenseAIAgent"
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Targeted financial and UPI apps
    private val targetPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay (GPay)
        "com.phonepe.app",                         // PhonePe
        "net.one97.paytm",                         // Paytm
        "in.org.npci.upiapp",                      // BHIM UPI
        "com.dreamplug.androidapp",                // CRED
        "com.sbi.lotusintouch",                    // SBI Yono
        "com.hdfcbank.payzapp",                    // HDFC PayZapp
        "com.icicibank.mobile",                    // ICICI iMobile
        "com.axis.mobile",                         // Axis Mobile
        "com.google.android.apps.messaging",       // Google Messages (SMS)
        "com.android.mms",                         // Android MMS
        "com.samsung.android.messaging"            // Samsung Messages
    )

    private val paymentKeywords = listOf(
        "debited", "paid", "sent", "transferred", "spent",
        "withdrawn", "atm", "inr", "rs.", "rs ", "₹", "cash wdl"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val fullContent = if (bigText.isNotBlank()) "$title $bigText" else "$title $text"
        val lowerContent = fullContent.lowercase()

        // Check package target or financial SMS keywords
        val isTargetApp = targetPackages.contains(packageName)
        val hasFinancialKeywords = paymentKeywords.any { lowerContent.contains(it) }

        if (isTargetApp || hasFinancialKeywords) {
            Log.d(tag, "💳 Payment notification detected from: $packageName")
            Log.d(tag, "Content: $fullContent")

            sendNotificationToBackend(fullContent, packageName, sbn.postTime)
        }
    }

    private fun sendNotificationToBackend(text: String, pkgName: String, timestamp: Long) {
        val prefs = getSharedPreferences("ExpenseAIPrefs", Context.MODE_PRIVATE)
        val backendUrl = prefs.getString("backend_url", "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
        val authToken = prefs.getString("jwt_token", "") ?: ""

        val endpoint = "$backendUrl/api/notifications"

        val json = JSONObject().apply {
            put("notificationText", text)
            put("packageName", pkgName)
            put("timestamp", timestamp)
        }

        val body = json.toString().toRequestBody(jsonMediaType)

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(body)

        if (authToken.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $authToken")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "❌ Failed to dispatch notification to ExpenseAI backend: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string() ?: ""
                Log.d(tag, "✅ Server response (${response.code}): $respBody")
            }
        })
    }
}
