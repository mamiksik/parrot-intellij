package com.mamiksik.parrot.config

data class PluginSettingsState(
    var inferenceApiUrl: String = "https://api-inference.huggingface.co/models/mamiksik/CommitPredictor",
    var inferenceApiToken: String = ""
)