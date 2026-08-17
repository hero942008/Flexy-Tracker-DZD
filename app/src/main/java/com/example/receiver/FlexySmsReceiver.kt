package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.FlexyApp
import com.example.util.FlexySmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlexySmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return

                val fullBody = StringBuilder()
                var sender: String? = null

                for (msg in messages) {
                    if (sender == null) {
                        sender = msg.displayOriginatingAddress ?: msg.originatingAddress
                    }
                    fullBody.append(msg.displayMessageBody ?: msg.messageBody ?: "")
                }

                val repo = FlexyApp.instance.repository
                val config = repo.getOperatorSenderConfig()
                val parsed = FlexySmsParser.parse(sender, fullBody.toString(), config)
                if (parsed != null) {
                    Log.d("FlexySmsReceiver", "Detected Flexy SMS: ${parsed.amount} DA from ${parsed.operator}")
                    
                    // Save to Room DB asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repo.addTransaction(
                                amount = parsed.amount,
                                operatorName = parsed.operator,
                                senderNumber = parsed.sender,
                                rawMessage = parsed.rawBody,
                                isAutoDetected = true
                            )
                        } catch (e: Exception) {
                            Log.e("FlexySmsReceiver", "Error saving transaction to DB", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FlexySmsReceiver", "Error parsing SMS intent", e)
            }
        }
    }
}
