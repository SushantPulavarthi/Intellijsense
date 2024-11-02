package com.github.sushantpulavarthi.intellijsense

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.types.OllamaModelType
import kotlinx.coroutines.*

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
                You are a inline code completion assistant, tasked with completing the user's code snippet.

                Rules:
                1. Provide ONLY the code needed to complete the snippet; DO NOT INCLUDE additional text or explanations.
                2. Start your response with the first character that should immediately follow the snippet.
                3. NEVER use any markdown formatting in your response, including triple backticks.
                4. Do not include any comments, explanations or unnecessary text.
                5. Do not talk to the user, just provide the code completion.
                6. Do not worry about code in the provided snippet, NEVER try to edit or modify this.

                IMPORTANT: Your response must NEVER begin or end with triple backticks, single backticks, or any other formatting characters. Only return the code completion.
                
                Complete the following code STRICTLY following the rules: $prefix
            """,
            false,
        )
        val response = StringBuilder()

        while (streamer.isAlive) {
            val tokens = streamer.stream.poll()
            if (tokens != null) {
                response.append(tokens)
            }
            delay(1000)
        }
        return response.toString().trim()
    }
}