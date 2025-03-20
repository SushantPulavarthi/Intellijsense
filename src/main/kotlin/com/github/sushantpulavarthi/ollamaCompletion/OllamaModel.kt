package com.github.sushantpulavarthi.ollamaCompletion

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.types.OllamaModelType
import kotlinx.coroutines.*
import java.net.http.HttpTimeoutException

/**
 * Represents the Ollama model
 */
class OllamaModel {
    private val host = "http://localhost:11434/"
    private val ollamaAPI = OllamaAPI(host)
    private val model: String = OllamaModelType.CODELLAMA
    private val requestTimeout: Long = 60
    private val prompt = """
        You are an intelligent programmer and well-versed with coding. Your goal is to finish off the code that follows the given input.

        Rules:
        1. Provide ONLY the code necessary to complete the snippet. If given an incomplete word, only finish it off do not rewrite it.
        2. Your response MUST start with the character that follows the snippet.
        3. NEVER use any markdown formatting in your response.
        4. DO NOT include any comments, explanations or text.
        5. You DO NOT communicate to the user, just provide the code completion.
        6. DO NOT edit or modify the code in the snippet. Use only the given imports and follow the coding standards from the snippet.

        IMPORTANT: Your response must NEVER begin or end with triple backticks, single backticks, or any other formatting characters. Only return the code completion, NO comments or explanations.
        Your response must contain ONLY the continuation to the snippet given.

        <snippet>
    """.trimIndent()

    /**
     * Initialize the Ollama model
     * Validates model and API connection
     */
    init {
        if (!ollamaAPI.ping()) throw Exception("Ollama API is not reachable.")
        try {
            ollamaAPI.getModelDetails(model)
        } catch (e: Exception) {
            throw Exception("Model $model is not available. Pull the model from the Ollama API server.")
        }
        ollamaAPI.setRequestTimeoutSeconds(requestTimeout)
    }

    /**
     * Queries local Ollama model for suggestion
     * @param prefix Prefix to get suggestion for
     * @return Autocomplete suggestion
     */
    suspend fun getSuggestion(prefix: String): String {
        println("$prompt\n$prefix\n<\\snippet>")
        val streamer = ollamaAPI.generateAsync(
            model,
            "$prompt\n$prefix\n<\\snippet>",
            false,
        )

        // Wait till model finishes generating the response
        while (streamer.isAlive) {
            delay(500)
        }

        return try {
            streamer.completeResponse.trim()
        } catch (e: HttpTimeoutException) {
            "Ollama API request timed out."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}