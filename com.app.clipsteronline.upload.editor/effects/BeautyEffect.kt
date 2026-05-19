package com.app.clipsteronline.upload.editor.effects

/**
 * Beauty effect shader.
 * Skin smoothing, face brightening, detail sharpening.
 */
class BeautyEffect(
    private val intensity: Float = 0.5f
) : Effect("beauty", "Beauty", EffectType.BEAUTY) {

    /**
     * Create fragment shader.
     */
    fun createFragmentShader(): String {
        return """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uIntensity;
            
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                
                // Apply bilateral filter for smoothing
                float kernel[5];
                kernel[0] = 0.0625;
                kernel[1] = 0.25;
                kernel[2] = 0.375;
                kernel[3] = 0.25;
                kernel[4] = 0.0625;
                
                vec4 smoothed = vec4(0.0);
                float total = 0.0;
                
                for(int y = -2; y <= 2; y++) {
                    for(int x = -2; x <= 2; x++) {
                        vec2 offset = vec2(float(x), float(y)) * 0.003;
                        float k = kernel[x + 2] * kernel[y + 2];
                        smoothed += texture2D(sTexture, vTexCoord + offset) * k;
                        total += k;
                    }
                }
                smoothed /= total;
                
                // Blend original with smoothed based on intensity
                vec4 result = mix(color, smoothed, uIntensity * 0.7);
                
                // Add slight brightening for face
                result.rgb += result.rgb * 0.1 * uIntensity;
                
                gl_FragColor = result;
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
            "uIntensity" to intensity
        )
    }
}