package com.example.notes_manager

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.core.paging.Page
import com.example.notes_manager.core.paging.Paginator
import com.example.notes_manager.core.ui.textChangesFlow
import com.example.notes_manager.data.NotesRepo
import com.example.notes_manager.domain.Post
import com.example.notes_manager.ui.NotesAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var rv: RecyclerView
    private lateinit var btnMore: Button
    private lateinit var adapter: NotesAdapter

    private lateinit var repo: NotesRepo
    private lateinit var paginator: Paginator<Int, Post>

    private var nextKey: Int? = null
    private var currentQuery: String? = null
    private val pageSize = 20
    private val items = mutableListOf<Post>()
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var isLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repo = NotesRepo(this)
        paginator = Paginator { key ->
            repo.loadPage(
                page = key,
                limit = pageSize,
                query = currentQuery,
                sort = "id",
                order = "asc"
            )
        }

        etSearch = findViewById(R.id.etSearch)
        rv = findViewById(R.id.rv)
        btnMore = findViewById(R.id.btnMore)

        adapter = NotesAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // поиск с дебаунсом 300 мс
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            etSearch.textChangesFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .onEach { query ->
                    currentQuery = query.ifBlank { null }
                    items.clear()
                    adapter.submitList(items.toList())
                    nextKey = null
                    btnMore.isEnabled = true
                    loadMore()
                }
                .collect { }
        }

        btnMore.setOnClickListener { loadMore() }

        if (savedInstanceState == null) {
            lifecycleScope.launch { currentQuery = null; loadMore() }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnMore.isEnabled = !loading && nextKey != null
        btnMore.text = when {
            loading -> "Загрузка…"
            nextKey == null -> "Больше нет"
            else -> "Ещё"
        }
    }

    private fun applyPage(page: Page<Int, Post>) {
        nextKey = page.nextKey
        items += page.items
        adapter.submitList(items.toList())
        setLoading(false)
    }

    private fun showError(text: String) {
        btnMore.text = text
        btnMore.isEnabled = true
    }

    private fun loadMore() {
        if (isLoading) return
        if (nextKey == null && items.isNotEmpty()) { setLoading(false); return }

        val queryAtStart = currentQuery
        isLoading = true
        setLoading(true)

        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            runCatching { paginator.next(nextKey) }
                .onSuccess { page ->
                    // если за время запроса строка поиска изменилась — игнорим старый результат
                    if (currentQuery == queryAtStart) applyPage(page) else setLoading(false)
                }
                .onFailure { showError("Ошибка: ${it.message ?: "неизвестно"}") }
            isLoading = false
        }
    }

}
