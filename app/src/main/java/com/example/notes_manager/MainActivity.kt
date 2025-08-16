package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.Network
import com.example.notes_manager.core.network.rawGet
import kotlinx.coroutines.launch
import okhttp3.Request
import java.io.IOException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        lifecycleScope.launch {
            val out = StringBuilder()

            // sanity
            try {
                val ping = rawGet(this@MainActivity, "https://httpbingo.org/get")
                out.appendLine("✓ httpbingo GET -> ${ping.code}")
            } catch (e: UnknownHostException) {
                tv.text = "DNS/интернет мёртв: ${e.message}"
                return@launch
            } catch (e: IOException) {
                tv.text = "Ошибка сети: ${e.message}"
                return@launch
            }
            out.appendLine()

            // ETag
            try {
                val first = rawGet(this@MainActivity, "https://httpbingo.org/etag/abc123")
                val etag = first.headers["ETag"]
                out.appendLine("1) /etag/abc123 -> code=${first.code}, ETag=$etag")

                val client = Network.client(this@MainActivity)
                val req = Request.Builder()
                    .url("https://httpbingo.org/etag/abc123")
                    .get()
                    .header("If-None-Match", etag ?: "")
                    .build()
                client.newCall(req).execute().use { resp ->
                    out.appendLine("2) If-None-Match -> code=${resp.code}")
                }
                out.appendLine()
            } catch (e: IOException) {
                out.appendLine("Ошибка на ETag: ${e.message}")
                out.appendLine()
            }

            // Cache-Control
            try {
                val client = Network.client(this@MainActivity)

                val req1 = Request.Builder().url("https://httpbingo.org/cache/5").get().build()
                client.newCall(req1).execute().use { resp ->
                    out.appendLine("3) cache/5: первый -> code=${resp.code}")
                    out.appendLine("   source: cache=${resp.cacheResponse != null}, network=${resp.networkResponse != null}")
                }

                val req2 = Request.Builder().url("https://httpbingo.org/cache/5").get().build()
                client.newCall(req2).execute().use { resp ->
                    out.appendLine("   второй -> code=${resp.code}")
                    out.appendLine("   source: cache=${resp.cacheResponse != null}, network=${resp.networkResponse != null}")
                }
            } catch (e: IOException) {
                out.appendLine("Ошибка на cache/5: ${e.message}")
            }

            tv.text = out.toString()
        }
    }
}
