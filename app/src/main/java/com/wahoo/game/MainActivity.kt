package com.wahoo.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.Cast
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val dashNamespace = "urn:x-cast:es.offd.dashcast"
    private val ui = Handler(Looper.getMainLooper())

    private lateinit var web: WebView
    private lateinit var castButton: MediaRouteButton
    private var castContext: CastContext? = null
    private var pendingUrl: String? = null

    /** Exposed to the page as window.AndroidCast */
    inner class Bridge {
        @JavascriptInterface
        fun cast(url: String) {
            runOnUiThread {
                pendingUrl = url
                val session = castContext?.sessionManager?.currentCastSession
                if (session != null && session.isConnected) sendUrl(session) else castButton.performClick()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        castButton = findViewById(R.id.cast_button)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.addJavascriptInterface(Bridge(), "AndroidCast")
        web.webViewClient = WebViewClient()
        web.loadUrl(getString(R.string.start_url))

        CastButtonFactory.setUpMediaRouteButton(applicationContext, castButton)
        CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor())
            .addOnSuccessListener { ctx ->
                castContext = ctx
                ctx.sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
            }
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) = onConnected(session)
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {}
        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionStartFailed(session: CastSession, error: Int) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionEnded(session: CastSession, error: Int) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    private fun onConnected(session: CastSession) {
        if (pendingUrl != null) {
            sendUrl(session)
        } else {
            // Cast icon tapped directly: ask the page for the TV link (it makes a room code)
            web.evaluateJavascript("(window.__wahooCastUrl?window.__wahooCastUrl():'')") { raw ->
                val url = raw?.trim('"') ?: ""
                if (url.startsWith("http")) {
                    pendingUrl = url
                    sendUrl(session)
                }
            }
        }
    }

    /** Waits for DashCast to say it's ready, then tells it which page to open (once). */
    private fun sendUrl(session: CastSession) {
        val url = pendingUrl ?: return
        pendingUrl = null
        var sent = false
        fun go() {
            if (sent) return
            sent = true
            val json = JSONObject()
                .put("url", url)
                .put("force", true)
                .put("reload", false)
                .put("reload_time", 0)
            try { session.sendMessage(dashNamespace, json.toString()) } catch (_: Exception) {}
        }
        try {
            session.setMessageReceivedCallbacks(dashNamespace, Cast.MessageReceivedCallback { _, _, message ->
                if (message.contains("ready", ignoreCase = true)) runOnUiThread { go() }
            })
        } catch (_: Exception) {}
        ui.postDelayed({ go() }, 4000)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionListener, CastSession::class.java)
        super.onDestroy()
    }
}
