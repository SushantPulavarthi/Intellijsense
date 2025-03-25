package com.github.sushantpulavarthi.ollamaCompletion

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.OptionsBuilder
import java.net.http.HttpTimeoutException

/**
 * Represents the Ollama model
 */
class OllamaModel {
    private val host = "http://localhost:11434/"
    private val ollamaAPI = OllamaAPI(host)
    private val model: String = "codellama:7b-code"
    private val requestTimeout: Long = 60

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
    fun getSuggestion(prefix: String): String {
        // https://ollama.com/blog/how-to-prompt-code-llama
        val streamer = ollamaAPI.generate(
            model,
            "<PRE> $prefix <SUF> <MID>",
            false,
            OptionsBuilder()
                .setTemperature(0.8F)
                .setTopP(0.9F)
                .setTopK(40)
                .build()
        )


        return try {
            // May end in <EOT>
            streamer.response.substringBefore("<EOT>")
        } catch (e: HttpTimeoutException) {
            "Ollama API request timed out."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}