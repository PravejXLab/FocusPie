package com.pravejxlab.focuspie.manager

import kotlinx.serialization.Serializable

sealed class EndpointState {
    object Awaiting : EndpointState()
    data class AvailableEndpoints(val endpoints: List<EndpointInfo>) : EndpointState()
}

data class EndpointInfo(
    val endpointId: String,
    val endpointName: String,
    val status: Status
)

sealed class ConnectionState {
    object Awaiting : ConnectionState()
    data class Initiated(val endpointId: String, val endpointName: String, val authDigits: String) : ConnectionState()
    data class Connected(val endpoints: List<EndpointInfo>) : ConnectionState()
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
    data class TriggerStartStudy(val startTime: Long) : PayloadType()

    @Serializable
    data class Distracted(val endpointName: String) : PayloadType()

    @Serializable
    data class Disconnected(val endpointName: String) : PayloadType()

    @Serializable
    data class LiveNow(val endpointName: String) : PayloadType()
}

enum class Status {
    Awaiting, Connected, Denied, None
}