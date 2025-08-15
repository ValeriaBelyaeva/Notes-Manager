package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.debugGet
import com.example.notes_manager.core.network.parseRetryAfterMillis
import com.example.notes_manager.core.network.rawGet
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        lifecycleScope.launch {
            val out = StringBuilder()

            // A) Убедиться, что debugGet падает на !2xx с понятной ошибкой
            run {
                try {
                    debugGet(this@MainActivity, "https://httpbin.org/status/404")
                    out.appendLine("Ожидали ошибку 404, но её нет")
                } catch (t: Throwable) {
                    out.appendLine("404 отловлен: ${t.message}")
                }
                out.appendLine()
            }

            // B) Retry-After: число секунд
            run {
                val url = "https://httpbin.org/response-headers?Retry-After=5&status=429"
                val raw = rawGet(this@MainActivity, url)
                val ra = parseRetryAfterMillis(raw.headers)
                out.appendLine("429 (seconds) code=${raw.code}, Retry-After(ms)=$ra")
                out.appendLine()
            }

            // C) Retry-After: HTTP‑date (на 7 секунд вперёд)
            run {
                val futureHttpDate = ZonedDateTime.now(ZoneOffset.UTC)
                    .plusSeconds(7)
                    .format(DateTimeFormatter.RFC_1123_DATE_TIME)

                val httpUrl = HttpUrl.Builder()
                    .scheme("https")
                    .host("httpbin.org")
                    .addPathSegments("response-headers")
                    .addQueryParameter("Retry-After", futureHttpDate)
                    .addQueryParameter("status", "429")
                    .build()

                val raw = rawGet(this@MainActivity, httpUrl.toString())
                val ra = parseRetryAfterMillis(raw.headers)
                out.appendLine("429 (date) code=${raw.code}, Retry-After(ms)=$ra")
                out.appendLine("Header: ${raw.headers["Retry-After"]}")
            }

            tv.text = out.toString()
        }
    }
}
