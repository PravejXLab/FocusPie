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
import com.google.android.gms.nearby.connection.Strategy
import com.pravejxlab.focuspie.join_table.StudentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class NearbyConnectionManager @Inject constructor(
    private val client: ConnectionsClient
) {
    private val connectedEndpoints = mutableSetOf<EndpointInfo>()
    private val availableTables = mutableSetOf<EndpointInfo>()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Awaiting)
    val connectionState = _connectionState.asStateFlow()

    private val _endpointState = MutableStateFlow<EndpointState>(EndpointState.Awaiting)
    val endpointState = _endpointState.asStateFlow()

    val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: Connection initiated with endpoint Id - $endpointId")

            connectedEndpoints.add(EndpointInfo(endpointId, info.endpointName))
            _connectionState.value = ConnectionState.Initiated(endpointId, info.endpointName, info.authenticationDigits)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            Log.i(TAG, "onConnectionResult: Connection result arrived for $endpointId")

            _connectionState.value = when(resolution.status.statusCode) {
                ConnectionResult.SUCCESS -> ConnectionState.Connected(connectedEndpoints.toList())
                else -> {
                    connectedEndpoints.remove(connectedEndpoints.first { it.endpointId == endpointId })
                    ConnectionState.Connected(connectedEndpoints.toList())
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: Connection dropped - $endpointId")

            connectedEndpoints.remove(connectedEndpoints.first { it.endpointId == endpointId })
            ConnectionState.Connected(connectedEndpoints.toList())
        }
    }

    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: Endpoint found - $endpointId")

            availableTables.add(EndpointInfo(endpointId, info.endpointName))
            _endpointState.value = EndpointState.AvailableEndpoints(availableTables.toList())
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "onEndpointLost: Endpoint lost - $endpointId")

            availableTables.remove(availableTables.first { it.endpointId == endpointId })
            _endpointState.value = EndpointState.AvailableEndpoints(availableTables.toList())
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
        // Hold on here, because it requires payload callback.
    }

    suspend fun rejectConnection(endpointId: String) {
        Log.i(TAG, "rejectConnection: Rejecting the connection process with - $endpointId")
        client.rejectConnection(endpointId).await()
        Log.i(TAG, "rejectConnection: Connection rejected successfully with - $endpointId")
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