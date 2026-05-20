package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.graphics.Bitmap
import com.app.clipsteronline.upload.editor.player.FrameExtractor

class ThumbnailDecoder(
    private val frameExtractor: FrameExtractor = FrameExtractor(),
) {
    fun decode(path: String, frameUs: Long, width: Int, height: Int): Bitmap? {
        return frameExtractor.extractFrame(path, frameUs, width, height)
    }
}
