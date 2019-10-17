/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.core

import android.graphics.SurfaceTexture
import android.view.TextureView
import java.io.File

class StudioEngine : TextureView.SurfaceTextureListener {

    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0
    private lateinit var renderThread: StudioRenderThread

    var files: List<File> = listOf()

    private fun setViewport(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
    }

    private fun onSurfaceCreated(surface: SurfaceTexture, width: Int, height: Int) {
        setViewport(width, height)
        renderThread = StudioRenderThread(surface)
        renderThread.start()
    }

    private fun onSurfaceSizeChanged(width: Int, height: Int) {
        setViewport(width, height)
    }

    private fun releaseSurface() {
        renderThread.quit()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        onSurfaceSizeChanged(width, height)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        releaseSurface()

        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (surface == null) return
        onSurfaceCreated(surface, width, height)
    }

    fun begin() {
        println(this.files)
    }
}
