package com.seanghay.studioexample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.view.TextureView
import com.seanghay.studio.core.StudioDrawable
import com.seanghay.studio.core.StudioRenderThread
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.transition.*
import com.seanghay.studio.utils.BitmapProcessor
import java.util.*
import kotlin.math.max
import kotlin.math.min


class Scene(var bitmap: Bitmap) {

    var duration: Long = 4000L
    var transition: Transition = FadeTransition("fade", 1000L)
    var texture: Texture2d = Texture2d()

    @GlContext
    fun setup() {
        texture.initialize()
        texture.use(GL_TEXTURE_2D) {
            texture.configure(GL_TEXTURE_2D)
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        }
    }

}


fun Float.minMax(a: Float, b: Float): Float {
    return (min(b, max(a, this)))
}

class VideoComposer(private val context: Context) : StudioDrawable {

    private var studioRenderThread: StudioRenderThread? = null
    private var width: Int = -1
    private var height: Int = -1

    private val transitions = arrayListOf(
        FadeTransition("fade", 1000L),
        AngularTransition(),
        BounceTransition(),
        BowTieHorizontalTransition(),
        BowTieVerticalTransition(),
        BurnTransition(),
        ButterflyWaveScrawlerTransition(),
        CannabisleafTransition(),
        CircleCropTransition(),
        CircleTransition(),
        CircleopenTransition(),
        ColorphaseTransition(),
        ColourDistanceTransition(),
        CrazyParametricFunTransition(),
        CrossZoomTransition(),
        CrosshatchTransition(),
        CrosswarpTransition(),
        CubeTransition(),
        DirectionalTransition(),
        DirectionalwarpTransition(),
        DirectionalwipeTransition(),
        DoomScreenTransitionTransition(),
        DoorwayTransition(),
        DreamyTransition(),
        DreamyZoomTransition(),
        FadecolorTransition(),
        FadegrayscaleTransition(),
        FlyeyeTransition(),
        GlitchDisplaceTransition(),
        GlitchMemoriesTransition(),
        HeartTransition(),
        HexagonalizeTransition(),
        InvertedPageCurlTransition(),
        KaleidoscopeTransition(),
        LinearBlurTransition(),
        LuminanceMeltTransition(),
        MorphTransition(),
        MosaicTransition(),
        MultiplyBlendTransition(),
        PerlinTransition(),
        PinwheelTransition(),
        PolarFunctionTransition(),
        PolkaDotsCurtainTransition(),
        RadialTransition(),
        RippleTransition(),
        RotateScaleFadeTransition(),
        SimpleZoomTransition(),
        SqueezeTransition(),
        StereoViewerTransition(),
        SwapTransition(),
        SwirlTransition(),
        UndulatingBurnOutTransition(),
        WaterDropTransition(),
        WindTransition(),
        WindowblindsTransition(),
        WindowsliceTransition(),
        WipeDownTransition(),
        WipeLeftTransition(),
        WipeRightTransition(),
        WipeUpTransition(),
        ZoomInCirclesTransition()
    )

    private val defaultTransition: Transition = FadeTransition("fade", 1000L)
    private val scenes = mutableListOf<Scene>()
    private val preDrawRunnables: Queue<Runnable> = LinkedList()
    private val textureShaders = transitions.associate { it.name to TransitionalTextureShader(it) }
    private val blankTexture = Texture2d()
    private var durations = longArrayOf()

    var progress: Float = 0f
    var totalDuration = 0L

    fun getTransitions() = transitions

    fun getScenes(): List<Scene> = scenes

    fun insertScenes(vararg bitmaps: Bitmap) {
        for (bitmap in bitmaps) {
            val bitmapProcessor = BitmapProcessor(bitmap)
            bitmapProcessor.crop(1920, 1080)
            val scene = Scene(bitmapProcessor.proceed())
            scenes.add(scene)
            postPreDraw { scene.setup() }
            totalDuration += scene.duration
        }

        var last = 0L
        durations = scenes.map {
            last += it.duration
            last
        }.toLongArray()
    }

    override fun onSetup() {
        textureShaders.forEach {
            it.value.isFlipVertical = true
            it.value.setup()
        }

        blankTexture.initialize()
        blankTexture.configure(GL_TEXTURE_2D)
    }


    private inline fun postPreDraw(crossinline run: () -> Unit) {
        preDrawRunnables.add(Runnable { run() })
    }


    val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            this@VideoComposer.width = width
            this@VideoComposer.height = height

            studioRenderThread?.let {
                it.height = height
                it.width = width
            }
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            if (surfaceTexture == null) return

            this@VideoComposer.width = width
            this@VideoComposer.height = height

            studioRenderThread = StudioRenderThread(surfaceTexture).also {
                it.height = height
                it.width = width
                it.drawable = this@VideoComposer
                it.start()
            }
        }
    }


    private fun runOnPreDraw() {
        while (preDrawRunnables.isNotEmpty()) preDrawRunnables.poll()?.run()
    }


    private fun calculateIndexFromDuration(seekAt: Float): Int? {
        for ((index, value) in durations.withIndex()) {
            if (seekAt <= value) return index
        }
        return null
    }

    private fun calculateSeekOffset(index: Int, seekAt: Float): Float {
        val last = durations.getOrElse(index - 1) { 0L }
        return (seekAt - last) / scenes[index].duration.toFloat()
    }

    override fun onDraw(): Boolean {
        runOnPreDraw()
        if (width != -1 && height != -1) {
            glViewport(0, 0, width, height)
        }

        glScope {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0f, 0f, 0f, 1f)

        val seekAt = totalDuration * progress
        val seekIndex = calculateIndexFromDuration(seekAt) ?: return false
        val offset = calculateSeekOffset(seekIndex, seekAt)
        val currentScene = scenes[seekIndex]

        val currentTexture = currentScene.texture
        val nextTexture = scenes.getOrNull(seekIndex + 1)?.texture ?: blankTexture

        val textureShader = textureShaders[currentScene.transition.name] ?: return false
        textureShader.progress = interpolateOffset(currentScene, offset)
        textureShader.draw(currentTexture, nextTexture)

        try {
            Thread.sleep(10)
        } catch (e: Exception) {
        }

        return true
    }

    private fun interpolateOffset(scene: Scene, offset: Float): Float {
        val slideDuration = scene.duration
        val transitionDuration = scene.transition.duration
        val diff = transitionDuration.toFloat() / slideDuration.toFloat()
        return ((offset - (1f - diff)) / diff).minMax(0f, 1f)
    }

    fun release() {
        studioRenderThread?.quit()
    }
}