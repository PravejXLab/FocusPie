package com.pravejxlab.focuspie.domain

import com.pravejxlab.focuspie.ui.common.StudentInfo
import kotlinx.serialization.Serializable

data class EndpointInfo(
    val endpointId: String,
    val endpointName: String,
    val status: Status
)

sealed class ConnectionState {
    object Awaiting : ConnectionState()
    data class Initiated(val id: String, val name: String, val authDigits: String) : ConnectionState()
    object Connected : ConnectionState()
}

sealed class PayloadState {
    object Awaiting : PayloadState()
    data class Sent(val payloadType: PayloadType) : PayloadState()
    data class Received(val payloadType: PayloadType) : PayloadState()
}

@Serializable
sealed class PayloadType {
    @Serializable
    object StartTimeBroadcast : PayloadType()

    @Serializable
    data class TriggerStartStudy(val startTime: Long, val students: List<StudentInfo>) : PayloadType()

    @Serializable
    data class Distracted(val endpointName: String) : PayloadType()

    @Serializable
    data class Disconnected(val endpointId: String, val endpointName: String) : PayloadType()
}

enum class Status {
    Awaiting, Connected, Denied, None, Disconnected
}