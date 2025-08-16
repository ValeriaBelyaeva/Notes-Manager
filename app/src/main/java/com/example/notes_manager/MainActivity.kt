package com.example.notes_manager

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.core.nb.Resource
import com.example.notes_manager.data.NotesRepository
import com.example.notes_manager.ui.NotesRoomAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var adapter: NotesRoomAdapter
    private lateinit var repo: NotesRepository

    private var streamJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        progress = findViewById(R.id.progress)
        rv = findViewById(R.id.rv)

        adapter = NotesRoomAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        repo = NotesRepository(this)

        // Подписка: сначала Room, потом сеть и апдейт Room
        streamJob?.cancel()
        streamJob = lifecycleScope.launch {
            repo.notesStream(page = 1, limit = 50, query = null)
                .collectLatest { res ->
                    when (res) {
                        is Resource.Loading -> {
                            progress.visibility = View.VISIBLE
                            tvStatus.text = "Обновляем из сети…"
                            res.data?.let { adapter.submitList(it) }
                        }
                        is Resource.Success -> {
                            progress.visibility = View.GONE
                            tvStatus.text = "Готово"
                            adapter.submitList(res.data)
                        }
                        is Resource.Error -> {
                            progress.visibility = View.GONE
                            tvStatus.text = "Ошибка сети: ${res.throwable.message ?: "неизвестно"} (показан кэш)"
                            res.data?.let { adapter.submitList(it) }
                        }
                    }
                }
        }
    }
}
