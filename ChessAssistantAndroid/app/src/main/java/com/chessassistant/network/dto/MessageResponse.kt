package com.chessassistant.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Claude API messages endpoint.
 */
@Serializable
data class MessageResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ResponseContent>,
    val model: String,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("stop_sequence")
    val stopSequence: String? = null,
    val usage: Usage? = null
)

@Serializable
data class ResponseContent(
    val type: String,
    val text: String? = null
)

@Serializable
data class Usage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int
)

/**
 * Error response from Claude API.
 */
@Serializable
data class ErrorResponse(
    val type: String,
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val type: String,
    val message: String
)

/**
 * Extension to get the text content from a response.
 */
fun MessageResponse.getTextContent(): String? {
    return content.firstOrNull { it.type == "text" }?.text
}
