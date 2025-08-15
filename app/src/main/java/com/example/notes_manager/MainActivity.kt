package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.core.network.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        lifecycleScope.launch {
            val out = StringBuilder()

            // 1) POST — создать запись
            run {
                val payload = """
                    {
                      "title": "hello",
                      "body": "from android",
                      "userId": 1
                    }
                """.trimIndent()

                val res = postJson(
                    context = this@MainActivity,
                    url = "https://httpbin.org/anything",
                    payloadJson = """{ "check": "token" }"""
                )
                out.appendLine("POST /posts -> ${res.code}")
                out.appendLine(res.bodyFirst200)
                out.appendLine()
            }

            // 2) PUT — полная замена
            run {
                val payload = """
                    {
                      "id": 1,
                      "title": "updated via PUT",
                      "body": "full replace",
                      "userId": 1
                    }
                """.trimIndent()

                val res = putJson(
                    context = this@MainActivity,
                    url = "https://jsonplaceholder.typicode.com/posts/1",
                    payloadJson = payload
                )
                out.appendLine("PUT /posts/1 -> ${res.code}")
                out.appendLine(res.bodyFirst200)
                out.appendLine()
            }

            // 3) PATCH — частичное обновление
            run {
                val payload = """{ "title": "patched title" }"""
                val res = patchJson(
                    context = this@MainActivity,
                    url = "https://jsonplaceholder.typicode.com/posts/1",
                    payloadJson = payload
                )
                out.appendLine("PATCH /posts/1 -> ${res.code}")
                out.appendLine(res.bodyFirst200)
                out.appendLine()
            }

            // 4) DELETE — удаление
            run {
                val code = deleteRequest(
                    context = this@MainActivity,
                    url = "https://jsonplaceholder.typicode.com/posts/1"
                )
                out.appendLine("DELETE /posts/1 -> $code")
                out.appendLine("(ожидай 200 или 204; тело обычно пустое)")
            }

            tv.text = out.toString()
        }
    }
}
