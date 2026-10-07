package com.pravejxlab.focuspie.data

import android.os.Build
import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.EndpointInfo
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
import com.pravejxlab.focuspie.domain.Status
import com.pravejxlab.focuspie.ui.common.toStudentsInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostNearbyConnectionManager @Inject constructor(
    private val client: ConnectionsClient
) {
    val scope = CoroutineScope(Dispatchers.Main)
    private val _connectedEndpoints = MutableStateFlow<Map<String, EndpointInfo>>(emptyMap())
    val connectedEndpoints = _connectedEndpoints.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Awaiting)
    val connectionState = _connectionState.asStateFlow()

    private val _payloadState = MutableStateFlow<PayloadState>(PayloadState.Awaiting)
    val payloadState = _payloadState.asStateFlow()


    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: Connection initiated with endpoint Id - $endpointId")

            val rawData = info.endpointName.split(SPLITTER)
            val uuid = rawData.first()
            val name = rawData.last()

            _connectedEndpoints.update { it + (uuid to EndpointInfo(endpointId, name, Status.Awaiting)) }
            _connectionState.value = ConnectionState.Initiated(endpointId, name, info.authenticationDigits)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val uuid = _connectedEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            val endpointToUpdate = _connectedEndpoints.value[uuid] ?: return

            when(resolution.status.statusCode) {

                ConnectionsStatusCodes.STATUS_OK -> {
                    _connectedEndpoints.update { it + (uuid to endpointToUpdate.copy(status = Status.Connected)) }
                    _connectionState.value = ConnectionState.Connected
                }

                ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {}

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _connectedEndpoints.update { endpoints -> endpoints.filterNot { it.key == uuid } }
                }

                else -> {}
            }.also {
                Log.i(TAG, "onConnectionResult: Status code - ${resolution.status.statusCode}")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: Connection dropped with - $endpointId")

            val uuid = _connectedEndpoints.value.entries.find { it.value.endpointId == endpointId }?.key ?: return
            _connectedEndpoints.update { endpoints -> endpoints.filterNot { it.key == uuid } }
            scope.launch { triggerDisconnected(endpointId) }
        }
    }

    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {

            if (payload.type == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes() ?: return
                val jsonString = String(receivedBytes, Charsets.UTF_8)

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
                    Log.i(TAG, "onPayloadTransferUpdate: Payload transferred successfully")
                }

                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.e(TAG, "onPayloadTransferUpdate: Payload transfer failed")
                }
            }
        }
    }

    suspend fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        client.startAdvertising(hostData, SERVICE_ID, connectionLifecycleCallback, advertisingOptions).await()
    }

    suspend fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId).await()
    }

    suspend fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback).await()
    }

    suspend fun triggerStudyHasStarted() {
        val endpoints = _connectedEndpoints.value.values.map { it.endpointId }
        if (endpoints.isEmpty()) return

        val data = PayloadType.StartTimeBroadcast
        val jsonString = Json.encodeToString<PayloadType>(data)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(endpoints, payload).await()
        _payloadState.value = PayloadState.Sent(data)
    }

    suspend fun triggerStartStudy() {
        val endpoints = _connectedEndpoints.value.values.map { it.endpointId }
        if (endpoints.isEmpty()) return

        val data = PayloadType.TriggerStartStudy(
            startTime = System.currentTimeMillis(),
            students = _connectedEndpoints.value.values.toStudentsInfo()
        )
        val jsonString = Json.encodeToString<PayloadType>(data)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(endpoints, payload).await()
        _payloadState.value = PayloadState.Sent(data)
    }

    suspend fun triggerDisconnected(endpointId: String) {
        val endpoints = _connectedEndpoints.value.values.map { it.endpointId } - endpointId
        if (endpoints.isEmpty()) return

        val endpointName = _connectedEndpoints.value.values.find { it.endpointId == endpointId }?.endpointName ?: "Unknown"
        val data = PayloadType.Disconnected(endpointId, endpointName)
        val jsonString = Json.encodeToString<PayloadType>(data)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(endpoints, payload).await()
        _payloadState.value = PayloadState.Sent(data)
    }

    suspend fun triggerDistracted(endpointName: String) {
        val endpoints = _connectedEndpoints.value.values.map { it.endpointId }
        if (endpoints.isEmpty()) return

        val data = PayloadType.Distracted(endpointName)
        val jsonString = Json.encodeToString<PayloadType>(data)
        val payload = Payload.fromBytes(jsonString.toByteArray())

        client.sendPayload(endpoints, payload).await()
        _payloadState.value = PayloadState.Sent(data)
    }

    fun stopAdvertisement() = client.stopAdvertising()
    fun destroyConnection() = client.stopAllEndpoints()

    companion object {
        private val TAG = HostNearbyConnectionManager::class.simpleName
        private val uuid = UUID.randomUUID().toString()
        private val deviceName = Build.MODEL.let { if (it.isNullOrBlank()) "Unknown" else it }
        private const val SPLITTER = "|<..x|-->"

        private val hostData = "$uuid$SPLITTER$deviceName"
        private const val SERVICE_ID = "com.pravejxlab.focuspie"
        private val STRATEGY = Strategy.P2P_STAR
    }
}