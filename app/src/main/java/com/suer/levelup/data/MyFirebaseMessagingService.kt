package com.suer.levelup.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.suer.levelup.MainActivity
import com.suer.levelup.R
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Firebase'den yeni bir token verildiğinde çalışır
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Bu token'ı sunucuna kaydedebilirsin, böylece spesifik bu telefona mesaj atabilirsin.
        println("FCM Token: $token")
    }

    // Uygulama AÇIKKEN mesaj gelirse burası çalışır
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Bildirim verisi varsa göster
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Level Up", it.body ?: "Yeni bildirim!")
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "levelup_notification_channel"
        val notificationId = Random.nextInt()

        // Bildirime tıklayınca hangi aktivite açılsın?
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) ve üzeri için Kanal oluşturmak zorunludur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Level Up Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Bildirim ikonu (varsayılan)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}