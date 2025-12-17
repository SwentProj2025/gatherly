package com.android.gatherly.ui.badge

import androidx.annotation.DrawableRes

/** UI model for a badge displayed in the Badge screen. */
data class BadgeUI(
    val title: String = "",
    val description: String = "",
    @DrawableRes val icon: Int = 0,
)
