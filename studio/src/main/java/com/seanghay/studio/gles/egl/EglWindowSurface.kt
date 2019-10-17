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
package com.seanghay.studio.gles.egl

import android.graphics.SurfaceTexture
import android.view.Surface

class EglWindowSurface : EglSurfaceBase {

    private var surface: Surface? = null
    private var releaseSurface = false

    constructor(
      eglCore: EglCore,
      surface: Surface?,
      releaseSurface: Boolean
    ) : super(eglCore) {
        createWindowSurface(surface)
        this.surface = surface
        this.releaseSurface = releaseSurface
    }

    constructor(eglCore: EglCore, surfaceTexture: SurfaceTexture) :
            super(eglCore) {
        createWindowSurface(surfaceTexture)
    }

    fun release() {
        releaseEglSurface()
        if (surface != null) {
            if (releaseSurface) surface?.release()
            surface = null
        }
    }

    fun recreate(newEglCore: EglCore) {
        if (surface == null)
            throw RuntimeException("Not yet implemented for surface texture")

        eglCore = newEglCore
        createWindowSurface(surface)
    }
}
