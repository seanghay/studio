package com.seanghay.studio.gles.transition

import android.content.Context

class AssetTransition(
    private val context: Context,
    private val filename: String,
    name: String
) : Transition(name, "", 1000) {

    init {
        source = readTextFromAsset()
    }

    private fun readTextFromAsset(): String {
        return context.assets.open(filename).readBytes().toString(Charsets.UTF_8)
    }
}