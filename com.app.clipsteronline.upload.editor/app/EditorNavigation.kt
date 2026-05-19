package com.app.clipsteronline.upload.editor.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Navigation helper for the video editor.
 * Handles navigation between editor screens safely with proper intent handling.
 */
object EditorNavigation {

    private const val ACTION_OPEN_EDITOR = "upload.editor.ACTION_OPEN_EDITOR"
    private const val ACTION_OPEN_EXPORT = "upload.editor.ACTION_OPEN_EXPORT"
    private const val EXTRA_PROJECT_URI = "project_uri"
    private const val EXTRA_TIMELINE_CLIPS = "timeline_clips"

    /**
     * Flags for navigation behavior.
     */
    object Flags {
        const val CLEAR_TOP = Intent.FLAG_ACTIVITY_CLEAR_TOP
        const val NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK
        const val SINGLE_TOP = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    /**
     * Create an intent to open the editor screen.
     *
     * @param context The context to create the intent from.
     * @param projectUri Optional URI of the project to load.
     * @return An intent configured to open the editor screen.
     */
    fun createEditorIntent(context: Context, projectUri: Uri? = null): Intent {
        return Intent(context, EditorActivity::class.java).apply {
            action = ACTION_OPEN_EDITOR
            projectUri?.let { putExtra(EXTRA_PROJECT_URI, it) }
        }
    }

    /**
     * Create an intent to open the export screen.
     *
     * @param context The context to create the intent from.
     * @param timelineClips Clips to export.
     * @return An intent configured to open the export screen.
     */
    fun createExportIntent(context: Context, timelineClips: List<Clip>): Intent {
        return Intent(context, EditorActivity::class.java).apply {
            action = ACTION_OPEN_EXPORT
            putExtra(EXTRA_TIMELINE_CLIPS, ArrayList(timelineClips))
        }
    }

    /**
     * Navigate to the editor screen.
     *
     * @param context The context to navigate from.
     * @param projectUri Optional URI of the project to load.
     * @param flags Optional intent flags.
     */
    fun openEditor(context: Context, projectUri: Uri? = null, flags: Int = 0) {
        val intent = createEditorIntent(context, projectUri)
        if (flags != 0) {
            intent.addFlags(flags)
        }
        safeStartActivity(context, intent)
    }

    /**
     * Navigate to the export screen.
     *
     * @param context The context to navigate from.
     * @param timelineClips Clips to export.
     * @param flags Optional intent flags.
     */
    fun openExport(context: Context, timelineClips: List<Clip>, flags: Int = 0) {
        val intent = createExportIntent(context, timelineClips)
        if (flags != 0) {
            intent.addFlags(flags)
        }
        safeStartActivity(context, intent)
    }

    /**
     * Navigate back from the editor.
     *
     * @param activity The activity to navigate back from.
     * @param resultData Optional data to return to the caller.
     */
    fun navigateBack(activity: Activity, resultData: Intent? = null) {
        resultData?.let {
            activity.setResult(Activity.RESULT_OK, it)
        } ?: activity.setResult(Activity.RESULT_CANCELED)
        activity.finish()
    }

    /**
     * Extract project URI from the given intent.
     *
     * @param intent The intent to extract from.
     * @return The project URI, or null if not present.
     */
    fun getProjectUri(intent: Intent?): Uri? {
        return intent?.getParcelableExtra(EXTRA_PROJECT_URI, Uri::class.java)
    }

    /**
     * Extract timeline clips from the given intent.
     *
     * @param intent The intent to extract from.
     * @return The list of clips, or empty list if not present.
     */
    fun getTimelineClips(intent: Intent?): List<Clip> {
        @Suppress("UNCHECKED_CAST")
        return intent?.getParcelableArrayListExtra(EXTRA_TIMELINE_CLIPS, Clip::class.java) ?: emptyList()
    }

    /**
     * Check if the intent targets the export screen.
     *
     * @param intent The intent to check.
     * @return True if the intent targets export, false otherwise.
     */
    fun isExportIntent(intent: Intent?): Boolean {
        return intent?.action == ACTION_OPEN_EXPORT
    }

    /**
     * Safely start an activity, handling edge cases.
     */
    private fun safeStartActivity(context: Context, intent: Intent) {
        try {
            if (context is Activity) {
                context.startActivity(intent)
            } else {
                intent.addFlags(Flags.NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Log error in production - activity not found or not accessible
            throw IllegalStateException("Cannot start editor activity: ${e.message}", e)
        }
    }
}