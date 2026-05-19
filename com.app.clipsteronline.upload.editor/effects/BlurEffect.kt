package com.app.clipsteronline.upload.editor.effects

/**
 * Blur effect.
 * Gaussian, directional, and zoom blur variants.
 */
class BlurEffect(
    private val radius: Float = 0.5f,
    private val type: BlurType = BlurType.GAUSSIAN
) : Effect("blur", "Blur", EffectType.BLUR) {

    /**
     * Create fragment shader.
     */
    fun createFragmentShader(): String {
        return when (type) {
            BlurType.GAUSSIAN -> GAUSSIAN_SHADER
            BlurType.DIRECTIONAL -> DIRECTIONAL_SHADER
            BlurType.ZOOM -> ZOOM_BLUR_SHADER
        }
    }

    /**
     * Get shader uniforms based on blur type.
     */
    fun getShaderUniforms(): Map<String, Float> {
        return when (type) {
            BlurType.GAUSSIAN -> mapOf(
                "uRadius" to radius
            )
            BlurType.DIRECTIONAL -> mapOf(
                "uRadius" to radius,
                "uDirectionX" to 1f,
                "uDirectionY" to 0f
            )
            BlurType.ZOOM -> mapOf(
                "uRadius" to radius,
                "uCenterX" to 0.5f,
                "uCenterY" to 0.5f
            )
        }
    }

    /**
     * Set blur radius.
     */
    fun setRadius(value: Float) {
        radius.coerceIn(0f, 1f)
    }

    /**
     * Set blur type.
     */
    fun setType(type: BlurType) {
        this.type = type
    }

    companion object {
        private const val GAUSSIAN_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uRadius;
            
            void main() {
                vec2 texel = vec2(1.0) / vec2(1920.0, 1080.0);
                float total = 0.0;
                vec4 color = vec4(0.0);
                
                int samples = int(uRadius * 10.0) + 1;
                
                for(int y = -samples; y <= samples; y++) {
                    for(int x = -samples; x <= samples; x++) {
                        float weight = exp(-float(x*x + y*y) / float(samples * samples));
                        color += texture2D(sTexture, vTexCoord + vec2(float(x), float(y)) * texel * uRadius) * weight;
                        total += weight;
                    }
                }
                
                gl_FragColor = color / total;
            }
        """

        private const val DIRECTIONAL_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uRadius;
            uniform float uDirectionX;
            uniform float uDirectionY;
            
            void main() {
                vec2 texel = vec2(1.0) / vec2(1920.0, 1080.0);
                vec2 dir = vec2(uDirectionX, uDirectionY) * uRadius * 10.0;
                vec4 color = vec4(0.0);
                
                for(int i = -10; i <= 10; i++) {
                    float weight = 1.0 - abs(float(i)) / 10.0;
                    color += texture2D(sTexture, vTexCoord + dir * float(i) * texel) * weight;
                }
                
                gl_FragColor = color / 10.0;
            }
        """

        private const val ZOOM_BLUR_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uRadius;
            uniform float uCenterX;
            uniform float uCenterY;
            
            void main() {
                vec2 center = vec2(uCenterX, uCenterY);
                vec2 dir = vTexCoord - center;
                vec4 color = vec4(0.0);
                float total = 0.0;
                
                for(int i = 0; i < 10; i++) {
                    float scale = 1.0 - float(i) * uRadius / 10.0;
                    float weight = 1.0 - float(i) / 10.0;
                    vec2 offset = center + dir * scale;
                    color += texture2D(sTexture, offset) * weight;
                    total += weight;
                }
                
                gl_FragColor = color / total;
            }
        """
    }
}

/**
 * Blur types.
 */
enum class BlurType {
    GAUSSIAN,
    DIRECTIONAL,
    ZOOM
}