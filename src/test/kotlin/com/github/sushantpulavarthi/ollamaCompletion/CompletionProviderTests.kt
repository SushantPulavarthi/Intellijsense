package com.github.sushantpulavarthi.ollamaCompletion

import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

// Requires ollama model to be running, as I cant mock private variables of a mocked object
class CompletionProviderTests {
    @Mock
    private lateinit var request: InlineCompletionRequest

    @Mock
    private lateinit var ollamaModel: OllamaModel

    @Mock
    private lateinit var cacheManager: CacheManager

    private val ollamaInlineCompletionProvider = OllamaInlineCompletionProvider()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Injecting mocks into completion provider
        val ollamaModelField = OllamaInlineCompletionProvider::class.java.getDeclaredField("ollamaModel")
        val cacheManagerField = OllamaInlineCompletionProvider::class.java.getDeclaredField("cacheManager")
        ollamaModelField.setAccessible(true)
        cacheManagerField.setAccessible(true)
        ollamaModelField.set(ollamaInlineCompletionProvider, ollamaModel)
        cacheManagerField.set(ollamaInlineCompletionProvider, cacheManager)

        whenever(request.document).thenReturn(mock())
        whenever(request.document.text).thenReturn("text")
        whenever(request.startOffset).thenReturn(0)
    }

    @Test
    fun testCacheMiss() {
        runBlocking {
            whenever(cacheManager.get(any())).thenReturn(null)
            whenever(ollamaModel.getSuggestion(any())).thenReturn("suggestion")
            val suggestion = ollamaInlineCompletionProvider.getSuggestionDebounced(request)
            suggestion.suggestionFlow.collect {}
            verify(cacheManager).put(any(), eq("suggestion"))
        }
    }

    @Test
    fun testCacheHit() {
        runBlocking {
            whenever(cacheManager.get(any())).thenReturn("cached suggestion")
            val suggestion = ollamaInlineCompletionProvider.getSuggestionDebounced(request)
            suggestion.suggestionFlow.collect {}
            verify(ollamaModel, never()).getSuggestion(any())
        }
    }
}