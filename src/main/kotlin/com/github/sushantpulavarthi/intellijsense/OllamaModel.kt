package com.github.sushantpulavarthi.intellijsense

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.types.OllamaModelType
import kotlinx.coroutines.delay

class OllamaModel {
    private val host = "http://localhost:11434/"
    private val ollamaAPI = OllamaAPI(host)
    private val model: String = OllamaModelType.CODELLAMA

    init {
        if (!ollamaAPI.ping()) {
            throw Exception("Ollama API is not reachable.")
        }
        // Make sure the model is available
        try {
            ollamaAPI.getModelDetails(model)
        } catch (e: Exception) {
            throw Exception("Model $model is not available. Pull the model from the Ollama API server.")
        }
        ollamaAPI.setRequestTimeoutSeconds(60)
    }

    suspend fun getSuggestion(prefix: String): String {
        val streamer = ollamaAPI.generateAsync(
            OllamaModelType.CODELLAMA,
            """
                You are an code completion assistant, tasked with providing code to complete a given code snippet.

                Rules:
                1. Provide ONLY the code needed to complete the snippet; DO NOT INCLUDE additional text or explanations.
                2. Your response MUST start with the character that follows the snippet.
                3. NEVER use any markdown formatting your response, including triple backticks.
                4. DO NOT include any comments, explanations or text.
                5. You DO NOT communicate to the user, just provide the code completion suggestion.
                6. DO NOT worry about code in the provided snippet, NEVER try to edit or modify it.

                IMPORTANT: Your response must NEVER begin or end with triple backticks, single backticks, or any other formatting characters. Only return the code completion, NO comments.
                
                Complete the snippet, STRICTLY following the rules: 
                $prefix
            """,
            false,
        )

        // Wait till model finishes generating the response
        while (streamer.isAlive) {
            delay(500)
        }

        return streamer.completeResponse.trim()
    }
}