package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.data.NetResult
import com.example.notes_manager.data.Repo
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv = findViewById<TextView>(R.id.tv)
        val repo = Repo(this)

        lifecycleScope.launch {
            val sb = StringBuilder()

            // GET /posts/1
            when (val r = repo.loadPost(1)) {
                is NetResult.Ok -> {
                    sb.appendLine("GET /posts/1 -> OK")
                    sb.appendLine("id=${r.data.id}, title=${r.data.title}")
                }
                is NetResult.Err -> {
                    sb.appendLine("GET error: ${r.code ?: "-"} ${r.message}")
                }
            }
            sb.appendLine()

            // POST /posts
            when (val r = repo.createPost(userId = 7, title = "Hello", body = "Retrofit + kotlinx.serialization")) {
                is NetResult.Ok -> {
                    sb.appendLine("POST /posts -> OK")
                    sb.appendLine("new id=${r.data.id}, title=${r.data.title}")
                }
                is NetResult.Err -> {
                    sb.appendLine("POST error: ${r.code ?: "-"} ${r.message}")
                }
            }

            tv.text = sb.toString()
        }
    }
}
