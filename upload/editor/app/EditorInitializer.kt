package upload.editor.app

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout

class EditorInitializer(private val appContext: Context) {
    data class Dependencies(
        val playerController: PlayerController,
        val timelineController: TimelineController,
        val renderEngine: RenderEngine,
    )

    fun initialize(container: FrameLayout): Dependencies {
        require(container.parent != null || container.layoutParams != null) { "Container must be attached or measured" }

        val renderSurface = SurfaceView(appContext).apply {
            setZOrderOnTop(false)
            holder.setKeepScreenOn(true)
        }
        container.removeAllViews()
        container.addView(
            renderSurface,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )

        val renderEngine = RenderEngine(renderSurface)
        val playerController = PlayerController(renderEngine)
        val timelineController = TimelineController(playerController)

        renderEngine.start()
        return Dependencies(playerController, timelineController, renderEngine)
    }

    class PlayerController(private val renderEngine: RenderEngine) {
        private var source: String? = null
        private var currentPositionMs: Long = 0L

        fun attachMedia(sourceUri: String) {
            val normalized = Uri.parse(sourceUri).toString()
            require(normalized.isNotBlank()) { "Media source must not be blank" }
            renderEngine.prepare(normalized)
            source = normalized
            currentPositionMs = 0L
        }

        fun seekTo(positionMs: Long) {
            require(positionMs >= 0L) { "positionMs must be >= 0" }
            check(source != null) { "No media attached" }
            currentPositionMs = positionMs
        }

        fun currentPositionMs(): Long = currentPositionMs
    }

    class TimelineController(private val playerController: PlayerController) {
        fun seekTo(positionMs: Long) {
            playerController.seekTo(positionMs)
        }

        fun currentPositionMs(): Long = playerController.currentPositionMs()
    }

    class RenderEngine(private val surfaceView: SurfaceView) {
        private var started = false

        fun start() {
            check(!started) { "RenderEngine already started" }
            started = true
        }

        fun prepare(source: String) {
            check(started) { "RenderEngine is not started" }
            require(source.isNotBlank()) { "source must not be blank" }
            check(surfaceView.holder.surface.isValid) { "Surface is unavailable" }
        }
    }
}
