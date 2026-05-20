package com.app.clipsteronline.upload.editor.core.utils

object TimeUtils { fun clamp(ms:Long,min:Long,max:Long)=ms.coerceIn(min,max) }
