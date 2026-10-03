package com.pravejxlab.focuspie.manager

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class StudentNearbyConnectionManager @Inject constructor(
    private val client: ConnectionsClient
) {
    private val _availableEndpoints = MutableStateFlow<Map<String, EndpointInfo>>(emptyMap())
    val availableEndpoints = _availableEndpoints.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Awaiting)
    val connectionState = _connectionState.asStateFlow()

    private val _payloadState = MutableStateFlow<PayloadState>(PayloadState.Awaiting)
    val payloadState = _payloadState.asStateFlow()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: Connection initiated with endpoint Id - $endpointId")

            _connectionState.value = ConnectionState.Initiated(endpointId, info.endpointName, info.authenticationDigits)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val uuid = _availableEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            val endpointToUpdate = _availableEndpoints.value[uuid] ?: return

            when(resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Connected)) }
                }

                ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {}

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Denied)) }
                }

                else -> {
                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.None)) }
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: Endpoint disconnected - $endpointId")

            val uuid = _availableEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            val endpointToUpdate = _availableEndpoints.value[uuid] ?: return

            _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.None)) }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: Found endpoint is $endpointId")

            val rawData = info.endpointName.split(SPLITTER)
            val uuid = rawData.first()
            val name = rawData.last()

            _availableEndpoints.update { it + (uuid to EndpointInfo(endpointId, name, Status.Awaiting)) }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "onEndpointLost: Endpoint lost - $endpointId")

            val uuid = _availableEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            _availableEndpoints.update { endpoints -> endpoints.filterNot { it.key == uuid } }
        }
    }

    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val rawData = payload.asBytes() ?: return
                val jsonString = String(rawData, Charsets.UTF_8)

                try {
                    val data = Json.decodeFromString<PayloadType>(jsonString)
                    _payloadState.value = PayloadState.Received(data)
                } catch (e: Exception) {
                    Log.e(TAG, "onPayloadReceived: ${e.message}", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when(update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    Log.i(TAG, "onPayloadTransferUpdate: Payload transferred successfully - $endpointId")
                }

                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.e(TAG, "onPayloadTransferUpdate: Payload transfer failed")
                }
            }
        }
    }

    suspend fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions).await()
    }

    suspend fun requestConnection(endpointId: String) {
        withTimeout(30.seconds) {
            client.requestConnection(userData, endpointId, connectionLifecycleCallback,).await()
        }
    }

    suspend fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId).await()
    }

    suspend fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback).await()
    }

    fun stopDiscovery() = client.stopDiscovery()


    companion object {
        private val TAG = StudentNearbyConnectionManager::class.simpleName
        private val uuid = UUID.randomUUID().toString()
        private val deviceName = android.os.Build.MODEL.let { if (it.isNullOrBlank()) "Unknown" else it }
        private const val SPLITTER = "|<..x|-->"

        private val userData = "$uuid$SPLITTER$deviceName"
        private const val SERVICE_ID = "com.pravejxlab.focuspie"
        private val STRATEGY = Strategy.P2P_STAR
    }
}