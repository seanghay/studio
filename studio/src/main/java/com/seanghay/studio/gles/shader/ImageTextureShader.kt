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
package com.seanghay.studio.gles.shader

import android.graphics.Bitmap
import com.seanghay.studio.gles.graphics.texture.BitmapTextureUniform
import com.seanghay.studio.gles.graphics.texture.Texture2dUniform

open class ImageTextureShader(var bitmap: Bitmap) : TextureShader() {

    var texture2d: Texture2dUniform = BitmapTextureUniform("texture", bitmap).autoInit()

    open fun draw() {
        draw(texture2d.texture)
    }
}
