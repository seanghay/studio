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
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglWindowSurface

class StudioRenderThread(private val surfaceTexture: SurfaceTexture) : Thread() {

    private val eglCore = EglCore()
    private lateinit var windowSurface: EglWindowSurface
    private var isRunning = true
    var drawable: StudioDrawable? = null

    var height: Int = -1
    var width: Int = -1

    private fun setup() {
        eglCore.setup()
        windowSurface = EglWindowSurface(eglCore, surfaceTexture)
    }

    fun recreate() {
        windowSurface.recreate(eglCore)
    }

    private fun swapBuffers() {
        windowSurface.swapBuffers()
    }

    private fun makeCurrent() {
        windowSurface.makeCurrent()
    }

    private fun release() {
        windowSurface.release()
        eglCore.release()
    }

    private fun drawFrame(): Boolean {
        return drawable?.onDraw() ?: false
    }

    override fun run() {
        setup()
        makeCurrent()
        drawable?.onSetup()

        while (isRunning && !interrupted()) {
            makeCurrent()
            if (drawFrame())
                swapBuffers()
        }

        release()
    }

    fun getEglCore(): EglCore = eglCore

    fun quit() {
        isRunning = false
    }
}
