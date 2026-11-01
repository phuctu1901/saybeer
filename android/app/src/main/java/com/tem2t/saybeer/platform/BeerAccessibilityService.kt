package com.tem2t.saybeer.platform

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.tem2t.saybeer.MainActivity
import com.tem2t.saybeer.data.SessionEngine
import com.tem2t.saybeer.data.remaining
import com.tem2t.saybeer.repository

/** Receives package names only. Never requests a node tree, text, gestures, or keystrokes. */
class BeerAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var foreground = ""
    private var shield: View? = null
    private var countdown: TextView? = null
    private var safePackages = emptySet<String>()
    private val ticker = object : Runnable {
        override fun run() {
            repository.tick()
            refresh()
            handler.postDelayed(this, 1000)
        }
    }
    override fun onServiceConnected() {
        safePackages = AppCatalog.protectedPackages(this)
        handler.removeCallbacks(ticker)
        handler.post(ticker)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        // Ignore our overlay's events; opening the activity explicitly clears foreground below.
        if (pkg == packageName && shield != null) return
        foreground = pkg
        repository.tick()
        refresh()
    }
    private fun refresh() {
        val now = System.currentTimeMillis()
        val locked = getSystemService(android.app.KeyguardManager::class.java).isKeyguardLocked
        if (!locked && foreground !in safePackages && SessionEngine.shouldBlock(repository.current, foreground, now)) {
            if (shield == null) showShield()
            repository.current.session?.let { countdown?.text = remaining(it.end, now) }
        } else removeShield()
    }
    private fun showShield() {
        val density = resources.displayMetrics.density
        fun dp(n: Int) = (n * density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(28), dp(32), dp(28), dp(32))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.rgb(18,14,10), Color.rgb(11,9,7)))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        fun text(value: String, size: Float, color: Int) = TextView(this).apply {
            text = value; textSize = size; setTextColor(color); gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(12))
        }
        val amber = Color.rgb(245,166,35)
        layout.addView(text("SayBeer", 36f, amber).apply { setTypeface(null, Typeface.BOLD) })
        layout.addView(text("Để sáng mai không phải xin lỗi.", 22f, Color.WHITE))
        layout.addView(text("App này đang được khóa theo lựa chọn của bạn.", 16f, Color.LTGRAY))
        countdown = text("", 46f, amber).apply { typeface = Typeface.MONOSPACE }
        layout.addView(countdown)
        layout.addView(text("Hãy say bia. Xin đừng “say” bậy.", 16f, Color.LTGRAY))
        layout.addView(Button(this).apply {
            setText(com.tem2t.saybeer.R.string.shield_open)
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.rgb(36,22,0))
            backgroundTintList = android.content.res.ColorStateList.valueOf(amber)
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.rgb(255,193,7), Color.rgb(229,128,0))).apply { cornerRadius = dp(16).toFloat() }
            setOnClickListener {
                this@BeerAccessibilityService.foreground = packageName
                removeShield()
                startActivity(Intent(this@BeerAccessibilityService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }, LinearLayout.LayoutParams(-1, dp(64)))
        layout.addView(Button(this).apply {
            setText(com.tem2t.saybeer.R.string.shield_home); isAllCaps = false
            textSize = 14f
            setTextColor(amber)
            background = GradientDrawable().apply { setColor(Color.rgb(23,20,16)); setStroke(dp(1), Color.rgb(58,45,27)); cornerRadius = dp(16).toFloat() }
            setOnClickListener { this@BeerAccessibilityService.foreground = ""; removeShield(); performGlobalAction(GLOBAL_ACTION_HOME) }
        }, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(12) })
        val params = WindowManager.LayoutParams(-1, -1, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, android.graphics.PixelFormat.OPAQUE)
        try {
            getSystemService(WindowManager::class.java).addView(layout, params)
            shield = layout
        } catch (_: WindowManager.BadTokenException) { countdown = null }
    }
    private fun removeShield() {
        shield?.let { getSystemService(WindowManager::class.java).removeView(it) }
        shield = null; countdown = null
    }
    override fun onInterrupt() { removeShield() }
    override fun onDestroy() { handler.removeCallbacks(ticker); removeShield(); super.onDestroy() }
}
