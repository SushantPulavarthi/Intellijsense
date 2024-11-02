@file:Suppress("UnstableApiUsage")

package com.github.sushantpulavarthi.intellijsense

import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionElement
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionElementManipulator
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.openapi.util.TextRange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class OllamaInlineCompletionProvider : InlineCompletionProvider {
    private val ollamaModel = OllamaModel()

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("OllamaInlineCompletionProvider")

    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        return InlineCompletionSuggestion.withFlow {
            emit(InlineCompletionGrayTextElement(ollamaModel.getSuggestion()))
        }
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return true
    }
}