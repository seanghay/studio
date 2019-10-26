package com.seanghay.studioexample.experiment.fragment

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.seanghay.studioexample.R
import com.seanghay.studioexample.experiment.core.EditorEngine

class StudioFragment : Fragment() {

    private lateinit var textureView: TextureView
    private val editorEngine = EditorEngine.getInstance()

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            editorEngine.setViewportSize(width, height)
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            // ignored
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            editorEngine.release()
            return true // will be release automatically
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            editorEngine.initialize()
            editorEngine.setViewportSize(width, height)
            editorEngine.attachSurfaceTexture(surface)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_studio, container, false).apply {
            textureView = findViewById(R.id.textureView)
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {

        @JvmStatic
        fun newInstance(): StudioFragment {
            return StudioFragment()
        }
    }
}