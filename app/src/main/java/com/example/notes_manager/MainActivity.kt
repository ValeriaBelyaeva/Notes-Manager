package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.Network
import com.example.notes_manager.core.network.rawGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.Request
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        // чтобы вместо мгновенного краша видеть стек на экране
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
            runOnUiThread { tv.text = "FATAL:\n${sw.toString().take(4000)}" }
        }

        lifecycleScope.launch {
            val out = StringBuilder()

            // A) sanity
            try {
                val ping = rawGet(this@MainActivity, "https://httpbingo.org/get")
                out.appendLine("✓ sanity: httpbingo -> ${ping.code}")
            } catch (e: UnknownHostException) {
                tv.text = "DNS/интернет мёртв: ${e.message}"
                return@launch
            } catch (e: IOException) {
                tv.text = "Ошибка сети на sanity: ${e.message}"
                return@launch
            }
            out.appendLine()

            // B) ETag: первый запрос (уже уходит в IO внутри rawGet)
            try {
                val first = rawGet(this@MainActivity, "https://httpbingo.org/etag/abc123")
                val etag = first.headers["ETag"]
                out.appendLine("1) /etag/abc123 -> code=${first.code}, ETag=$etag")

                // второй запрос — SYNCHRONOUS execute, поэтому в IO!
                val client = Network.client(this@MainActivity)
                val req = Request.Builder()
                    .url("https://httpbingo.org/etag/abc123")
                    .get()
                    .header("If-None-Match", etag ?: "")
                    .build()

                val code304 = withContext(Dispatchers.IO) {
                    client.newCall(req).execute().use { resp -> resp.code }
                }
                out.appendLine("2) If-None-Match -> code=$code304")
                out.appendLine()
            } catch (t: Throwable) {
                out.appendLine("Ошибка на ETag: ${t::class.simpleName} ${t.message}")
                out.appendLine()
            }

            // C) Cache-Control: оба execute в IO
            try {
                val client = Network.client(this@MainActivity)

                val firstCodePair = withContext(Dispatchers.IO) {
                    val r1 = Request.Builder().url("https://httpbingo.org/cache/5").get().build()
                    client.newCall(r1).execute().use { resp ->
                        Triple(resp.code, resp.cacheResponse != null, resp.networkResponse != null)
                    }
                }
                out.appendLine("3) cache/5 first -> code=${firstCodePair.first}")
                out.appendLine("   source: cache=${firstCodePair.second}, network=${firstCodePair.third}")

                val secondCodePair = withContext(Dispatchers.IO) {
                    val r2 = Request.Builder().url("https://httpbingo.org/cache/5").get().build()
                    client.newCall(r2).execute().use { resp ->
                        Triple(resp.code, resp.cacheResponse != null, resp.networkResponse != null)
                    }
                }
                out.appendLine("   cache/5 second -> code=${secondCodePair.first}")
                out.appendLine("   source: cache=${secondCodePair.second}, network=${secondCodePair.third}")
                out.appendLine()
            } catch (t: Throwable) {
                out.appendLine("Ошибка на cache/5: ${t::class.simpleName} ${t.message}")
                out.appendLine()
            }

            tv.text = out.toString()
        }
    }
}
