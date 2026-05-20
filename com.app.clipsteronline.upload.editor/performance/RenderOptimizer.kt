package com.app.clipsteronline.upload.editor.performance

class RenderOptimizer {
    fun configure() = Unit

    fun optimize(stats: RenderStats): OptimizationPlan {
        val recommendedScale = when {
            stats.frameTimeMs > 22f -> 0.75f
            stats.frameTimeMs > 18f -> 0.9f
            else -> 1f
        }
        val shouldBatch = stats.drawCalls > 120
        val throttleEffects = stats.gpuBusyPercent > 90
        return OptimizationPlan(recommendedScale, shouldBatch, throttleEffects)
    }

    data class RenderStats(val frameTimeMs: Float, val drawCalls: Int, val gpuBusyPercent: Int)
    data class OptimizationPlan(val resolutionScale: Float, val enableBatching: Boolean, val throttleEffects: Boolean)
}
