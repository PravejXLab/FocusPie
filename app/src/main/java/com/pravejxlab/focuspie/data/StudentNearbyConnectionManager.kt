package com.pravejxlab.focuspie.data

import android.os.Build
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
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.EndpointInfo
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
import com.pravejxlab.focuspie.domain.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class StudentNearbyConnectionManager @Inject constructor(
    private val client: ConnectionsClient
) {
    val scope = CoroutineScope(Dispatchers.Main)

    private val _availableEndpoints = MutableStateFlow<Map<String, EndpointInfo>>(emptyMap())
    val availableEndpoints = _availableEndpoints.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Awaiting)
    val connectionState = _connectionState.asStateFlow()

    private val _payloadState = MutableStateFlow<PayloadState>(PayloadState.Awaiting)
    val payloadState = _payloadState.asStateFlow()

    init {
        scope.launch {
            _availableEndpoints.collectLatest { endpoints ->
                Log.i(TAG, "Endpoints: $endpoints")
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: Connection initiated with endpoint Id - $endpointId")

            val name = info.endpointName.split(SPLITTER).last()
            _connectionState.value = ConnectionState.Initiated(endpointId, name, info.authenticationDigits)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val uuid = _availableEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            val endpointToUpdate = _availableEndpoints.value[uuid] ?: return

            when(resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    val connectedHost = _availableEndpoints.value.filter { it.value.status == Status.Connected }
                    if (connectedHost.isNotEmpty()) client.disconnectFromEndpoint(connectedHost.values.first().endpointId)

                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Connected)) }
                    _connectionState.value = ConnectionState.Connected
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Denied)) }
                    _connectionState.value = ConnectionState.Connected
                }

                else -> {
                    _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.None)) }
                    _connectionState.value = ConnectionState.Connected
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: Endpoint disconnected - $endpointId")

            val uuid = _availableEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            val endpointToUpdate = _availableEndpoints.value[uuid] ?: return

            _availableEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Disconnected)) }
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
                    Log.i(TAG, "onPayloadReceived: $data")
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

    suspend fun triggerDistracted(endpointId: String?, endpointName: String) {
        if (endpointId.isNullOrBlank()) return

        val data = PayloadType.Distracted(endpointName)
        val jsonString = Json.encodeToString<PayloadType>(data)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(endpointId, payload).await()
        _payloadState.value = PayloadState.Sent(PayloadType.Distracted(endpointName))
    }

    fun stopDiscovery() = client.stopDiscovery()
    fun destroyConnection() = client.stopAllEndpoints()


    companion object {
        private val TAG = StudentNearbyConnectionManager::class.simpleName
        private val uuid = UUID.randomUUID().toString()
        private val deviceName = Build.MODEL.let { if (it.isNullOrBlank()) "Unknown" else it }
        private const val SPLITTER = "|<..x|-->"

        private val userData = "$uuid$SPLITTER$deviceName"
        private const val SERVICE_ID = "com.pravejxlab.focuspie"
        private val STRATEGY = Strategy.P2P_STAR
    }
}