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

import android.opengl.EGL14
import android.opengl.GLES20

/**
 * Error checking for OpenGL
 * @param message this will show up when it throw an exception
 * @param block a callback for OpenGL calls
 */

inline fun <T> glScope(message: String = "", block: (() -> T)): T {
    return block().also {
        var error: Int = GLES20.glGetError()
        while (error != GLES20.GL_NO_ERROR) {
            val errorMessage = error
            error = GLES20.glGetError()
            throw RuntimeException("GL Error: $message\n 0x${errorMessage.toString(16)}")
        }
    }
}

/**
 * Error checking for EGL
 * @param message this will show up when it throw an exception
 * @param block callback for EGL calls
 */
inline fun <T> eglScope(message: String = "", block: (() -> T)): T {
    return block().also {
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            throw RuntimeException("$message: EGL Error: 0x${error.toString(16)}")
        }
    }
}
