package com.pravejxlab.focuspie.manager

import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.nearby.connection.AdvertisingOptions
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
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class NearbyConnectionManager @Inject constructor(
    private val client: ConnectionsClient
) {
    private val _connectedEndpoints = MutableStateFlow(setOf<EndpointInfo>())
    val connectedEndpoints = _connectedEndpoints.asStateFlow()

    private val _availableTables = MutableStateFlow(setOf<EndpointInfo>())
    val availableTables = _availableTables.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Awaiting)
    val connectionState = _connectionState.asStateFlow()

    private val _endpointState = MutableStateFlow<EndpointState>(EndpointState.Awaiting)
    val endpointState = _endpointState.asStateFlow()

    private val _payloadState = MutableStateFlow<PayloadState>(PayloadState.Awaiting)
    val payloadState = _payloadState.asStateFlow()

    val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: Connection initiated with endpoint Id - $endpointId")

            _connectedEndpoints.update { it + EndpointInfo(endpointId, info.endpointName, Status.Awaiting) }
            _connectionState.value = ConnectionState.Initiated(endpointId, info.endpointName, info.authenticationDigits)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            Log.i(TAG, "onConnectionResult: Connection result arrived for $endpointId")

            val itemToUpdate = _connectedEndpoints.value.first { it.endpointId == endpointId }

            _connectionState.value = when(resolution.status.statusCode) {
                ConnectionResult.SUCCESS -> {
                    _connectedEndpoints.update { it - itemToUpdate }
                    _connectedEndpoints.update { it + itemToUpdate.copy(status = Status.Connected) }

                    ConnectionState.Connected(_connectedEndpoints.value.toList())
                }

                ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {
                    ConnectionState.Connected(_connectedEndpoints.value.toList())
                }

                ConnectionResult.CANCELED -> {
                    _connectedEndpoints.update { it - itemToUpdate }
                    _connectedEndpoints.update { it + itemToUpdate.copy(status = Status.Denied) }

                    ConnectionState.Connected(_connectedEndpoints.value.toList())
                }
                else -> {
                    _connectedEndpoints.update { it - itemToUpdate }
                    ConnectionState.Connected(_connectedEndpoints.value.toList())
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: Connection dropped - $endpointId")

            _connectedEndpoints.update { endpoints -> endpoints - endpoints.first { it.endpointId == endpointId } }
            ConnectionState.Connected(_connectedEndpoints.value.toList())
        }
    }

    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: Endpoint found - $endpointId")

            _availableTables.update { it + EndpointInfo(endpointId, info.endpointName, Status.None) }
            _endpointState.value = EndpointState.AvailableEndpoints(_availableTables.value.toList())
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "onEndpointLost: Endpoint lost - $endpointId")

            _availableTables.update { tables -> tables - _availableTables.value.first { it.endpointId == endpointId } }
            _endpointState.value = EndpointState.AvailableEndpoints(availableTables.value.toList())
        }
    }

    val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.i(TAG, "onPayloadReceived: Received payload is - $payload")

            if (payload.type == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes() ?: return

                val jsonString = String(receivedBytes, Charsets.UTF_8)

                try {
                    val receivedState = Json.decodeFromString<PayloadType>(jsonString)
                    _payloadState.value = PayloadState.Received(receivedState)
                } catch (e: Exception) {
                    Log.e(TAG, "onPayloadReceived: ${e.message}", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when(update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    Log.i(TAG, "onPayloadTransferUpdate: Transfer successfully to - $endpointId")
                }
            }
        }
    }

    val advertisingOptions = AdvertisingOptions.Builder()
        .setStrategy(STRATEGY)
        .build()

    suspend fun startAdvertising() {
        client.startAdvertising("Pravej", SERVICE_ID, connectionLifecycleCallback, advertisingOptions).await()
    }

    val discoveryOptions = DiscoveryOptions.Builder()
        .setStrategy(STRATEGY)
        .build()

    suspend fun startDiscovery() {
        Log.i(TAG, "startDiscovery: Starting the discovery process...")
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions).await()
        Log.i(TAG, "startDiscovery: Discovery process started.")
    }

    suspend fun requestConnection(endpointId: String) {
        Log.i(TAG, "requestConnection: Requesting connection to - $endpointId")

        // Will throw TimeoutCancellationException, handle it in next sprint.
        withTimeout(30.seconds) {
            client.requestConnection("Pravej", endpointId, connectionLifecycleCallback).await()
        }

        Log.i(TAG, "requestConnection: Connection requested successfully to - $endpointId")
    }

    suspend fun acceptConnection(endpointId: String) {
        Log.i(TAG, "acceptConnection: Accepting connection...")
        client.acceptConnection(endpointId, payloadCallback).await()
        Log.i(TAG, "acceptConnection: Connection accepted with - $endpointId")
    }

    suspend fun rejectConnection(endpointId: String) {
        Log.i(TAG, "rejectConnection: Rejecting the connection process...")
        client.rejectConnection(endpointId).await()
        Log.i(TAG, "rejectConnection: Connection rejected successfully with - $endpointId")
    }

    suspend fun broadcastStudyHasStarted() {
        val availableEndpointsId = if (_connectionState.value is ConnectionState.Connected) {
            (_connectionState.value as ConnectionState.Connected).endpoints.map { it.endpointId }
        } else null

        if (availableEndpointsId.isNullOrEmpty()) return
        val jsonString = Json.encodeToString<PayloadType>(PayloadType.StartTimeBroadcast)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(availableEndpointsId, payload).await()
        _payloadState.value = PayloadState.Sent(PayloadType.StartTimeBroadcast)
        Log.i(TAG, "broadcastStudyHasStarted: Payload sent to all devices - $availableEndpointsId")
    }

    suspend fun triggerStartStudy(startTime: Long) {
        val availableEndpoints = if (_connectionState.value is ConnectionState.Connected) {
            (_connectionState.value as ConnectionState.Connected).endpoints.map { it.endpointId }
        } else null

        val jsonString = Json.encodeToString<PayloadType>(PayloadType.TriggerStartStudy(startTime))
        val payload = Payload.fromBytes(jsonString.toByteArray())

        if (availableEndpoints.isNullOrEmpty()) return
        client.sendPayload(availableEndpoints, payload).await()
        _payloadState.value = PayloadState.Sent(PayloadType.TriggerStartStudy(startTime))
        Log.i(TAG, "triggerStartStudy: Payload $payload sent to - $availableEndpoints")
    }

    fun stopAdvertisement() {
        client.stopAdvertising()
        client.stopAllEndpoints()
    }
    fun stopDiscovery() = client.stopDiscovery()

    companion object {
        private val TAG = NearbyConnectionManager::class.java.simpleName
        private val STRATEGY = Strategy.P2P_STAR
        private const val SERVICE_ID = "com.pravejxlab.focuspie"
    }
}