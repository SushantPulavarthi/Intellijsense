package com.github.sushantpulavarthi.ollamaCompletion

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PrefixTreeTests {
    private val prefixTree: PrefixTree = PrefixTree()

    private fun checkCompletion(prefix: String, completion: String) {
        val completionVal = prefixTree.getCompletion(prefix)
        assertNotNull(completionVal)
        assertEquals(completion, completionVal!!.first)
    }

    @Test
    fun `test basic insert and search`() {
        val end = prefixTree.insert("key ", "value")
        assertEquals("value", end.word)
        val found = prefixTree.search("key value")
        assertNotNull(found)
        assertEquals("value", found!!.word)
    }

    @Test
    fun `test search on empty tree`() {
        assertNull(prefixTree.search("key value"))
    }

    @Test
    fun `test getting completion on empty tree`() {
        assertNull(prefixTree.getCompletion("key value"))
    }

    @Test
    fun `test remove without children`() {
        val node = prefixTree.insert("key ", "value")
        prefixTree.remove(node)
        assertNull(prefixTree.search("key value"))
    }

    @Test
    fun `test remove with children`() {
        val node = prefixTree.insert("key ", "value")
        prefixTree.insert("key value ", "value2")
        prefixTree.remove(node)
        assertNotNull(prefixTree.search("key value"))
    }

    @Test
    fun `test getting cached completion`() {
        prefixTree.insert("key ", "value")
        val (completion, completionNode) = prefixTree.getCompletion("key ") ?: Pair("", null)
        assertNotNull(completionNode)
        assertEquals("value", completion)
    }

    @Test
    fun `test insert with same prefix`() {
        prefixTree.insert("key ", "value")
        prefixTree.insert("key ", "value2")
        val keyNode = prefixTree.search("key ")
        assertNotNull(keyNode)
        assertEquals(2, keyNode!!.children.size)
    }

    @Test
    fun `test insert with different prefixes`() {
        prefixTree.insert("key ", "value")
        prefixTree.insert("key2 ", "value2")
        assertEquals(2, prefixTree.root.children.size)
    }

    @Test
    fun `test insert with similar completions`() {
        val prefix = "int x = "
        prefixTree.insert(prefix, "1 + 2 + 3 + 4")
        prefixTree.insert(prefix, "1 + 2 + 3 + 4 + 5 + 6")
        prefixTree.insert(prefix, "1 + 2 + 3 + 5")

        val prefixNode = prefixTree.search(prefix)
        assertNotNull(prefixNode)
        assertEquals(1, prefixNode!!.children.size)

        val split = prefixTree.search(prefix + "1 + 2 + 3 + ")
        assertNotNull(split)
        assertEquals(2, split!!.children.size)
    }

    @Test
    fun `test partial completion request at first word`() {
        prefixTree.insert("pri", "nt( 'Hello World')")
        val completion = prefixTree.getCompletion("print")
        assertNotNull(completion)
        assertEquals( "( 'Hello World')",completion!!.first)
    }

    @Test
    fun `test partial completion request in middle`() {
        prefixTree.insert("print( ", "'Hello World')")
        checkCompletion("print( 'Hel", "lo World')")
    }

    @Test
    fun `test partial completion request at end`() {
        prefixTree.insert("print( ", "'Hello World')")
        checkCompletion("print( 'Hello Wor", "ld')")
    }

    @Test
    fun `test removal properly removes ancestors`() {
        prefixTree.insert("foo bar ", "baz")
        val endNode = prefixTree.search("foo bar baz")
        assertNotNull(endNode)
        prefixTree.remove(endNode!!)
        assertNull(prefixTree.search("foo bar baz"))
        assertNull(prefixTree.search("foo bar"))
        assertNull(prefixTree.search("foo"))
    }
}