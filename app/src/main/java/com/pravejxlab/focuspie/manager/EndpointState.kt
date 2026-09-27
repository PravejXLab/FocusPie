package com.pravejxlab.focuspie.manager

sealed class EndpointState {
    object Awaiting : EndpointState()
    data class AvailableEndpoints(val endpoints: List<EndpointInfo>) : EndpointState()
}

data class EndpointInfo(
    val endpointId: String,
    val endpointName: String
)

sealed class ConnectionState {
    object Awaiting : ConnectionState()
    data class Initiated(val endpointId: String, val endpointName: String, val authDigits: String) : ConnectionState()
    data class Connected(val endpoints: List<EndpointInfo>) : ConnectionState()
}