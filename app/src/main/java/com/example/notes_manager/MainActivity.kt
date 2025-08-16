package com.example.notes_manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes_manager.data.NetResult
import com.example.notes_manager.data.Repo
import com.example.notes_manager.data.api.toDomain
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv = findViewById<TextView>(R.id.tv)
        val repo = Repo(this)

        lifecycleScope.launch {
            val sb = StringBuilder()

            // 1) GET ok + маппинг DTO→Domain
            when (val r = repo.loadPost(1)) {
                is NetResult.Ok -> {
                    val post = r.data.toDomain()
                    sb.appendLine("GET /posts/1 -> OK")
                    sb.appendLine("domain: id=${post.id}, author=${post.authorId}, title=${post.title}")
                }
                is NetResult.Err -> sb.appendLine("GET error: ${r.code ?: "-"} ${r.message}")
            }
            sb.appendLine()

            // 2) GET 404
            when (val r = repo.loadPost(999999)) {
                is NetResult.Ok -> sb.appendLine("GET /posts/999999 -> неожиданно OK")
                is NetResult.Err -> sb.appendLine("GET 404: ${r.code ?: "-"} ${r.message}")
            }
            sb.appendLine()

            // 3) POST + Authorization интерцептор
            when (val r = repo.createPost(7, "Hello", "Retrofit + kotlinx.serialization")) {
                is NetResult.Ok -> sb.appendLine("POST /posts -> OK, id=${r.data.id}, title=${r.data.title}")
                is NetResult.Err -> sb.appendLine("POST error: ${r.code ?: "-"} ${r.message}")
            }

            tv.text = sb.toString()
        }
    }
}
