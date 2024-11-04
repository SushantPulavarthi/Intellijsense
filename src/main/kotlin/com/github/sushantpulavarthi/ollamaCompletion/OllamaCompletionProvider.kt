@file:Suppress("UnstableApiUsage")

package com.github.sushantpulavarthi.ollamaCompletion

import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import java.net.http.HttpTimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class OllamaInlineCompletionProvider : DebouncedInlineCompletionProvider() {
    private val ollamaModel: OllamaModel = OllamaModel()
    private val cacheManager: CacheManager = CacheManager()

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("OllamaInlineCompletionProvider")

    override suspend fun getDebounceDelay(request: InlineCompletionRequest): Duration {
        return 600.milliseconds
    }

    override suspend fun getSuggestionDebounced(request: InlineCompletionRequest): InlineCompletionSuggestion {
        return InlineCompletionSuggestion.withFlow {
            val offset = request.startOffset + 1
            val prefix = request.document.text.substring(0, offset)
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
                    emit(InlineCompletionGrayTextElement("Ollama API request timed out."))
                } catch (e: Exception) {
                    emit(InlineCompletionGrayTextElement("Error: ${e.message}"))
                }
            }
        }
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return true
    }
}