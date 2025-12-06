package com.example.coffeeshop.Utils

import android.util.TypedValue
import android.content.res.Resources

/**
 * Extension function to convert dp to pixels
 */
fun Int.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

