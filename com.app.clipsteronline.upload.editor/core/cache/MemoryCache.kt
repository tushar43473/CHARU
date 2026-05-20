package com.app.clipsteronline.upload.editor.core.cache

class MemoryCache<K,V>(private val max:Int=128){private val map=LinkedHashMap<K,V>(16,0.75f,true); @Synchronized fun put(k:K,v:V){map[k]=v; if(map.size>max) map.remove(map.entries.first().key)}; @Synchronized fun get(k:K):V?=map[k]}
