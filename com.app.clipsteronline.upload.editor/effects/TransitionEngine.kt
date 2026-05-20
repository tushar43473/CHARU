package com.app.clipsteronline.upload.editor.effects

import com.app.clipsteronline.upload.editor.core.model.TransitionModel
import com.app.clipsteronline.upload.editor.render.ShaderRenderer

class TransitionEngine(
    private val shaderRenderer: ShaderRenderer = ShaderRenderer(),
) {
    private val shaderCache = mutableMapOf<TransitionModel.Type, TransitionShader>()

    fun configure() = Unit

    fun createSchedule(
        transition: TransitionModel,
        fromClipStartMs: Long,
        fromClipEndMs: Long,
        toClipStartMs: Long,
        toClipEndMs: Long,
    ): TransitionSchedule {
        val overlapStart = maxOf(fromClipStartMs, toClipStartMs)
        val overlapEnd = minOf(fromClipEndMs, toClipEndMs)
        val requestedDuration = transition.durationMs
        val usableDuration = if (overlapEnd > overlapStart) {
            minOf(requestedDuration, overlapEnd - overlapStart)
        } else {
            requestedDuration
        }

        val startMs = if (overlapEnd > overlapStart) overlapEnd - usableDuration else fromClipEndMs - usableDuration
        val endMs = startMs + usableDuration
        return TransitionSchedule(startMs.coerceAtLeast(0L), endMs.coerceAtLeast(startMs), transition)
    }

    fun progressFor(timeMs: Long, schedule: TransitionSchedule): Float {
        val elapsed = (timeMs - schedule.startMs).coerceAtLeast(0L)
        return schedule.transition.progressAt(elapsed)
    }

    fun resolveShader(transition: TransitionModel): TransitionShader {
        if (!transition.gpuCompatible) return TransitionShader.fallback(transition.type)
        return shaderCache.getOrPut(transition.type) {
            val shader = TransitionShader.forType(transition.type)
            val valid = shaderRenderer.compileShader(android.opengl.GLES20.GL_FRAGMENT_SHADER, shader.fragmentShader) != 0
            if (valid) shader else TransitionShader.fallback(transition.type)
        }
    }

    data class TransitionSchedule(
        val startMs: Long,
        val endMs: Long,
        val transition: TransitionModel,
    ) {
        val durationMs: Long get() = (endMs - startMs).coerceAtLeast(1L)
        fun contains(timeMs: Long): Boolean = timeMs in startMs..endMs
    }

    data class TransitionShader(
        val type: TransitionModel.Type,
        val fragmentShader: String,
    ) {
        companion object {
            fun fallback(type: TransitionModel.Type): TransitionShader = TransitionShader(type, FRAGMENT_FADE)

            fun forType(type: TransitionModel.Type): TransitionShader = when (type) {
                TransitionModel.Type.CUT -> TransitionShader(type, FRAGMENT_CUT)
                TransitionModel.Type.FADE,
                TransitionModel.Type.DISSOLVE -> TransitionShader(type, FRAGMENT_FADE)
                TransitionModel.Type.SLIDE -> TransitionShader(type, FRAGMENT_SLIDE)
                TransitionModel.Type.ZOOM -> TransitionShader(type, FRAGMENT_ZOOM)
                TransitionModel.Type.BLUR -> TransitionShader(type, FRAGMENT_BLUR)
                TransitionModel.Type.GLITCH -> TransitionShader(type, FRAGMENT_GLITCH)
                TransitionModel.Type.CINEMATIC_WIPE,
                TransitionModel.Type.CINEMATIC_FLASH -> TransitionShader(type, FRAGMENT_CINEMATIC)
            }
        }
    }

    companion object {
        private const val FRAGMENT_HEADER = "precision mediump float; varying vec2 vTexCoord; uniform sampler2D uFromTex; uniform sampler2D uToTex; uniform float uProgress;"
        private const val FRAGMENT_CUT = "$FRAGMENT_HEADER void main(){ gl_FragColor = texture2D(uToTex, vTexCoord);}"
        private const val FRAGMENT_FADE = "$FRAGMENT_HEADER void main(){ vec4 a=texture2D(uFromTex,vTexCoord); vec4 b=texture2D(uToTex,vTexCoord); gl_FragColor=mix(a,b,uProgress);}"
        private const val FRAGMENT_SLIDE = "$FRAGMENT_HEADER uniform vec2 uDirection; void main(){ vec2 p=vTexCoord; vec2 fromUv=p+uDirection*uProgress; vec2 toUv=p-uDirection*(1.0-uProgress); vec4 a=texture2D(uFromTex,clamp(fromUv,0.0,1.0)); vec4 b=texture2D(uToTex,clamp(toUv,0.0,1.0)); gl_FragColor=mix(a,b,smoothstep(0.0,1.0,uProgress)); }"
        private const val FRAGMENT_ZOOM = "$FRAGMENT_HEADER void main(){ vec2 c=vec2(0.5); vec2 fromUv=(vTexCoord-c)*(1.0+uProgress*0.2)+c; vec2 toUv=(vTexCoord-c)*(1.2-uProgress*0.2)+c; vec4 a=texture2D(uFromTex,clamp(fromUv,0.0,1.0)); vec4 b=texture2D(uToTex,clamp(toUv,0.0,1.0)); gl_FragColor=mix(a,b,uProgress);}"
        private const val FRAGMENT_BLUR = "$FRAGMENT_HEADER void main(){ vec2 off=vec2(0.002*uProgress); vec4 a=(texture2D(uFromTex,vTexCoord-off)+texture2D(uFromTex,vTexCoord)+texture2D(uFromTex,vTexCoord+off))/3.0; vec4 b=(texture2D(uToTex,vTexCoord-off)+texture2D(uToTex,vTexCoord)+texture2D(uToTex,vTexCoord+off))/3.0; gl_FragColor=mix(a,b,uProgress);}"
        private const val FRAGMENT_GLITCH = "$FRAGMENT_HEADER void main(){ float n=fract(sin(dot(vTexCoord,vec2(12.9898,78.233)))*43758.5453); vec2 j=vec2((n-0.5)*0.02*uProgress,0.0); vec4 a=texture2D(uFromTex,vTexCoord+j); vec4 b=texture2D(uToTex,vTexCoord-j); gl_FragColor=mix(a,b,uProgress);}"
        private const val FRAGMENT_CINEMATIC = "$FRAGMENT_HEADER void main(){ vec2 uv=vTexCoord; float vignette=smoothstep(0.9,0.2,distance(uv,vec2(0.5))); vec4 a=texture2D(uFromTex,uv); vec4 b=texture2D(uToTex,uv); vec4 base=mix(a,b,uProgress); gl_FragColor=vec4(base.rgb*vignette,base.a);}"
    }
}
