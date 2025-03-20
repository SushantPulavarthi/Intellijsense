package com.github.sushantpulavarthi.ollamaCompletion

import java.util.LinkedList

/**
 * Represents cache using an internal prefix tree
 * Also contains a linked list of completion nodes to handle an LRU eviction policy
 */
class CacheManager {
    private val maxPrefixSize = 400
    private val maxCapacity = 100

    private val recencyList: LinkedList<PrefixNode> = LinkedList()
    private val prefixTree = PrefixTree()

    /**
     * Get number of completion in cache
     */
    val size: Int
        get() = recencyList.size

    /**
     * Get completion for a given prefix
     * If it exists, it is moved to the front of the recency list
     * @param prefix Prefix to search for
     * @return Completion for the prefix, or null if not found
     */
    fun get(prefix: String): String? {
        // Find prefix in tree then use it to get completion
        val prefixNode = prefixTree.search(prefix.takeLast(maxPrefixSize)) ?: return null
        val (completion, completionNode) = prefixTree.getCompletion(prefixNode) ?: return null

        // Move to front of recency list
        recencyList.remove(completionNode)
        recencyList.add(completionNode)

        return completion
    }

    /**
     * Adds a completion to the cache
     * If it already exists, it is moved to the front of the recency list
     * If the cache is full, the least recently used completion is removed
     * @param prefix Prefix to add
     * @param completion Completion to add
     */
    fun put(prefix: String, completion: String) {
        if (prefix.isBlank() || completion.isBlank()) return

        // Evict least recently used completion if cache is full
        if (recencyList.size >= maxCapacity) {
            val leastFrequent = recencyList.poll()
            prefixTree.remove(leastFrequent)
        }

        // Insert new completion
        val node = prefixTree.insert(prefix.takeLast(maxPrefixSize), completion)
        recencyList.add(node)
    }
}