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
        val prefix = "key "
        val completion = "value"
        cacheManager.put(prefix, completion)
        assertEquals(completion, cacheManager.get(prefix))
        assertEquals(1, cacheManager.size)
    }

    @Test
    fun `test get on empty cache`() {
        assertNull(cacheManager.get("value"))
    }

    @Test
    fun `test item moved to front when accessed`() {
        cacheManager.put("key ", "value")
        cacheManager.put("key2 ", "value2")
        assertEquals("value", cacheManager.get("key "))
        assertEquals("value", getRecent())
        cacheManager.put("key3 ", "value3")
        assertEquals("value3", cacheManager.get("key3 "))
        assertEquals("value2", cacheManager.get("key2 "))
        assertEquals("value2", getRecent())
    }

    @Test
    fun `test cache LRU evictions`() {
        setCacheSize(2)

        cacheManager.put("key ", "value")
        cacheManager.put("key2 ", "value2")
        cacheManager.put("key3 ", "value3")
        assertEquals(null, cacheManager.get("key "))
        assertEquals("value2", cacheManager.get("key2 "))
        assertEquals("value3", cacheManager.get("key3 "))
        assertEquals(2, cacheManager.size)

        assertEquals("value2", cacheManager.get("key2 "))
        cacheManager.put("key4 ", "value4")
        assertEquals(2, cacheManager.size)
        assertEquals(null, cacheManager.get("key "))
        assertEquals(null, cacheManager.get("key3 "))
        assertEquals("value2", cacheManager.get("key2 "))
        assertEquals("value4", cacheManager.get("key4 "))
    }

    @Test
    fun `test code scenario`() {
        val prefix = "fun main() { "
        val completion = "println(\"Hello, world!\") }"
        val completion2 = "println(\"Welcome\") }"
        cacheManager.put(prefix, completion)
        cacheManager.put(prefix, completion2)
        assertEquals(completion2, cacheManager.get(prefix))
        assertEquals("world!\") }", cacheManager.get("fun main() { println(\"Hello, "))
    }

    @Test
    fun `test code scenario with partial completion`() {
        val prefix = "if (n <= 1) "
        val completion = "return n"
        cacheManager.put(prefix, completion)
        assertEquals(completion, cacheManager.get(prefix))
        assertEquals(" <= 1) return n", cacheManager.get("if (n"))
    }

    @Test
    fun `properly handles strings just over max prefix size`() {
        val prefix = "a".repeat(400) + ": "
        val completion = "b"
        cacheManager.put(prefix, completion)
        assertEquals(completion, cacheManager.get(prefix))
    }

    @Test
    fun `properly handles strings over max prefix size`() {
        val prefix = "a".repeat(600) + ": "
        val completion = "b"
        cacheManager.put(prefix, completion)
        assertEquals(completion, cacheManager.get(prefix))
    }
}