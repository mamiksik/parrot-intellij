package com.mamiksik.parrot.config

data class PluginState(
    var inferenceApiUrl: String = "https://api-inference.huggingface.co/models/mamiksik/CommitPredictor",
    var inferenceApiToken: String = ""
)