package com.chessassistant.network

import com.chessassistant.network.dto.MessageRequest
import com.chessassistant.network.dto.MessageResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for Anthropic Claude API.
 */
interface AnthropicApiService {

    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String,
        @Header("content-type") contentType: String = "application/json",
        @Body request: MessageRequest
    ): MessageResponse
}
