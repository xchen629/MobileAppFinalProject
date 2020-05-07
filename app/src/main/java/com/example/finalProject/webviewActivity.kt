package com.example.finalProject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Button
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_webview.*

class webviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val url = "https://www.google.com/"
        web_view.webViewClient = WebViewClient()
        web_view.loadUrl(url)


        val returnButton = findViewById<Button>(R.id.returnPrevious)
        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val returnCompleteTask = findViewById<Button>(R.id.returnList)
        returnCompleteTask.setOnClickListener {
            val intent = Intent(this, CompletedTaskActivity::class.java)
            startActivity(intent)
        }
    }
}
