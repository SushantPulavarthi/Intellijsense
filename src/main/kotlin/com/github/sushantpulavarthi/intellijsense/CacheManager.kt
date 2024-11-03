package com.github.sushantpulavarthi.intellijsense

import java.util.LinkedList

class CacheManager {
    private val maxPrefixSize = 400
    private val maxCapacity = 100

    private val cache = mutableMapOf<String, String>()
    private val frequency: LinkedList<String> = LinkedList()

    @Synchronized
    fun get(key: String): String? {
        val value = cache[key.takeLast(maxPrefixSize)]
        return value?.also {
            frequency.remove(key)
            frequency.add(key)
        }
    }

    @Synchronized
    fun put(key: String, value: String) {
        if (cache.size >= maxCapacity) {
            val leastFrequent = frequency.poll()
            cache.remove(leastFrequent)
        }
        cache[key.takeLast(maxPrefixSize)] = value
    }
}