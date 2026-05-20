package com.app.clipsteronline.upload.editor.core.cache

import java.io.File

class DiskCache(private val root: File){ fun file(key:String)=File(root,key) }
