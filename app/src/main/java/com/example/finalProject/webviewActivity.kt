package com.example.finalProject

import android.content.Intent
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_webview.*

class webviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val url = intent.getStringExtra("url")
        Log.d("webActivity",url)
        web_view.webViewClient = WebViewClient()
        web_view.settings.domStorageEnabled = true
        web_view.settings.javaScriptEnabled = true
        web_view.loadUrl(url)




        val returnButton = findViewById<Button>(R.id.returnPrevious)
        returnButton.setOnClickListener {
            finish()
        }

        val returnCompleteTask = findViewById<Button>(R.id.returnList)
        returnCompleteTask.setOnClickListener {
            val intent = Intent(this, CompletedTaskActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @Override
    fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed(); // Ignore SSL certificate errors
    }
}
