package com.example.notes_manager.core.paging

data class Page<K, T>(val items: List<T>, val nextKey: K?)

class Paginator<Key : Any, Item : Any>(
    private val load: suspend (key: Key?) -> Page<Key, Item>
) {
    suspend fun next(current: Key?): Page<Key, Item> = load(current)
}
