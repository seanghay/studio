package com.seanghay.studio.gles.shader.filter.pack

data class PackFilter(
    var intensity: Float = 1f,
    var brightness: Float = 0f,
    var contrast: Float = 1f,
    var saturation: Float = 1f,
    var warmth: Float = 0f,
    var tint: Float = 0f,
    var gamma: Float = 1f,
    var vibrant: Float = 0f,
    var sepia: Float = 0f
)