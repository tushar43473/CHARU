package com.app.clipsteronline.upload.editor.core.cache

class ThumbnailCache(max:Int=200): MemoryCache<String,ByteArray>(max)
