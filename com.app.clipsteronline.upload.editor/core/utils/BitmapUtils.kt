package com.app.clipsteronline.upload.editor.core.utils

object BitmapUtils { fun computeScaledSize(w:Int,h:Int,max:Int):Pair<Int,Int>{ val r=max.toFloat()/maxOf(w,h); return (w*r).toInt() to (h*r).toInt() } }
