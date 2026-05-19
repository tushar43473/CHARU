package com.app.clipsteronline.upload.editor.app

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main editor activity for the video editor.
 * Initializes the editor UI, connects player and timeline, and handles lifecycle.
 */
class EditorActivity : Activity() {

    private lateinit var initializer: EditorInitializer
    private lateinit var viewModel: EditorViewModel

    private var playerView: VideoPlayerView? = null
    private var timelineView: TimelineView? = null

    private val contentView: FrameLayout by lazy {
        FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEdgeToEdge()
        initializeEditor()
        createUI()
        observeState()
        handleIntent()
    }

    /**
     * Configure edge-to-edge display.
     * Sets dark background and enables edge-to-edge content.
     */
    private fun setupEdgeToEdge() {
        // Set dark black background
        window.decorView.setBackgroundColor(Color.BLACK)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make status and navigation bars transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK

        // Set light status bar icons for dark background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    /**
     * Initialize the editor components.
     */
    private fun initializeEditor() {
        initializer = EditorInitializer(this)

        val initResult = initializer.initialize()
        if (initResult is EditorInitializer.InitResult.Error) {
            throw IllegalStateException("Failed to initialize editor: ${initResult.message}")
        }

        viewModel = EditorViewModel(initializer)
    }

    /**
     * Create the editor UI programmatically.
     */
    private fun createUI() {
        // Create player view
        playerView = VideoPlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            bindPlayer(initializer.getPlayer())
        }

        // Create timeline view
        timelineView = TimelineView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            bindTimeline(initializer.getTimeline())
        }

        // Add views to content view
        contentView.addView(playerView)
        contentView.addView(timelineView)

        // Set content view
        setContentView(contentView)

        // Apply window insets to content view
        applyWindowInsets()
    }

    /**
     * Apply window insets to handle edge-to-edge properly.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, insets.top, 0, insets.bottom)

            // Apply insets to player view
            playerView?.let { player ->
                val playerParams = player.layoutParams as FrameLayout.LayoutParams
                playerParams.topMargin = insets.top
                player.layoutParams = playerParams
            }

            // Apply insets to timeline view
            timelineView?.let { timeline ->
                val timelineParams = timeline.layoutParams as FrameLayout.LayoutParams
                timelineParams.bottomMargin = insets.bottom
                timeline.layoutParams = timelineParams
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Observe editor state changes.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUI(state)
            }
        }

        // Handle lifecycle events
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onPause_()
                Lifecycle.Event.ON_RESUME -> onResume_()
                Lifecycle.Event.ON_DESTROY -> onDestroy_()
                else -> {}
            }
        })
    }

    /**
     * Update UI based on state.
     */
    private fun updateUI(state: EditorState) {
        // Update player view with current playback info
        playerView?.updatePlayback(state.isPlaying, state.playbackInfo.currentPositionMs)

        // Update timeline view with current state
        timelineView?.updateState(state)

        // Handle export state changes
        when (val exportState = state.exportState) {
            is ExportState.Success -> handleExportSuccess(exportState)
            is ExportState.Error -> handleExportError(exportState)
            else -> {}
        }

        // Handle error state
        state.error?.let { error ->
            handleError(error)
        }
    }

    /**
     * Handle incoming intents.
     */
    private fun handleIntent() {
        val projectUri = EditorNavigation.getProjectUri(intent)
        if (projectUri != null) {
            loadProject(projectUri)
        }

        if (EditorNavigation.isExportIntent(intent)) {
            val clips = EditorNavigation.getTimelineClips(intent)
            if (clips.isNotEmpty()) {
                handleExportIntent(clips)
            }
        }
    }

    /**
     * Load a project from URI.
     */
    private fun loadProject(uri: android.net.Uri) {
        // Load project data
        viewModel.setSaving(true)
    }

    /**
     * Handle export intent.
     */
    private fun handleExportIntent(clips: List<Clip>) {
        // Navigate to export screen with clips
    }

    /**
     * Handle export success.
     */
    private fun handleExportSuccess(state: ExportState.Success) {
        viewModel.setSaving(true)
    }

    /**
     * Handle export error.
     */
    private fun handleExportError(state: ExportState.Error) {
        viewModel.clearError()
    }

    /**
     * Handle editor error.
     */
    private fun handleError(error: EditorError) {
        viewModel.clearError()
    }

    /**
     * Pause handler.
     */
    private fun onPause_() {
        viewModel.setPlaying(false)
    }

    /**
     * Resume handler.
     */
    private fun onResume_() {
        // Resume playback if it was playing before
    }

    /**
     * Destroy handler.
     */
    private fun onDestroy_() {
        viewModel.release()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        viewModel.setPlaying(false)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        playerView?.release()
        timelineView?.release()
        super.onDestroy()
    }

    /**
     * Get the editor ViewModel.
     */
    fun getViewModel(): EditorViewModel = viewModel
}

/**
 * Video player view for displaying video frames.
 */
class VideoPlayerView(context: android.content.Context) : FrameLayout(context) {

    private var player: MediaPlayer? = null

    init {
        setBackgroundColor(Color.BLACK)
    }

    fun bindPlayer(player: MediaPlayer) {
        this.player = player
    }

    fun updatePlayback(isPlaying: Boolean, positionMs: Long) {
        // Update playback display
    }

    fun release() {
        player = null
    }
}

/**
 * Timeline view for displaying and editing the timeline.
 */
class TimelineView(context: android.content.Context) : FrameLayout(context) {

    private var timeline: TimelineManager? = null
    private var currentState: EditorState? = null

    init {
        setBackgroundColor(Color.parseColor("#1A1A1A"))
    }

    fun bindTimeline(timeline: TimelineManager) {
        this.timeline = timeline
    }

    fun updateState(state: EditorState) {
        this.currentState = state
        // Update timeline display with current state
    }

    fun release() {
        timeline = null
    }
}