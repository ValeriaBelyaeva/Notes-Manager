package com.example.notes_manager.core.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun EditText.textChangesFlow(): Flow<String> = callbackFlow {
    val w = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { trySend(s?.toString() ?: "") }
        override fun afterTextChanged(s: Editable?) {}
    }
    addTextChangedListener(w); awaitClose { removeTextChangedListener(w) }
}
