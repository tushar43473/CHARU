package upload.editor.core.utils

import android.content.Context
import android.util.TypedValue
import android.view.WindowInsets

object DensityUtils {
    fun dpToPx(context: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    fun pxToDp(context: Context, px: Float): Float = px / context.resources.displayMetrics.density

    fun spToPx(context: Context, sp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()

    fun pxToSp(context: Context, px: Float): Float = px / context.resources.displayMetrics.scaledDensity

    fun screenWidth(context: Context): Int = context.resources.displayMetrics.widthPixels

    fun screenHeight(context: Context): Int = context.resources.displayMetrics.heightPixels

    fun safeInsetTop(insets: WindowInsets?): Int = insets?.systemWindowInsetTop ?: 0

    fun safeInsetBottom(insets: WindowInsets?): Int = insets?.systemWindowInsetBottom ?: 0

    fun density(context: Context): Float = context.resources.displayMetrics.density
}
