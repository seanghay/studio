package com.seanghay.studio.composer

interface AudioComposer {
    fun setup()
    fun stepPipeline(): Boolean
    fun getWrittenPresentationTimeUs(): Long
    fun isFinished(): Boolean
    fun release()
}