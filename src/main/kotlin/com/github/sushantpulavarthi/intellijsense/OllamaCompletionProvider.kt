@file:Suppress("UnstableApiUsage")

package com.github.sushantpulavarthi.intellijsense

import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import java.net.http.HttpTimeoutException

class OllamaInlineCompletionProvider : InlineCompletionProvider {
    private val ollamaModel = OllamaModel()
    private val cacheManager = CacheManager()

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("OllamaInlineCompletionProvider")

    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        return InlineCompletionSuggestion.withFlow {
            val code = request.document.text
            val offset = request.startOffset + 1
            val prefix = code.substring(0, offset)
            val cacheSuggestion = cacheManager.get(prefix)
            if (cacheSuggestion != null) {
                emit(InlineCompletionGrayTextElement(cacheSuggestion))
            } else {
                try {
                    val suggestion = ollamaModel.getSuggestion(prefix)
                    if (suggestion.isNotEmpty()) {
                        cacheManager.put(prefix, suggestion)
                        emit(InlineCompletionGrayTextElement(suggestion))
                    }
                } catch (e: HttpTimeoutException) {
                    println("Ollama API request timed out.")
                }
            }
        }
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return true
    }
}