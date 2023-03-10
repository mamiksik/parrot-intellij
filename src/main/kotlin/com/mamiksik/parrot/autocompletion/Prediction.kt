package com.mamiksik.parrot.autocompletion

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prediction (val score: Double, @SerialName("token_str") val prediction: String)