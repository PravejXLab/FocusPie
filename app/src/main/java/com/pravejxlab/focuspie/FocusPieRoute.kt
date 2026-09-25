package com.pravejxlab.focuspie

import kotlinx.serialization.Serializable

@Serializable
sealed class FocusPieRoute {
    @Serializable
    object Home : FocusPieRoute()
}