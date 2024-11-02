@file:Suppress("UnstableApiUsage")

package com.github.sushantpulavarthi.intellijsense

import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement

class OllamaInlineCompletionProvider : InlineCompletionProvider {
    private val ollamaModel = OllamaModel()

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("OllamaInlineCompletionProvider")

    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        return InlineCompletionSuggestion.withFlow {
            val code = request.document.text
            val offset = request.startOffset + 1
            val prefix = code.substring(0, offset)
            println("Getting suggestion")
            val suggestion = ollamaModel.getSuggestion(prefix)
            println("Suggestion: $suggestion")
            emit(InlineCompletionGrayTextElement(suggestion))
        }
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return true
    }
}