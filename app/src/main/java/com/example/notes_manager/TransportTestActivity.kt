package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.SecureRetrofitFactory
import com.example.notes_manager.data.api.SecuredProbeApi
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownServiceException

class TransportTestActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    // 1) ЗАМЕНИ на свой боевой хост и правильные пины
    private val host = "api.example.com"
    private val baseUrl = "https://api.example.com/"
    private val pins = listOf(
        // подставь реальные пины вида "sha256/BASE64..."
        "sha256/REPLACE_WITH_REAL_PIN_1",
        "sha256/REPLACE_WITH_REAL_PIN_2"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport_test)
        tv = findViewById(R.id.tvLog)

        lifecycleScope.launch {
            val sb = StringBuilder()

            // A) HTTPS с пиннингом (должен пройти при правильных пинах)
            runCatching {
                val retrofit = SecureRetrofitFactory.retrofit(this@TransportTestActivity, baseUrl, host, pins)
                val api = retrofit.create(SecuredProbeApi::class.java)
                // Любой публичный URL твоего API (корень тоже ок, даже если 404, нам важен TLS)
                api.get(baseUrl.removeSuffix("/"))
            }.onSuccess {
                sb.appendLine("HTTPS pinned: OK — TLS+пиннинг пройден, тело получено/прочитано.")
            }.onFailure { e ->
                sb.appendLine("HTTPS pinned: FAIL — ${e::class.simpleName}: ${e.message}")
            }

            // B) Пробуем HTTP (должно падать по cleartext policy)
            runCatching {
                val client = OkHttpClient() // без разницы, policy сработает до клиента
                val req = Request.Builder().url(baseUrl.replace("https://", "http://")).build()
                client.newCall(req).execute().use { it.body?.string() }
            }.onSuccess {
                sb.appendLine("HTTP: ОЖИДАЛСЯ провал, но пришёл ответ — проверь usesCleartextTraffic=false.")
            }.onFailure { e ->
                val ok = e is UnknownServiceException && e.message?.contains("CLEARTEXT") == true
                sb.appendLine(
                    if (ok) "HTTP: OK — CLEARTEXT запрещён политикой безопасности."
                    else "HTTP: FAIL — ${e::class.simpleName}: ${e.message}"
                )
            }

            tv.text = sb.toString()
        }
    }
}
