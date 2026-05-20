package com.app.clipsteronline.upload.editor.database

class EditorDatabase(
    val projectDao: ProjectDao = ProjectDao(),
    val clipDao: ClipDao = ClipDao(),
    val effectDao: EffectDao = EffectDao(),
    val mediaCacheDao: MediaCacheDao = MediaCacheDao(),
    val timelineStateDao: TimelineStateDao = TimelineStateDao(),
) {
    fun initialize() {
        projectDao.initialize()
        clipDao.initialize()
        effectDao.initialize()
        mediaCacheDao.initialize()
        timelineStateDao.initialize()
    }
}
