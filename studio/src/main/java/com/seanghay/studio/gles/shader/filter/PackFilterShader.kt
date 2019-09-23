package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniformMat4
import com.seanghay.studio.gles.shader.TextureShader
import com.seanghay.studio.gles.shader.filter.pack.PackFilter

class PackFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var intensity = 1f
    var brightness = 0f
    var contrast = 1f
    var saturation = .5f
    var warmth = 0f
    var tint = 0f
    var gamma = 0f
    var vibrant = 0f
    var sepia = 0f

    fun applyPackFilter(f: PackFilter) {
        intensity = f.intensity
        brightness = f.brightness
        contrast = f.contrast
        saturation = f.saturation
        warmth = f.warmth
        tint = f.tint
        gamma = f.gamma
        vibrant = f.vibrant
        sepia = f.sepia
    }

    var colorMatrixIntensity = 1f

    var colorMatrix = mat4()
    var sepiaMatrix = mat4()
    
    var intensityUniform = uniform1f("intensity").autoInit()
    var brightnessUniform = uniform1f("brightness").autoInit()
    var contrastUniform = uniform1f("contrast").autoInit()
    var saturationUniform = uniform1f("saturation").autoInit()
    var warmthUniform = uniform1f("warmth").autoInit()
    var tintUniform = uniform1f("tint").autoInit()
    var gammaUniform = uniform1f("gamma").autoInit()
    var vibrantUniform = uniform1f("vibrant").autoInit()
    var sepiaUniform = uniform1f("sepia").autoInit()
    var sepiaMatrixUniform = uniformMat4("sepiaMatrix").autoInit()
    var colorMatrixUniform = uniformMat4("colorMatrix").autoInit()
    var colorMatrixIntensityUniform = uniform1f("colorMatrixIntensity").autoInit()


    init {
        sepiaMatrix.elements = floatArrayOf(
            0.3588f, 0.7044f, 0.1368f, 0.0f,
            0.2990f, 0.5870f, 0.1140f, 0.0f,
            0.2392f, 0.4696f, 0.0912f, 0.0f,
            0f, 0f, 0f, 1.0f
        )
    }

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        intensityUniform.setValue(intensity)
        brightnessUniform.setValue(brightness)
        contrastUniform.setValue(contrast)
        saturationUniform.setValue(saturation)
        warmthUniform.setValue(warmth)
        tintUniform.setValue(tint)
        gammaUniform.setValue(gamma)
        vibrantUniform.setValue(vibrant)
        sepiaUniform.setValue(sepia)
        sepiaMatrixUniform.setValue(sepiaMatrix)
        colorMatrixUniform.setValue(colorMatrix)
        colorMatrixIntensityUniform.setValue(colorMatrixIntensity)
    }


    companion object {
        // language=glsl

        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

            varying vec2 texCoord;
            uniform sampler2D texture;
            
            uniform float intensity;
            
            uniform float brightness;
            uniform float contrast;
            uniform float saturation;
            uniform float warmth;
            uniform float tint;
            uniform mat4 colorMatrix;
            uniform float colorMatrixIntensity;
            
            uniform float gamma;
            uniform float vibrant;
            
            uniform float sepia;
            uniform mat4 sepiaMatrix;

            vec4 applyBrightness(vec4 color, float value) {
                color.rgb += clamp(value, 0.0, 1.0);
                return color;
            }
            
            vec4 applyContrast(vec4 color, float value) {
                float a = clamp(value, 0.5, 1.5);
                return vec4(((color.rgb - vec3(0.5)) * a + vec3(0.5)), color.w);
            }

            vec4 applySaturation(vec4 color, float value) {
                float luminance = dot(color.rgb, luminanceWeighting);
                vec3 greyScaleColor = vec3(luminance);
                return vec4(mix(greyScaleColor, color.rgb, value), color.w);
            }
            
            vec4 applyWarmth(vec4 color, float value) { 
                color.r += value;
                color.b -= value;
                return color;
            }
            
            vec4 applyTint(vec4 color, float value) {
                color.g += clamp(value, -0.2, 0.2);
                return color;
            }
            
            vec4 applyGamma(vec4 color, float value) {
                return vec4(pow(color.rgb, vec3(value)), color.w);
            }
            
            vec4 applyVibrant(vec4 color, float value) {
                float mx = max(max(color.r, color.g), color.b);
                float average = (color.r + color.g + color.b) / 3.0;
                float amt = (mx - average) * (-value * 3.0);
                color.rgb = mix(color.rgb, vec3(mx), amt);
                return color;
            }
            
            vec4 applyColorMatrix(vec4 color, mat4 matrix, float opaque) {
                return (color * matrix) * opaque + color * (1.0 - opaque);
            }
            
            vec4 applySepia(vec4 color, float value) {
                return applyColorMatrix(color, sepiaMatrix, value);
            }


            void main() {
            
                vec4 color = texture2D(texture, texCoord);
                vec4 textureColor = texture2D(texture, texCoord);

                textureColor = applyBrightness(textureColor, brightness);
                textureColor = applySaturation(textureColor, saturation);
                textureColor = applyWarmth(textureColor, warmth);
                textureColor = applyTint(textureColor, tint);
                textureColor = applyGamma(textureColor, gamma);
                textureColor = applyVibrant(textureColor, vibrant);
                textureColor = applyColorMatrix(textureColor, colorMatrix, colorMatrixIntensity);
                textureColor = applySepia(textureColor, sepia);
                textureColor = applyContrast(textureColor, contrast);
                

                gl_FragColor = textureColor * intensity + color * (1.0 - intensity);
            }
            
            
        """
    }
}