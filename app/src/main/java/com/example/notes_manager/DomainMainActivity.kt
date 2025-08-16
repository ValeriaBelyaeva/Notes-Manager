package com.example.notes_manager

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.domain.error.Outcome
import com.example.notes_manager.data.repo.NotesRepositoryImpl
import com.example.notes_manager.domain.usecase.GetNotesUseCase
import com.example.notes_manager.ui.domain.DomainNotesAdapter
import kotlinx.coroutines.launch

class DomainMainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var adapter: DomainNotesAdapter

    // Собираем доменный репозиторий и use-case без DI
    private val domainRepo by lazy { NotesRepositoryImpl(this) }
    private val getNotes by lazy { GetNotesUseCase(domainRepo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_domain_main)

        tvStatus = findViewById(R.id.tvStatus)
        progress = findViewById(R.id.progress)
        rv = findViewById(R.id.rv)

        adapter = DomainNotesAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Подписка на поток Outcome<List<Note>>
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                getNotes(page = 1, limit = 50, query = null).collect { outcome ->
                    when (outcome) {
                        is Outcome.Loading -> {
                            progress.visibility = View.VISIBLE
                            tvStatus.text = "Обновляем…"
                            outcome.data?.let { adapter.submitList(it) }
                        }
                        is Outcome.Success -> {
                            progress.visibility = View.GONE
                            tvStatus.text = "Готово"
                            adapter.submitList(outcome.data)
                        }
                        is Outcome.Error -> {
                            progress.visibility = View.GONE
                            tvStatus.text = "Ошибка: ${outcome.error.javaClass.simpleName} (показан кэш, если был)"
                            outcome.data?.let { adapter.submitList(it) }
                        }
                    }
                }
            }
        }
    }
}
