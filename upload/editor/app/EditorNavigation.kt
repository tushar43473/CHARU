package upload.editor.app

import android.app.Activity
import android.content.Context
import android.content.Intent

object EditorNavigation {
    fun openEditor(context: Context, sourceUri: String, clearTop: Boolean = false) {
        require(sourceUri.isNotBlank()) { "sourceUri must not be blank" }
        val intent = Intent(context, EditorActivity::class.java)
            .putExtra(EditorActivity.EXTRA_SOURCE_URI, sourceUri)
            .addFlags(if (context !is Activity) Intent.FLAG_ACTIVITY_NEW_TASK else 0)
            .addFlags(if (clearTop) Intent.FLAG_ACTIVITY_CLEAR_TOP else 0)
        context.startActivity(intent)
    }

    fun openExport(activity: Activity, sessionId: String) {
        require(sessionId.isNotBlank()) { "sessionId must not be blank" }
        val intent = Intent(activity, EditorActivity::class.java)
            .putExtra(EditorActivity.EXTRA_OPEN_EXPORT, true)
            .putExtra(EditorActivity.EXTRA_SESSION_ID, sessionId)
        activity.startActivity(intent)
    }

    fun finishSafely(activity: Activity) {
        if (!activity.isFinishing && !activity.isDestroyed) activity.finish()
    }
}
