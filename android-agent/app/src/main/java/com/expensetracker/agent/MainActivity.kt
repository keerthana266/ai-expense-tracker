package com.expensetracker.agent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var urlInput: EditText
    private lateinit var tokenInput: EditText
    private lateinit var btnPermission: Button
    private lateinit var btnSave: Button
    private lateinit var btnSimulate: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.txtStatus)
        urlInput = findViewById(R.id.edtBackendUrl)
        tokenInput = findViewById(R.id.edtToken)
        btnPermission = findViewById(R.id.btnPermission)
        btnSave = findViewById(R.id.btnSave)
        btnSimulate = findViewById(R.id.btnSimulate)

        val prefs = getSharedPreferences("ExpenseAIPrefs", Context.MODE_PRIVATE)
        urlInput.setText(prefs.getString("backend_url", "http://10.0.2.2:8080"))
        tokenInput.setText(prefs.getString("jwt_token", ""))

        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnSave.setOnClickListener {
            prefs.edit()
                .putString("backend_url", urlInput.text.toString().trim())
                .putString("jwt_token", tokenInput.text.toString().trim())
                .apply()
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }

        btnSimulate.setOnClickListener {
            simulatePaymentAlert()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isEnabled = enabledListeners != null && enabledListeners.contains(packageName)

        if (isEnabled) {
            statusText.text = "● Background Agent Status: Active & Listening"
            statusText.setTextColor(getColor(android.R.color.holo_green_dark))
            btnPermission.isEnabled = false
            btnPermission.text = "Permission Granted ✓"
        } else {
            statusText.text = "● Status: Permission Required"
            statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            btnPermission.isEnabled = true
            btnPermission.text = "Grant Notification Access"
        }
    }

    private fun simulatePaymentAlert() {
        val prefs = getSharedPreferences("ExpenseAIPrefs", Context.MODE_PRIVATE)
        val backendUrl = prefs.getString("backend_url", "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
        val authToken = prefs.getString("jwt_token", "") ?: ""

        val endpoint = "$backendUrl/api/notifications"
        val mockText = "Paid ₹380 to Zomato via UPI transaction ID 4038104812"

        val json = JSONObject().apply {
            put("notificationText", mockText)
            put("packageName", "com.google.android.apps.nbu.paisa.user")
            put("timestamp", System.currentTimeMillis())
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val reqBuilder = Request.Builder().url(endpoint).post(body)
        if (authToken.isNotBlank()) reqBuilder.addHeader("Authorization", "Bearer $authToken")

        client.newCall(reqBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Test Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string() ?: ""
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Dispatched! Response: $resp", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
