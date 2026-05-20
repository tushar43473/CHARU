package upload.editor.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class EditorActivity : ComponentActivity() {
    private val viewModel: EditorViewModel by viewModels()

    private lateinit var root: FrameLayout
    private lateinit var previewContainer: FrameLayout
    private lateinit var timelineContainer: FrameLayout
    private lateinit var initializer: EditorInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        root = buildRoot()
        setContentView(root)

        initializer = EditorInitializer(applicationContext)
        val dependencies = initializer.initialize(previewContainer)

        val sourceUri = intent.getStringExtra(EXTRA_SOURCE_URI)
        viewModel.initializeSession(sourceUri)
        sourceUri?.takeIf { it.isNotBlank() }?.let { dependencies.playerController.attachMedia(it) }
        dependencies.timelineController.seekTo(0L)
        viewModel.seekTo(0L)

        observeState()
    }

    private fun buildRoot(): FrameLayout {
        val height = resources.displayMetrics.heightPixels
        val previewHeight = (height * 0.65f).toInt()

        return FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)

            previewContainer = FrameLayout(context).apply {
                setBackgroundColor(Color.BLACK)
                addView(View(context).apply { setBackgroundColor(Color.BLACK) })
            }
            addView(previewContainer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, previewHeight))

            timelineContainer = FrameLayout(context).apply {
                setBackgroundColor(Color.BLACK)
            }
            addView(
                timelineContainer,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    topMargin = previewHeight
                },
            )

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> insets }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    if (state.playback.isPlaying) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    companion object {
        const val EXTRA_SOURCE_URI = "extra_source_uri"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_OPEN_EXPORT = "extra_open_export"
    }
}
