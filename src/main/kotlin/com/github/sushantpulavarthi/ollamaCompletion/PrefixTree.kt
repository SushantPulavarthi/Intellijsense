package com.github.sushantpulavarthi.ollamaCompletion

/**
 * Represents child node in prefix tree - also contains reference to parent node
 * @param word Value contained in the node
 */
data class PrefixNode(val word: String, var isCompletion: Boolean) {
    val children: MutableMap<String, PrefixNode> = mutableMapOf()
    var parent: PrefixNode? = null

    /**
     * Adds a child to the current node
     * @param word Value of the child node
     * @return Newly created child node
     */
    fun addChild(word: String, isCompletion: Boolean): PrefixNode {
        if (children.containsKey(word)) {
            return children[word]!!
        }
        val newNode = PrefixNode(word, isCompletion)
        children[word] = newNode
        newNode.parent = this
        return newNode
    }
}

/**
 * Prefix Tree data structure that holds completions as nodes of words
 */
class PrefixTree {
    val root = PrefixNode("", false)

    /**
     * Inserts given prefix and completion into the prefix tree
     * Should not be called with empty strings
     * @param prefix Prefix to insert
     * @param completion Completion to insert
     * @return End node of the completion
     */
    fun insert(prefix: String, completion: String): PrefixNode {
        val prefixWords: List<String>
        val completionWords: List<String>
        if (prefix.last() != ' ') {
            prefixWords = prefix.split(" ").dropLast(1)
            completionWords = (prefix.split(" ").last() + completion).split(" ")
        } else {
            prefixWords = prefix.split(" ")
            completionWords = completion.split(" ")
        }

        // Naive word splitting - would be better to split by actual syntax tokens
        val prefixEnd = addWords(prefixWords, root, false)
        return addWords(completionWords, prefixEnd, true)
    }

    /**
     * Inserts given list of words into the prefix tree and marks whether they are completions
     * Should not be called with empty strings
     * @param words List of words to insert
     * @param start Node to start from
     * @param isCompletion Whether the string is a completion or not
     * @return End node of the completion
     */
    private fun addWords(words: List<String>, start: PrefixNode, isCompletion: Boolean): PrefixNode {
        var current = start
        for (word in words) {
            if (word.isBlank()) continue
            current = current.addChild(word, isCompletion)
        }

        return current
    }

    /**
     * Removes given node from the tree
     * If it does not have children - removes the node and relevant ancestors
     * @param node Node to remove
     */
    fun remove(node: PrefixNode) {
        if (node == root || node.children.isNotEmpty()) return
        val parent = node.parent ?: return
        parent.children.remove(node.word)
        node.parent = null
        remove(parent)
    }

    /**
     * Given a string, searches tree to find end node of the string
     * @param toFind String to search for
     * @param start Node to start searching from
     * @return End node of the string if found, otherwise null
     */
    fun search(toFind: String, start: PrefixNode = root): PrefixNode? {
        if (toFind.isBlank()) return start
        val words = toFind.split(" ")
        var current = start
        for (word in words) {
            if (word.isBlank()) continue
            current = current.children[word] ?: return null
        }
        return current
    }

    /**
     * Given a start node, traverses the last child of each child node to get completion and final child node
     * @param start Node to start from
     * @return Pair of completion string and end node if it is a valid completion end, otherwise null
     */
    private fun traverseTree(start: PrefixNode): Pair<String, PrefixNode>? {
        if (start.children.isEmpty()) return null
        val completion = mutableListOf<String>()
        var current = start
        while (current.children.isNotEmpty()) {
            current = current.children.values.last { it.isCompletion }
            completion.add(current.word)
        }
        return Pair(completion.joinToString(" "), current)
    }

    /**
     * Given a prefix string, searches tree to find the end and then traverses rest to get completion string
     * @param prefix Prefix to search for
     * @return Pair of completion string and end node if it is a valid completion end, otherwise null
     */
    fun getCompletion(prefix: String): Pair<String, PrefixNode>? {
        if (prefix.isBlank()) return null
        if (prefix.last() != ' ') {
            return getPartialCompletion(prefix)
        } else {
            val prefixNode = search(prefix) ?: return null
            return traverseTree(prefixNode)
        }
    }

    /**
     * Takes a prefix string that ends in the middle of a word and returns relevant completion string
     * @param prefix Prefix to search for
     * @return Pair of completion string and end node if it is a valid completion end, otherwise null
     */
    private fun getPartialCompletion(prefix: String): Pair<String, PrefixNode>? {
        // Handle case where request is sent from middle of a word
        val words = prefix.split(" ")
        val complete = words.dropLast(1)
        val incomplete = words.last()

        val prefixNode = search(complete.joinToString(" ")) ?: return null
        if (prefixNode.children.isEmpty()) return null

        val potential = prefixNode.children.filter { it.key.startsWith(incomplete) && it.value.isCompletion }
        if (potential.isEmpty()) return null

        val child = potential.values.last()
        val strippedPrefix = child.word.removePrefix(incomplete)
        if (child.children.isEmpty()) return Pair(strippedPrefix, child) // Handles case where completion is final word
        val (completion, completionNode) = traverseTree(child) ?: return null // Should not be null
        return Pair("$strippedPrefix $completion", completionNode)
    }

    /**
     * Prints in a tree-like form with indentation scope line for the children of each node
     */
    override fun toString(): String {
        if (root.children.isEmpty()) return ""

        val sb = StringBuilder()
        fun printNode(node: PrefixNode, depth: Int) {
            // Depth 0 has no preceding |
            if (depth > 0) sb.append("|  ".repeat(depth))
            sb.append(node.word).append(node.isCompletion).append("\n")

            node.children.values.forEach { printNode(it, depth + 1) }
        }

        root.children.values.forEach { printNode(it, 0) }
        return sb.toString()
    }
}