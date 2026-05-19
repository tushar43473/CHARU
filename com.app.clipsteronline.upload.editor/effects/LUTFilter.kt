package com.app.clipsteronline.upload.editor.effects

import android.graphics.Bitmap
import android.opengl.GLES20
import android.net.Uri

/**
 * LUT filter for color grading.
 * Loads LUT textures and applies color grading.
 */
class LUTFilter(
    private val lutUri: Uri? = null,
    private val intensity: Float = 1f
) : Effect("lut", "LUT", EffectType.COLOR) {

    private var textureId: Int = 0
    private var lutBitmap: Bitmap? = null
    private var lutSize: Int = 64
    private var currentPreset: LUTPreset = LUTPreset.NONE

    /**
     * Load LUT texture.
     */
    fun loadLUT(bitmap: Bitmap) {
        lutBitmap = bitmap
        lutSize = minOf(bitmap.width, bitmap.height)

        // Upload texture
        if (textureId == 0) {
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0,
            GLES20.GL_RGBA, lutSize, lutSize,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            null
        )
    }

    /**
     * Create fragment shader.
     */
    fun createFragmentShader(): String {
        return """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform sampler2D sLUT;
            uniform float uIntensity;
            uniform int uLUTSize;
            
            vec4 sampleLUT(vec3 color, sampler2D lut) {
                float blueColor = color.b * float(uLUTSize - 1);
                vec2 uv = vec2(
                    color.r / float(uLUTSize) + 0.5 / float(uLUTSize),
                    color.g / float(uLUTSize) + 0.5 / float(uLUTSize)
                );
                
                float blueLayer = floor(blueColor);
                float layerMix = blueColor - blueLayer;
                
                vec4 lut1 = texture2D(lut, vec2(uv.x + blueLayer / float(uLUTSize), uv.y));
                vec4 lut2 = texture2D(lut, vec2(uv.x + (blueLayer + 1.0) / float(uLUTSize), uv.y));
                
                return mix(lut1, lut2, layerMix);
            }
            
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                vec4 lutColor = sampleLUT(color.rgb, sLUT);
                gl_FragColor = mix(color, lutColor, uIntensity);
            }
        """
    }

    /**
     * Create vertex shader.
     */
    fun createVertexShader(): String {
        return """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """
    }

    /**
     * Apply preset.
     */
    fun applyPreset(preset: LUTPreset) {
        currentPreset = preset

        when (preset) {
            LUTPreset.NONE -> intensity = 0f
            LUTPreset.WARM -> {} // Warm tones
            LUTPreset.COOL -> {} // Cool tones
            LUTPreset.FADE -> {} // Faded look
            LUTPreset.CRISP -> {} // Crisp high contrast
            LUTPreset.VINTAGE -> {} // Vintage look
            LUTPreset.NOIR -> {} // Black and white
            LUTPreset.FOREST -> {} // Green tones
            LUTPreset.SUNSET -> {} // Orange/purple
        }
    }

    /**
     * Set intensity.
     */
    fun setIntensity(value: Float) {
        intensity.coerceIn(0f, 1f)
    }

    /**
     * Get intensity.
     */
    fun getIntensity(): Float = intensity

    /**
     * Get shader uniforms.
     */
    fun getShaderUniforms(): Map<String, Float> {
        return mapOf(
            "uIntensity" to intensity,
            "uLUTSize" to lutSize.toFloat()
        )
    }

    /**
     * Release resources.
     */
    fun release() {
        if (textureId > 0) {
            val textures = intArrayOf(textureId)
            GLES20.glDeleteTextures(1, textures, 0)
            textureId = 0
        }
        lutBitmap?.recycle()
        lutBitmap = null
    }
}

/**
 * LUT presets.
 */
enum class LUTPreset {
    NONE,
    WARM,
    COOL,
    FADE,
    CRISP,
    VINTAGE,
    NOIR,
    FOREST,
    SUNSET
}

/**
 * Preset shaders (when no texture available).
 */
object LUTPresets {
    fun getShader(preset: LUTPreset): String {
        return when (preset) {
            LUTPreset.WARM -> """ color.rgb += vec3(0.1, 0.05, -0.05); """
            LUTPreset.COOL -> """ color.rgb -= vec3(0.05, 0.0, 0.1); """
            LUTPreset.FADE -> """ color.rgb = color.rgb * 0.9 + 0.1; """
            LUTPreset.CRISP -> """ color.rgb = color.rgb * 1.2 - 0.1; color.rgb = clamp(color.rgb, 0.0, 1.0); """
            LUTPreset.VINTAGE -> """ color.rgb = color.rgb * vec3(1.1, 1.0, 0.9); """
            LUTPreset.NOIR -> """ float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114)); color.rgb = vec3(gray); """
            LUTPreset.FOREST -> """ color.rgb = color.rgb + vec3(-0.1, 0.1, -0.1); """
            LUTPreset.SUNSET -> """ color.rgb = color.rgb + vec3(0.15, 0.05, -0.1); """
            LUTPreset.NONE -> "" // Identity - no change
        }
    }
}