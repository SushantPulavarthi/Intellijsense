package com.github.sushantpulavarthi.ollamaCompletion

import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionElement
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// Requires ollama model to be running, as I cant mock private variables of a mocked object
@RunWith(MockitoJUnitRunner::class)
class CompletionProviderTests {
    @Mock
    private lateinit var ollamaModel: OllamaModel

    @Mock
    private lateinit var cacheManager: CacheManager

    @Mock
    private lateinit var request: InlineCompletionRequest

    @Mock
    private lateinit var editor: com.intellij.openapi.editor.Editor

    @Mock
    private lateinit var project: com.intellij.openapi.project.Project

    @Mock
    private lateinit var document: com.intellij.openapi.editor.Document

    private val ollamaInlineCompletionProvider = CompletionProvider()

    @Before
    fun setUp() {
        // Injecting mocks into completion provider
        val ollamaModelField = CompletionProvider::class.java.getDeclaredField("ollamaModel")
        ollamaModelField.setAccessible(true)
        ollamaModelField.set(ollamaInlineCompletionProvider, ollamaModel)

        val cacheManagerField = CompletionProvider::class.java.getDeclaredField("cacheManager")
        cacheManagerField.setAccessible(true)
        cacheManagerField.set(ollamaInlineCompletionProvider, cacheManager)

        whenever(request.editor).thenReturn(editor)
        whenever(editor.project).thenReturn(project)
        whenever(request.document).thenReturn(document)
    }

    @Test
    fun `test retrieves from model if cache empty`() {
        runBlocking {
            val prefix = "prefix"
            val modelSuggestion = "suggestion"

            whenever(cacheManager.get(prefix)).thenReturn(null)
            whenever(ollamaModel.getSuggestion(prefix)).thenReturn(modelSuggestion)
            whenever(document.text).thenReturn(prefix)
            whenever(request.startOffset).thenReturn(prefix.length - 1)

            val suggestion = ollamaInlineCompletionProvider.getSuggestionDebounced(request)
            val elements = mutableListOf<InlineCompletionElement>()
            suggestion.suggestionFlow.collect { elements.add(it) }

            assertEquals(1, elements.size)
            assertEquals(modelSuggestion, elements[0].text)

            verify(cacheManager).get(prefix)
            verify(ollamaModel).getSuggestion(prefix)
            verify(cacheManager).put(prefix, modelSuggestion)
        }
    }

    @Test
    fun `test retrieves from cache if found`() {
        runBlocking {
            val prefix = "prefix"
            val cachedSuggestion = "suggestion"

            whenever(cacheManager.get(prefix)).thenReturn(cachedSuggestion)
            whenever(document.text).thenReturn(prefix)
            whenever(request.startOffset).thenReturn(prefix.length - 1)

            val suggestion = ollamaInlineCompletionProvider.getSuggestionDebounced(request)
            val elements = mutableListOf<InlineCompletionElement>()
            suggestion.suggestionFlow.collect { elements.add(it) }

            assertEquals(1, elements.size)
            assertEquals(cachedSuggestion, elements[0].text)

            verify(cacheManager).get(prefix)
            verify(ollamaModel, never()).getSuggestion(any())
        }
    }
}