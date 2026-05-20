package com.app.clipsteronline.upload.editor.core.model

data class AudioClip(val id:String,val path:String,val startMs:Long,val endMs:Long,val gain:Float=1f)
