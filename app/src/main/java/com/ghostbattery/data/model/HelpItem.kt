package com.ghostbattery.data.model

data class HelpItem(
    val title: String,
    val content: String,
    var isExpanded: Boolean = false,
    val iconResId: Int = 0 // Optional: Pass 0 if you don't use icons
)
