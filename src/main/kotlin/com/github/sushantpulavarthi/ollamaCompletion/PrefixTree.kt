package com.github.sushantpulavarthi.ollamaCompletion

/**
 * Represents child node in prefix tree - also contains reference to parent node
 * @param word Value contained in the node
 */
data class PrefixNode(val word: String) {
    val children: MutableMap<String, PrefixNode> = mutableMapOf()
    var parent: PrefixNode? = null

    /**
     * Adds a child to the current node
     * @param word Value of the child node
     * @return Newly created child node
     */
    fun addChild(word: String): PrefixNode {
        if (children.containsKey(word)) {
            return children[word]!!
        }
        val newNode = PrefixNode(word)
        children[word] = newNode
        newNode.parent = this
        return newNode
    }
}

/**
 * Prefix Tree data structure that holds completions as nodes of words
 */
class PrefixTree {
    val root = PrefixNode("")

    /**
     * Inserts given prefix and completion into the prefix tree
     * Should not be called with empty strings
     * @param prefix Prefix to insert
     * @param completion Completion to insert
     * @return End node of the completion
     */
    fun insert(prefix: String, completion: String): PrefixNode {
        // Naive word splitting - would be better to split by actual syntax tokens
        val words = prefix.split(" ") + completion.split(" ")

        var current = root
        for (word in words) {
            if (word.isBlank()) continue
            current = current.addChild(word)
        }

        return current
    }

    /**
     * Removes given node from the tree
     * If it does not have children - removes the node and relevant ancestors
     * @param node Node to remove
     */
    fun remove(node: PrefixNode) {
        if (node == root) return
        if (node.children.isNotEmpty()) return
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
    fun getCompletion(start: PrefixNode): Pair<String, PrefixNode>? {
        if (start.children.isEmpty()) return null
        val completion = StringBuilder()
        var current = start
        while (current.children.isNotEmpty()) {
            current = current.children.values.last()
            completion.append(current.word).append(" ")
        }
        return Pair(completion.toString().trim(), current)
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
            sb.append(node.word).append("\n")

            node.children.values.forEach { printNode(it, depth + 1) }
        }

        root.children.values.forEach { printNode(it, 0) }
        return sb.toString()
    }
}