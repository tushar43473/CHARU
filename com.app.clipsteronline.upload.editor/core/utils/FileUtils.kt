package com.app.clipsteronline.upload.editor.core.utils

import java.io.File

object FileUtils { fun ensureDir(path:String): File = File(path).apply { mkdirs() } }
