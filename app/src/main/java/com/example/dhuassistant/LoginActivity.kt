package com.example.dhuassistant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                Log.d("WEBVIEW_URL", url ?: "null")

                // ✅ 真正进入 jwgl 系统后再结束
                if (url != null &&
                    url.contains("jwgl.dhu.edu.cn") &&
                    !url.contains("casLogin")
                ) {
                    val cookie = CookieManager.getInstance()
                        .getCookie("https://jwgl.dhu.edu.cn")

                    Log.d("JWGL_COOKIE", cookie ?: "null")

                    // 返回 MainActivity
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        webView.loadUrl("https://jwgl.dhu.edu.cn/dhu/")
    }
}
