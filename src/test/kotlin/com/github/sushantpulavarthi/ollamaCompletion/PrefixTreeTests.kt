package com.github.sushantpulavarthi.ollamaCompletion

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PrefixTreeTests {
    private val prefixTree: PrefixTree = PrefixTree()

    @Test
    fun `test basic insert and search`() {
        val end = prefixTree.insert("key", "value")
        assertEquals(end.word, "value")
        val found = prefixTree.search("key value")
        assertNotNull(found)
        assertEquals(found!!.word, "value")
    }

    @Test
    fun `test search on empty tree`() {
        assertNull(prefixTree.search("key value"))
    }

    @Test
    fun `test remove`() {
        val node = prefixTree.insert("key", "value")
        prefixTree.remove(node)
        assertNull(prefixTree.search("key value"))
    }

    @Test
    fun `test remove with children`() {
        val node = prefixTree.insert("key", "value")
        prefixTree.insert("key value", "value2")
        prefixTree.remove(node)
        assertNotNull(prefixTree.search("key value"))
    }

    @Test
    fun `test search`() {
        prefixTree.insert("key", "value")
        assertNotNull(prefixTree.search("key value"))
    }

    @Test
    fun `test getting cached completion`() {
        prefixTree.insert("key", "value")
        val node = prefixTree.search("key")
        assertNotNull(node)
        val (completion, _) = prefixTree.getCompletion(node!!) ?: Pair("", null)
        assertEquals(completion, "value")
    }

    @Test
    fun `test insert with similar key`() {
        prefixTree.insert("key", "value")
        prefixTree.insert("key", "value2")
        val keyNode = prefixTree.search("key")
        assertNotNull(keyNode)
        assertEquals(keyNode!!.children.size, 2)
    }

    @Test
    fun `test insert with different prefixes`() {
        prefixTree.insert("key", "value")
        prefixTree.insert("key2", "value2")
        assertEquals(prefixTree.root.children.size, 2)
    }

    @Test
    fun `test insert with similar completions`() {
        val prefix = "int x = "
        prefixTree.insert(prefix, "1 + 2 + 3 + 4")
        prefixTree.insert(prefix, "1 + 2 + 3 + 4 + 5 + 6")
        prefixTree.insert(prefix, "1 + 2 + 3 + 5")

        val prefixNode = prefixTree.search(prefix)
        assertNotNull(prefixNode)
        assertEquals(prefixNode!!.children.size, 1)
        println(prefixTree)

        val split = prefixTree.search(prefix + "1 + 2 + 3 + ")
        assertNotNull(split)
        assertEquals(split!!.children.size, 2)
    }
}