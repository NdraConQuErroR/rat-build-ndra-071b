package com.app.xgoogle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Base64
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream

class MediaPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("MediaPlayerChannel", "Media Playback", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
            val notif = NotificationCompat.Builder(this, "MediaPlayerChannel")
                .setContentTitle("Audio Service").setSmallIcon(android.R.drawable.ic_media_play).build()
            startForeground(5, notif)
        }

        val base64 = intent?.getStringExtra("base64")
        if (base64 != null) {
            try {
                mediaPlayer?.release()
                // Simpan base64 ke cache sementara biar bisa diputer
                val tempFile = File(cacheDir, "temp_audio.mp3")
                val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                FileOutputStream(tempFile).use { it.write(decodedBytes) }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()
                }
            } catch (e: Exception) { }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }
}
