package com.chessassistant.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for Claude API messages endpoint.
 */
@Serializable
data class MessageRequest(
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: List<ContentBlock>
)

@Serializable
sealed class ContentBlock {
    @Serializable
    @SerialName("text")
    data class Text(
        val type: String = "text",
        val text: String
    ) : ContentBlock()

    @Serializable
    @SerialName("image")
    data class Image(
        val type: String = "image",
        val source: ImageSource
    ) : ContentBlock()
}

@Serializable
data class ImageSource(
    val type: String = "base64",
    @SerialName("media_type")
    val mediaType: String,
    val data: String
)

/**
 * Simplified request builder for common use cases.
 */
object MessageRequestBuilder {

    fun textOnly(
        model: String,
        maxTokens: Int,
        prompt: String
    ): MessageRequest {
        return MessageRequest(
            model = model,
            maxTokens = maxTokens,
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(ContentBlock.Text(text = prompt))
                )
            )
        )
    }

    fun withImage(
        model: String,
        maxTokens: Int,
        imageBase64: String,
        mediaType: String,
        prompt: String
    ): MessageRequest {
        return MessageRequest(
            model = model,
            maxTokens = maxTokens,
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(
                        ContentBlock.Image(
                            source = ImageSource(
                                mediaType = mediaType,
                                data = imageBase64
                            )
                        ),
                        ContentBlock.Text(text = prompt)
                    )
                )
            )
        )
    }
}
