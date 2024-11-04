package com.github.sushantpulavarthi.ollamaCompletion

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.types.OllamaModelType
import kotlinx.coroutines.*

class OllamaModel {
    private val host = "http://localhost:11434/"
    private val ollamaAPI = OllamaAPI(host)
    private val model: String = OllamaModelType.CODELLAMA
    private val requestTimeout: Long = 60
    private val prompt = """
        You are an code completion assistant, tasked with providing code to complete a given code snippet.

        Rules:
        1. Provide ONLY the code necessary to complete the snippet.
        2. Your response MUST start with the character that follows the snippet.
        3. NEVER use any markdown formatting in your response.
        4. DO NOT include any comments, explanations or text.
        5. You DO NOT communicate to the user, just provide the code completion suggestion.
        6. DO NOT edit or modify the code in the snippet.

        IMPORTANT: Your response must NEVER begin or end with triple backticks, single backticks, or any other formatting characters. Only return the code completion, NO comments.
    """.trimIndent()

    init {
        if (!ollamaAPI.ping()) {
            throw Exception("Ollama API is not reachable.")
        }
        try {
            ollamaAPI.getModelDetails(model)
        } catch (e: Exception) {
            throw Exception("Model $model is not available. Pull the model from the Ollama API server.")
        }
        ollamaAPI.setRequestTimeoutSeconds(requestTimeout)
    }

    suspend fun getSuggestion(prefix: String): String {
        val streamer = ollamaAPI.generateAsync(
            OllamaModelType.CODELLAMA,
            prompt + prefix,
            false,
        )

        // Wait till model finishes generating the response
        while (streamer.isAlive) {
            delay(500)
        }

        return streamer.completeResponse.trim()
    }
}