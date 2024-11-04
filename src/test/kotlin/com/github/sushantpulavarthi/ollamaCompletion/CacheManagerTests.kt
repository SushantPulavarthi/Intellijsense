package com.github.sushantpulavarthi.ollamaCompletion

import org.junit.Before
import org.junit.Test
import org.mockito.*
import java.util.*

class CacheManagerTests {
    private val cacheManager: CacheManager = CacheManager()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private fun getFrequency(): LinkedList<String> {
        val frequencyField = CacheManager::class.java.getDeclaredField("frequency")
        frequencyField.isAccessible = true
        return frequencyField.get(cacheManager) as LinkedList<String>
    }


    @Test
    fun testCachePut() {
        cacheManager.put("key", "value")
        assert(cacheManager.get("key") == "value")
    }

    @Test
    fun testCacheLRU() {
        cacheManager.put("key", "value")
        cacheManager.put("key2", "value2")
        assert(cacheManager.get("key") == "value")
        val frequency = getFrequency()
        assert(frequency.last == "key")
    }

    @Test
    fun testCacheOverflow() {
        val maxCapacityField = CacheManager::class.java.getDeclaredField("maxCapacity")
        maxCapacityField.isAccessible = true
        maxCapacityField.set(cacheManager, 2)

        cacheManager.put("key", "value")
        cacheManager.put("key2", "value2")
        cacheManager.put("key3", "value3")
        assert(cacheManager.get("key") == null)
        assert(cacheManager.get("key2") == "value2")
        assert(cacheManager.get("key3") == "value3")
        assert(getFrequency().size == 2)
    }
}