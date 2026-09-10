package com.thambithappu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Shows/hides plain-Android-View overlays on top of whatever app is currently in the
 * foreground. Deliberately NOT built with Jetpack Compose - hosting a ComposeView from
 * a Service means manually wiring a LifecycleOwner / ViewModelStoreOwner /
 * SavedStateRegistryOwner onto the window, which is a lot of extra plumbing for no real
 * benefit here. Plain views are simpler and more robust for a Service-hosted overlay.
 *
 * Known simplification: there is no way to blur the REAL pixels of whatever app is
 * underneath without capturing the screen (MediaProjection API), which is a much bigger
 * permission + privacy story. So "Blur" and "Privacy Overlay" modes both render as the
 * same opaque full-screen cover here - only the in-app demo screen can do a true blur,
 * because there we control the content being blurred.
 */
class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentView: View? = null

    fun hide() {
        currentView?.let { runCatching { windowManager.removeView(it) } }
        currentView = null
    }

    /** Small, non-blocking banner - lets the user keep using whatever app is underneath. */
    fun showBanner(text: String, backgroundColor: Int, onTap: () -> Unit) {
        hide()
        val banner = TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setBackgroundColor(backgroundColor)
            setPadding(48, 32, 48, 32)
            setOnClickListener { onTap() }
        }
        val params = overlayParams(
            width = WindowManager.LayoutParams.WRAP_CONTENT,
            height = WindowManager.LayoutParams.WRAP_CONTENT,
            touchModal = false
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 96
        }
        windowManager.addView(banner, params)
        currentView = banner
    }

    /** Full-screen block - covers and blocks interaction with whatever is underneath. */
    fun showFullScreenBlock(title: String, message: String, onDismiss: () -> Unit) {
        hide()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F0000000"))
        }
        val titleView = TextView(context).apply {
            this.text = title
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setPadding(64, 0, 64, 24)
        }
        val messageView = TextView(context).apply {
            this.text = message
            setTextColor(Color.parseColor("#DDFFFFFF"))
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(64, 0, 64, 32)
        }
        val dismissBtn = Button(context).apply {
            text = "Dismiss"
            setOnClickListener { onDismiss() }
        }
        container.addView(titleView)
        container.addView(messageView)
        container.addView(dismissBtn)

        val params = overlayParams(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = WindowManager.LayoutParams.MATCH_PARENT,
            touchModal = true
        )
        windowManager.addView(container, params)
        currentView = container
    }

    /** Full-screen block showing a generated Bitmap (the meme) instead of plain text. */
    fun showFullScreenImage(bitmap: Bitmap, onDismiss: () -> Unit) {
        hide()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F0000000"))
            setOnClickListener { onDismiss() }
        }
        val imageView = ImageView(context).apply {
            setImageBitmap(bitmap)
            adjustViewBounds = true
        }
        container.addView(
            imageView,
            LinearLayout.LayoutParams(
                (context.resources.displayMetrics.widthPixels * 0.85f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        val dismissBtn = Button(context).apply {
            text = "Dismiss"
            setOnClickListener { onDismiss() }
        }
        container.addView(dismissBtn)

        val params = overlayParams(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = WindowManager.LayoutParams.MATCH_PARENT,
            touchModal = true
        )
        windowManager.addView(container, params)
        currentView = container
    }

    private fun overlayParams(width: Int, height: Int, touchModal: Boolean): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (!touchModal) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )
    }
}