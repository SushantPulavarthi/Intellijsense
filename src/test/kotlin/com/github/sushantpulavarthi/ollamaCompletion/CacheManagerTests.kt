package com.github.sushantpulavarthi.ollamaCompletion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class CacheManagerTests {
    private val cacheManager: CacheManager = CacheManager()

    private fun getRecent(): String {
        val recencyField = CacheManager::class.java.getDeclaredField("recencyList")
        recencyField.isAccessible = true
        val recencyList = recencyField.get(cacheManager) as LinkedList<PrefixNode>
        return recencyList.last.word
    }

    private fun setCacheSize(size: Int) {
        val maxCapacityField = CacheManager::class.java.getDeclaredField("maxCapacity")
        maxCapacityField.isAccessible = true
        maxCapacityField.set(cacheManager, size)
    }

    @Test
    fun `test basic put and get`() {
        val prefix = "key"
        val completion = "value"
        cacheManager.put(prefix, completion)
        assertEquals(cacheManager.get(prefix), completion)
        assertEquals(cacheManager.size, 1)
    }

    @Test
    fun `test get on empty cache`() {
        assertNull(cacheManager.get("value"))
    }

    @Test
    fun `test recency management`() {
        cacheManager.put("key", "value")
        cacheManager.put("key2", "value2")
        assertEquals(cacheManager.get("key"), "value")
        assertEquals(getRecent(), "value")
        cacheManager.put("key3", "value3")
        assertEquals(cacheManager.get("key3"), "value3")
        assertEquals(cacheManager.get("key2"), "value2")
        assertEquals(getRecent(), "value2")
    }

    @Test
    fun `test cache overflow`() {
        setCacheSize(2)

        cacheManager.put("key", "value")
        cacheManager.put("key2", "value2")
        cacheManager.put("key3", "value3")
        assertEquals(cacheManager.get("key"), null)
        assertEquals(cacheManager.get("key2"), "value2")
        assertEquals(cacheManager.get("key3"), "value3")
        assertEquals(cacheManager.size, 2)
    }

    @Test
    fun `test cache LRU evictions`() {
        setCacheSize(2)

        cacheManager.put("key", "value")
        cacheManager.put("key2", "value2")
        cacheManager.put("key3", "value3")
        assertEquals(cacheManager.get("key"), null)
        assertEquals(cacheManager.get("key2"), "value2")
        assertEquals(cacheManager.get("key3"), "value3")
        assertEquals(cacheManager.size, 2)

        assertEquals(cacheManager.get("key2"), "value2")
        cacheManager.put("key4", "value4")
        assertEquals(cacheManager.size, 2)
        assertEquals(cacheManager.get("key"), null)
        assertEquals(cacheManager.get("key3"), null)
        assertEquals(cacheManager.get("key2"), "value2")
        assertEquals(cacheManager.get("key4"), "value4")
    }

    @Test
    fun `test code scenario`() {
        val prefix = "fun main() {"
        val completion = "println(\"Hello, world!\") }"
        val completion2 = "println(\"Welcome\") }"
        cacheManager.put(prefix, completion)
        cacheManager.put(prefix, completion2)
        assertEquals(cacheManager.get(prefix), completion2)
        assertEquals(cacheManager.get("fun main() { println(\"Hello,"), "world!\") }")
    }
}