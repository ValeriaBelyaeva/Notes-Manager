package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.Network
import com.example.notes_manager.core.network.rawGet
import kotlinx.coroutines.launch
import okhttp3.Request
import java.net.UnknownHostException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        lifecycleScope.launch {
            val out = StringBuilder()

            // A) sanity: простая проверка, что вообще есть сеть и DNS работает
            try {
                val ping = rawGet(this@MainActivity, "https://example.com")
                out.appendLine("✓ Подключение есть: example.com -> code=${ping.code}")
            } catch (e: UnknownHostException) {
                tv.text = "Нет интернета или DNS ломается: ${e.message}"
                return@launch
            } catch (e: IOException) {
                tv.text = "Ошибка сети при базовой проверке: ${e.message}"
                return@launch
            }
            out.appendLine()

            // 1) Первый запрос: получаем ETag
            try {
                val first = rawGet(this@MainActivity, "https://httpbin.org/cache")
                val etag = first.headers["ETag"]
                out.appendLine("1) /cache -> code=${first.code}")
                out.appendLine("   ETag: $etag")
                out.appendLine("   Тело(50): ${first.body.take(50)}")
                out.appendLine()

                // 2) Второй запрос с If-None-Match
                if (etag != null) {
                    val client = Network.client(this@MainActivity)
                    val req = Request.Builder()
                        .url("https://httpbin.org/cache")
                        .get()
                        .header("If-None-Match", etag)
                        .build()
                    client.newCall(req).execute().use { resp ->
                        out.appendLine("2) If-None-Match -> code=${resp.code}")
                        if (resp.code == 304) {
                            out.appendLine("   304 Not Modified")
                        } else {
                            out.appendLine("   Тело(50): ${resp.body?.string().orEmpty().take(50)}")
                        }
                        out.appendLine()
                    }
                } else {
                    out.appendLine("2) ETag не пришёл, тест If-None-Match пропускаем")
                    out.appendLine()
                }
            } catch (e: IOException) {
                out.appendLine("Ошибка сети на шаге 1/2: ${e.message}")
                out.appendLine()
            }

            // 3) Cache-Control: кэш на 5 секунд
            try {
                val firstCache = rawGet(this@MainActivity, "https://httpbin.org/cache/5")
                out.appendLine("3) cache/5: первый -> code=${firstCache.code}")
                out.appendLine("   Тело(50): ${firstCache.body.take(50)}")
                val secondCache = rawGet(this@MainActivity, "https://httpbin.org/cache/5")
                out.appendLine("   второй -> code=${secondCache.code}")
                out.appendLine("   Тело(50): ${secondCache.body.take(50)}")
                out.appendLine()
            } catch (e: IOException) {
                out.appendLine("Ошибка сети на шаге 3: ${e.message}")
                out.appendLine()
            }

            tv.text = out.toString()
        }
    }
}
