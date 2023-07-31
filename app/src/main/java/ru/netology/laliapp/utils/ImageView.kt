package ru.netology.laliapp.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation

fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)