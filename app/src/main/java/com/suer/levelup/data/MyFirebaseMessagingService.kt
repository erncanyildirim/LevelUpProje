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

    // Firebase yeni token verdiğinde çalışır
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM Token: $token")
    }

    // Uygulama AÇIKKEN bildirim gelirse burası çalışır
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Bildirim başlığı ve içeriği varsa göster
        val title = remoteMessage.notification?.title ?: "Level Up"
        val body = remoteMessage.notification?.body ?: "Yeni bir mesajın var!"

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "levelup_notification_channel"
        val notificationId = Random.nextInt()

        // Bildirime tıklayınca MainActivity açılsın
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Android 12+ (SDK 31+) için IMMUTABLE bayrağı zorunludur
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_ONE_SHOT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) ve üzeri için kanal oluşturmak şarttır
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Level Up Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Uygulama bildirim kanalı"
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirimi oluştur
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Eğer 'ic_launcher_foreground' yoksa, direkt 'ic_launcher' kullanabilirsin.
            // Hata alırsan burayı R.mipmap.ic_launcher yap.
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}