package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.Network
import com.example.notes_manager.core.network.rawGet
import kotlinx.coroutines.launch
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        lifecycleScope.launch {
            val out = StringBuilder()

            // 1) Первый запрос: получаем ETag
            val first = rawGet(this@MainActivity, "https://httpbin.org/cache")
            val etag = first.headers["ETag"]
            out.appendLine("1) Первый запрос /cache -> code=${first.code}")
            out.appendLine("   ETag: $etag")
            out.appendLine("   Первые 50 символов тела: ${first.body.take(50)}")
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
                    out.appendLine("2) Повтор с If-None-Match -> code=${resp.code}")
                    if (resp.code == 304) {
                        out.appendLine("   Ответ: 304 Not Modified (тело не присылается)")
                    } else {
                        out.appendLine("   Тело (первые 50): ${resp.body?.string().orEmpty().take(50)}")
                    }
                    out.appendLine()
                }
            } else {
                out.appendLine("2) Сервер не прислал ETag, пропускаем If-None-Match тест")
                out.appendLine()
            }

            // 3) Cache-Control: ответ кэшируется 5 секунд
            run {
                val firstCache = rawGet(this@MainActivity, "https://httpbin.org/cache/5")
                out.appendLine("3) Cache-Control тест (5 сек): первый -> code=${firstCache.code}")
                out.appendLine("   Тело (первые 50): ${firstCache.body.take(50)}")

                // Второй запрос сразу после первого — при корректной настройке кэша OkHttp
                // иногда отдаст тот же контент. Код может по-прежнему быть 200 (это нормально).
                val secondCache = rawGet(this@MainActivity, "https://httpbin.org/cache/5")
                out.appendLine("   второй -> code=${secondCache.code}")
                out.appendLine("   Тело (первые 50): ${secondCache.body.take(50)}")
                out.appendLine()
                out.appendLine("Подсказка: чтобы явно использовать дисковый HTTP-кэш, в Network.client уже настроен Cache(10 MB).")
            }

            tv.text = out.toString()
        }
    }
}
