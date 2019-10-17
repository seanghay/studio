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
package com.seanghay.studio.gles.graphics

internal fun vec4(x: Float, y: Float, z: Float, a: Float) = Vector4f(x, y, z, a)
internal fun vec4(x: Float) = Vector4f(x, x, x, x)
internal fun vec4(vec3: Vector2f, z: Float, a: Float) = Vector4f(vec3.x, vec3.y, z, a)
internal fun vec4(vec3: Vector3f, a: Float) = Vector4f(vec3.x, vec3.y, vec3.z, a)
internal fun vec4() = Vector4f()

data class Vector4f(
  var x: Float = 0f,
  var y: Float = 0f,
  var z: Float = 0f,
  var a: Float = 0f
) {

    var values
        get() = floatArrayOf(x, y, z, a)
        set(value) {
            x = value[0]
            y = value[1]
            z = value[2]
            a = value[3]
        }

    // TODO: Add transformations Add, Sub, Multiply, Divide

    companion object {
        fun valueOf(vararg values: Float): Vector4f {
            return Vector4f(values[0], values[1], values[2], values[3])
        }

        fun fromArray(values: FloatArray): Vector4f {
            return Vector4f(values[0], values[1], values[2], values[3])
        }
    }
}
