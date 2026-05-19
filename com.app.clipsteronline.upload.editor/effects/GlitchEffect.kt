package com.app.clipsteronline.upload.editor.effects

/**
 * Glitch effect.
 * RGB shift, digital distortion, scanlines, noise animation, chromatic aberration.
 */
class GlitchEffect(
    private val intensity: Float = 0.5f
) : Effect("glitch", "Glitch", EffectType.GLITCH) {

    private var time: Float = 0f
    private var enableRGBShift: Boolean = true
    private var enableScanlines: Boolean = true
    private var enableNoise: Boolean = true

    /**
     * Update time.
     */
    fun update(time: Float) {
        this.time = time
    }

    /**
     * Create fragment shader.
     */
    fun createFragmentShader(): String {
        return """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            uniform float uIntensity;
            uniform float uTime;
            
            float random(vec2 co) {
                return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453;
            }
            
            void main() {
                vec2 uv = vTexCoord;
                vec4 color = texture2D(sTexture, uv);
                
                // RGB shift
                float shift = sin(uTime * 10.0 + uv.y * 20.0) * uIntensity * 0.02;
                float r = texture2D(sTexture, uv + vec2(shift, 0.0)).r;
                float b = texture2D(sTexture, uv - vec2(shift, 0.0)).b;
                color.r = mix(color.r, r, uIntensity);
                color.b = mix(color.b, b, uIntensity);
                
                // Digital distortion
                float distort = step(0.98, random(vec2(floor(uv.y * 20.0), floor(uTime))));
                if(distort > 0.5) {
                    uv.x += (random(vec2(uTime, uv.y)) - 0.5) * uIntensity * 0.1;
                    color = texture2D(sTexture, uv);
                }
                
                // Scanlines
                float scanline = sin(uv.y * 400.0 + uTime * 50.0) * 0.04 * uIntensity;
                color.rgb -= scanline;
                
                // Noise
                float noise = random(uv + uTime) * uIntensity * 0.1;
                color.rgb += vec3(noise) - uIntensity * 0.05;
                
                gl_FragColor = color;
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
            "uIntensity" to intensity,
            "uTime" to time
        )
    }

    /**
     * Enable/disable RGB shift.
     */
    fun setRGBShift(enabled: Boolean) {
        enableRGBShift = enabled
    }

    /**
     * Enable/disable scanlines.
     */
    fun setScanlines(enabled: Boolean) {
        enableScanlines = enabled
    }

    /**
     * Enable/disable noise.
     */
    fun setNoise(enabled: Boolean) {
        enableNoise = enabled
    }
}