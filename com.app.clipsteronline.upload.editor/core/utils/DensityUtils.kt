package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Density and dimension conversion utilities.
 * Provides dp/px/sp conversions and screen dimension helpers.
 */
object DensityUtils {

    /**
     * Convert dp to pixels.
     */
    fun dpToPx(dp: Float, context: Context): Float {
        return dp * getDensity(context)
    }

    /**
     * Convert dp to pixels as integer.
     */
    fun dpToPxInt(dp: Float, context: Context): Int {
        return dpToPx(dp, context).toInt()
    }

    /**
     * Convert pixels to dp.
     */
    fun pxToDp(px: Float, context: Context): Float {
        return px / getDensity(context)
    }

    /**
     * Convert pixels to dp as integer.
     */
    fun pxToDpInt(px: Float, context: Context): Int {
        return pxToDp(px, context).toInt()
    }

    /**
     * Convert sp to pixels.
     */
    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    /**
     * Convert sp to pixels as integer.
     */
    fun spToPxInt(sp: Float, context: Context): Int {
        return spToPx(sp, context).toInt()
    }

    /**
     * Convert pixels to sp.
     */
    fun pxToSp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }

    /**
     * Get screen width in pixels.
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * Get screen height in pixels.
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * Get screen width in dp.
     */
    fun getScreenWidthDp(context: Context): Int {
        return pxToDpInt(context.resources.displayMetrics.widthPixels.toFloat(), context)
    }

    /**
     * Get screen height in dp.
     */
    fun getScreenHeightDp(context: Context): Int {
        return pxToDpInt(context.resources.displayMetrics.heightPixels.toFloat(), context)
    }

    /**
     * Get device density.
     */
    fun getDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * Get device density dpi.
     */
    fun getDensityDpi(context: Context): Int {
        return context.resources.displayMetrics.densityDpi
    }

    /**
     * Get scaled density.
     */
    fun getScaledDensity(context: Context): Float {
        return context.resources.displayMetrics.scaledDensity
    }

    /**
     * Get xdpi.
     */
    fun getXdpi(context: Context): Float {
        return context.resources.displayMetrics.xdpi
    }

    /**
     * Get ydpi.
     */
    fun getYdpi(context: Context): Float {
        return context.resources.displayMetrics.ydpi
    }

    /**
     * Check if device is mdpi.
     */
    fun isMdpi(context: Context): Boolean {
        return getDensityDpi(context) in 120..160
    }

    /**
     * Check if device is hdpi.
     */
    fun isHdpi(context: Context): Boolean {
        return getDensityDpi(context) in 240..320
    }

    /**
     * Check if device is xhdpi.
     */
    fun isXhdpi(context: Context): Boolean {
        return getDensityDpi(context) in 320..480
    }

    /**
     * Check if device is xxhdpi.
     */
    fun isXxhdpi(context: Context): Boolean {
        return getDensityDpi(context) in 480..640
    }

    /**
     * Check if device is xxxhdpi.
     */
    fun isXxxhdpi(context: Context): Boolean {
        return getDensityDpi(context) >= 640
    }

    /**
     * Get status bar height.
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPxInt(24f, context)
        }
    }

    /**
     * Get navigation bar height.
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPxInt(48f, context)
        }
    }

    /**
     * Get action bar height.
     */
    fun getActionBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("action_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dpToPxInt(56f, context)
        }
    }

    /**
     * Apply window insets to view.
     */
    fun applyWindowInsets(view: View, applyTop: Boolean = true, applyBottom: Boolean = true) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams

            if (params is android.view.ViewGroup.MarginLayoutParams) {
                params.topMargin = if (applyTop) systemBars.top else 0
                params.bottomMargin = if (applyBottom) systemBars.bottom else 0
            }

            v.layoutParams = params
            insets
        }
    }

    /**
     * Get safe top inset.
     */
    fun getSafeTopInset(context: Context): Int {
        return getStatusBarHeight(context)
    }

    /**
     * Get safe bottom inset.
     */
    fun getSafeBottomInset(context: Context): Int {
        return getNavigationBarHeight(context)
    }

    /**
     * Get safe horizontal inset.
     */
    fun getSafeHorizontalInset(context: Context): Int {
        return 0
    }

    /**
     * Calculate aspect ratio.
     */
    fun calculateAspectRatio(width: Int, height: Int): Float {
        return width.toFloat() / height.toFloat()
    }

    /**
     * Scale dimension to fit within bounds.
     */
    fun scaleToFit(currentWidth: Int, currentHeight: Int, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        val scaleWidth = maxWidth.toFloat() / currentWidth
        val scaleHeight = maxHeight.toFloat() / currentHeight
        val scale = minOf(scaleWidth, scaleHeight)

        if (scale >= 1f) {
            return currentWidth to currentHeight
        }

        return (currentWidth * scale).toInt() to (currentHeight * scale).toInt()
    }

    /**
     * Scale dimension to fill bounds.
     */
    fun scaleToFill(currentWidth: Int, currentHeight: Int, targetWidth: Int, targetHeight: Int): Pair<Int, Int> {
        val scaleWidth = targetWidth.toFloat() / currentWidth
        val scaleHeight = targetHeight.toFloat() / currentHeight
        val scale = maxOf(scaleWidth, scaleHeight)

        return (currentWidth * scale).toInt() to (currentHeight * scale).toInt()
    }

    /**
     * Get smallest width qualifier.
     */
    fun getSmallestWidthQualifier(context: Context): Int {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp
    }

    /**
     * Check if in landscape orientation.
     */
    fun isLandscape(context: Context): Boolean {
        val orientation = context.resources.configuration.orientation
        return orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * Check if in portrait orientation.
     */
    fun isPortrait(context: Context): Boolean {
        val orientation = context.resources.configuration.orientation
        return orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * Format dimension for logging.
     */
    fun formatDimension(width: Int, height: Int): String {
        return "${width}x${height}"
    }

    /**
     * Get display metrics.
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }
}