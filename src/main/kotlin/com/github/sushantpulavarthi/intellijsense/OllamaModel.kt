package com.github.sushantpulavarthi.intellijsense

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.types.OllamaModelType

class OllamaModel {
    private val host = "http://localhost:11434/"
    private val prompt = "How do I write a list in kotlin"
    private val ollamaAPI = OllamaAPI(host)

    init {
        ollamaAPI.setRequestTimeoutSeconds(10)
        checkReachable()
//        ollamaAPI.pullModel(OllamaModelType.LLAMA2)
    }

    fun getSuggestion(): String {
        checkReachable()

        val models = ollamaAPI.listModels()
        models.forEach { println(it.name) }
        return models.toString()
    }

    fun checkReachable() {
        if (!ollamaAPI.ping()) {
            throw Exception("Ollama API is not reachable")
        }
    }
}

