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
package com.seanghay.studio.internal

import android.util.Log
import androidx.annotation.IntDef
import com.seanghay.studio.sonic.Sonic

@Suppress("NOTHING_TO_INLINE")
inline fun Any.logger(): Logger {
    return Logger(this.javaClass.simpleName)
}

class Logger(var tag: String) {

    @LogLevel
    var level: Int = LOG_VERBOSE

    fun w(message: String, throwable: Throwable? = null) = log(LOG_WARNING, tag, message, throwable)
    fun i(message: String, throwable: Throwable? = null) = log(LOG_INFO, tag, message, throwable)
    fun e(message: String, throwable: Throwable? = null) = log(LOG_ERROR, tag, message, throwable)
    fun d(message: String, throwable: Throwable? = null) = log(LOG_DEBUG, tag, message, throwable)

    @IntDef(LOG_VERBOSE, LOG_INFO, LOG_WARNING, LOG_ERROR, LOG_DEBUG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    companion object {
        const val LOG_VERBOSE = 0
        const val LOG_INFO = 1
        const val LOG_WARNING = 2
        const val LOG_ERROR = 3
        const val LOG_DEBUG = 4

        private fun log(
          @LogLevel level: Int,
          tag: String,
          message: String,
          throwable: Throwable? = null
        ) {
            when (level) {
                LOG_VERBOSE -> Log.v(tag, message, throwable)
                LOG_INFO -> Log.i(tag, message, throwable)
                LOG_WARNING -> Log.w(tag, message, throwable)
                LOG_ERROR -> Log.e(tag, message, throwable)
                LOG_DEBUG -> Log.d(tag, message, throwable)
            }
        }
    }
}
