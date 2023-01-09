package com.mamiksik.parrot.config

data class PluginSettingsState(
    var fillTokenEndpoint: String = "http://127.0.0.1:8000/fill-token",
    var summarizeEndpoint: String = "http://127.0.0.1:8000/summarize",
    var inferenceApiToken: String = ""
)