@file:Suppress("UnstableApiUsage")

package com.github.sushantpulavarthi.ollamaCompletion

import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Provide inline AI auto suggest completions
 */
class CompletionProvider : DebouncedInlineCompletionProvider() {
    private val ollamaModel: OllamaModel = OllamaModel()
    private val cacheManager: CacheManager = CacheManager()

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("OllamaInlineCompletionProvider")

    override fun isEnabled(event: InlineCompletionEvent): Boolean = true

    override suspend fun getDebounceDelay(request: InlineCompletionRequest): Duration {
        return 600.milliseconds
    }

    /**
     * Get suggestion for the given request
     * Checks cache first, then queries the model if nothing found
     * @param request Inline completion request
     * @return Inline completion suggestion
     */
    override suspend fun getSuggestionDebounced(request: InlineCompletionRequest): InlineCompletionSuggestion {
        if (request.editor.project == null) return InlineCompletionSuggestion.empty()

        return InlineCompletionSuggestion.withFlow {
            val offset = request.startOffset + 1
            val prefix = request.document.text.substring(0, offset)
            if (prefix.isBlank()) return@withFlow
            val cacheSuggestion = cacheManager.get(prefix)
            if (cacheSuggestion != null) {
                println("Cache hit for $prefix")
                emit(InlineCompletionGrayTextElement(cacheSuggestion))
            } else {
                println("Cache miss for $prefix")
                val suggestion = ollamaModel.getSuggestion(prefix)
                if (suggestion.isNotEmpty()) {
                    cacheManager.put(prefix, suggestion)
                }
                emit(InlineCompletionGrayTextElement(suggestion))
            }
        }
    }
}