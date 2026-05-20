package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

object DensityUtils {
    fun dpToPx(context: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    fun pxToDp(context: Context, px: Float): Float = px / context.resources.displayMetrics.density

    fun spToPx(context: Context, sp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()

    fun pxToSp(context: Context, px: Float): Float = px / context.resources.displayMetrics.scaledDensity

    fun metrics(context: Context): DisplayMetrics = context.resources.displayMetrics
    fun screenWidthPx(context: Context): Int = metrics(context).widthPixels
    fun screenHeightPx(context: Context): Int = metrics(context).heightPixels
}
