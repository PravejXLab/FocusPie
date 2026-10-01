package com.pravejxlab.focuspie

import kotlinx.serialization.Serializable

@Serializable
sealed class FocusPieRoute {
    @Serializable
    object Home : FocusPieRoute()

    @Serializable
    object HostTable : FocusPieRoute()

    @Serializable
    object JoinTable : FocusPieRoute()

    @Serializable
    object HostLiveStudy : FocusPieRoute()

    @Serializable
    object StudentLiveStudy
}