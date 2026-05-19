package com.app.clipsteronline.upload.editor.effects

/**
 * VHS retro effect.
 * VHS lines, analog noise, retro color shift, tape distortion, grain.
 */
class VHSRetroEffect(
    private val intensity: Float = 0.5f
) : Effect("vhs_retro", "VHS Retro", EffectType.RETRO) {

    private var time: Float = 0f
    private var enableVHSEffect: Boolean = true
    private var enableNoise: Boolean = true
    private var enableColorShift: Boolean = true

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
                
                // VHS horizontal lines
                float scanline = sin(uv.y * 800.0) * 0.03 * uIntensity;
                color.rgb -= scanline;
                
                // Occasional horizontal glitch lines
                float glitch = step(0.97, random(vec2(floor(uTime * 2.0), floor(uv.y * 10.0)));
                if(glitch > 0.5) {
                    float offset = (random(vec2(uTime, uv.y)) - 0.5) * uIntensity * 0.05;
                    color = texture2D(sTexture, uv + vec2(offset, 0.0));
                }
                
                // Color channel delay
                float delay = sin(uTime * 3.0 + uv.y * 5.0) * uIntensity * 2.0;
                float r = texture2D(sTexture, uv + vec2(delay * 0.002, 0.0)).r;
                float b = texture2D(sTexture, uv - vec2(delay * 0.001, 0.0)).b;
                color.r = mix(color.r, r, 0.5);
                color.b = mix(color.b, b, 0.5);
                
                // Reduce color resolution (posterize)
                color.rgb = floor(color.rgb * 32.0) / 32.0;
                
                // Add noise/grain
                float noise = (random(uv + uTime) - 0.5) * uIntensity * 0.15;
                color.rgb += noise;
                
                // Vignette
                float dist = distance(uv, vec2(0.5));
                color.rgb *= 1.0 - dist * uIntensity * 0.5;
                
                // Tape wobble
                float wobble = sin(uTime * 30.0 + uv.y * 50.0) * uIntensity * 0.003;
                
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
     * Set VHS effect.
     */
    fun setVHSEffect(enabled: Boolean) {
        enableVHSEffect = enabled
    }

    /**
     * Set color shift.
     */
    fun setColorShift(enabled: Boolean) {
        enableColorShift = enabled
    }
}