package com.app.clipsteronline.upload.editor.app

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class EditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }

    companion object {
        const val EXTRA_SELECTED_MEDIA = "selectedMedia"
    }
}
