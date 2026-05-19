package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Shader renderer for effects.
 * Compiles and manages filter shaders.
 */
class ShaderRenderer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val shaderCache = mutableMapOf<String, Int>()
    private var activeProgram = 0

    /**
     * Compile shader.
     */
    fun compileShader(name: String, vertexSource: String, fragmentSource: String): Int {
        val cached = shaderCache[name]
        if (cached != null) return cached

        val program = createProgram(vertexSource, fragmentSource)
        if (program > 0) {
            shaderCache[name] = program
        }

        return program
    }

    /**
     * Use shader.
     */
    fun useShader(name: String): Boolean {
        val program = shaderCache[name] ?: return false
        activeProgram = program
        GLES20.glUseProgram(program)
        return true
    }

    /**
     * Apply brightness/contrast.
     */
    fun applyBrightnessContrast(brightness: Float = 0f, contrast: Float = 1f): Int {
        val fragment = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uBrightness;
            uniform float uContrast;
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                color.rgb = (color.rgb - 0.5) * uContrast + 0.5 + uBrightness;
                gl_FragColor = color;
            }
        """

        return compileShader("brightness_contrast", VERTEX_SHADER, fragment)
    }

    /**
     * Apply saturation.
     */
    fun applySaturation(saturation: Float = 1f): Int {
        val fragment = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uSaturation;
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                color.rgb = mix(vec3(gray), color.rgb, uSaturation);
                gl_FragColor = color;
            }
        """

        return compileShader("saturation", VERTEX_SHADER, fragment)
    }

    /**
     * Apply sepia.
     */
    fun applySepia(): Int {
        val fragment = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                float r = color.r * 0.393 + color.g * 0.769 + color.b * 0.189;
                float g = color.r * 0.349 + color.g * 0.686 + color.b * 0.168;
                float b = color.r * 0.272 + color.g * 0.534 + color.b * 0.131;
                gl_FragColor = vec4(r, g, b, color.a);
            }
        """

        return compileShader("sepia", VERTEX_SHADER, fragment)
    }

    /**
     * Apply blur.
     */
    fun applyBlur(radius: Int = 1): Int {
        val fragment = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform vec2 uResolution;
            uniform int uRadius;
            void main() {
                vec2 texel = 1.0 / uResolution;
                vec4 color = vec4(0.0);
                float total = 0.0;
                for(int x = -2; x <= 2; x++) {
                    for(int y = -2; y <= 2; y++) {
                        float weight = 1.0 / (float(uRadius) * float(uRadius));
                        color += texture2D(sTexture, vTexCoord + vec2(float(x), float(y)) * texel * float(uRadius)) * weight;
                        total += weight;
                    }
                }
                gl_FragColor = color / total;
            }
        """

        return compileShader("blur", VERTEX_SHADER, fragment)
    }

    /**
     * Get shader program.
     */
    fun getShader(name: String): Int? = shaderCache[name]

    /**
     * Clear shader cache.
     */
    fun clearCache() {
        shaderCache.values.forEach { GLES20.glDeleteProgram(it) }
        shaderCache.clear()
    }

    /**
     * Create shader program.
     */
    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        return program
    }

    /**
     * Load shader.
     */
    private fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """
    }
}