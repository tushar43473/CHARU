package com.app.clipsteronline.upload.editor.import

class MediaCompressor {
    fun configure() = Unit

    fun compress(request: CompressionRequest): CompressionResult {
        if (request.sizeBytes <= 0) return CompressionResult(request.uri, request.uri, 0L, false, "invalid-size")
        val ratio = when (request.profile) {
            CompressionProfile.LOSSLESS -> 0.95
            CompressionProfile.BALANCED -> 0.65
            CompressionProfile.AGGRESSIVE -> 0.4
        }
        val outputSize = (request.sizeBytes * ratio).toLong().coerceAtLeast(1L)
        val outputUri = request.uri + "?compressed=" + request.profile.name.lowercase()
        return CompressionResult(request.uri, outputUri, outputSize, true, null)
    }

    data class CompressionRequest(val uri: String, val sizeBytes: Long, val profile: CompressionProfile)
    data class CompressionResult(val inputUri: String, val outputUri: String, val outputSizeBytes: Long, val success: Boolean, val error: String?)

    enum class CompressionProfile { LOSSLESS, BALANCED, AGGRESSIVE }
}
