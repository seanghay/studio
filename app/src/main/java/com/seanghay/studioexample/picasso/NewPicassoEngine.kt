package com.seanghay.studioexample.picasso

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhihu.matisse.engine.impl.PicassoEngine

class NewPicassoEngine : PicassoEngine() {
    override fun loadImage(
        context: Context?,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView?,
        uri: Uri?
    ) {
        Picasso.get().load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }


    override fun loadThumbnail(
        context: Context?,
        resize: Int,
        placeholder: Drawable?,
        imageView: ImageView?,
        uri: Uri?
    ) {
        Picasso.get().load(uri).apply {
            if (placeholder != null)
                placeholder(placeholder)
        }.resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }
}