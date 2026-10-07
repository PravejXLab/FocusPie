package com.pravejxlab.focuspie.data

import android.util.Log
import com.pravejxlab.focuspie.domain.HostNearbyConnectionRepository
import com.pravejxlab.focuspie.ui.common.StudentInfo
import com.pravejxlab.focuspie.ui.common.toStudentsInfo
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.PayloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostNearbyConnectionRepositoryImpl @Inject constructor(
    private val manager: HostNearbyConnectionManager
): HostNearbyConnectionRepository {
    val scope = CoroutineScope(Dispatchers.Main)

    override val connectedEndpoints: StateFlow<List<StudentInfo>>
        get() = manager.connectedEndpoints.map { endpoints ->
            endpoints.values.toStudentsInfo()
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override val connectionState: StateFlow<ConnectionState>
        get() = manager.connectionState

    override val payloadState: StateFlow<PayloadState>
        get() = manager.payloadState

    override suspend fun startAdvertising() {
        Log.i(TAG, "startAdvertising: Starting advertisement...")

        try {
            manager.startAdvertising()

            Log.i(TAG, "startAdvertising: Advertisement started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "startAdvertising: ${e.message}", e)
        }
    }

    override suspend fun acceptConnection(id: String) {
        Log.i(TAG, "acceptConnection: Accepting connection request with - $id")

        try {
            manager.acceptConnection(id)

            Log.i(TAG, "acceptConnection: Connection request accepted with - $id")
        } catch (e: Exception) {
            Log.e(TAG, "acceptConnection: ${e.message}", e)
        }
    }

    override suspend fun rejectConnection(id: String) {
        Log.i(TAG, "rejectConnection: Rejecting connection request with - $id")

        try {
            manager.rejectConnection(id)

            Log.i(TAG, "rejectConnection: Connection request rejected with - $id")
        } catch (e: Exception) {
            Log.e(TAG, "rejectConnection: ${e.message}", e)
        }
    }

    override suspend fun triggerStudyHasStarted() {
        Log.i(TAG, "triggerStudyHasStarted: Triggering study has started with all devices...")

        try {
            manager.triggerStudyHasStarted()

            Log.i(TAG, "triggerStudyHasStarted: Trigger study has started sent to all devices")
        } catch (e: Exception) {
            Log.e(TAG, "triggerStudyHasStarted: ${e.message}", e)
        }
    }

    override suspend fun triggerStartStudy() {
        Log.i(TAG, "triggerStartStudy: Triggering to start the study now to all devices...")

        try {
            manager.triggerStartStudy()

            Log.i(TAG, "triggerStartStudy: Trigger to start study sent to all devices")
        } catch (e: Exception) {
            Log.e(TAG, "triggerStartStudy: ${e.message}", e)
        }
    }

    override suspend fun triggerDisconnected(id: String) {
        Log.i(TAG, "triggerDisconnected: Triggering distracted event to all devices...")

        try {
            manager.triggerDisconnected(id)

            Log.i(TAG, "triggerDisconnected: Trigger disconnected sent to all devices")
        } catch (e: Exception) {
            Log.e(TAG, "triggerDisconnected: ${e.message}", e)
        }
    }

    override suspend fun triggerDistracted(name: String) {
        Log.i(TAG, "triggerDisconnected: Triggering distracted event to all devices...")

        try {
            manager.triggerDistracted(name)

            Log.i(TAG, "triggerDisconnected: Trigger disconnected sent to all devices")
        } catch (e: Exception) {
            Log.e(TAG, "triggerDisconnected: ${e.message}", e)
        }
    }

    override fun stopAdvertising() {
        try {
            manager.stopAdvertisement()

            Log.i(TAG, "stopAdvertising: Advertisement stopped successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "stopAdvertising: ${e.message}", e)
        }
    }

    override fun destroyConnection() {
        try {
            manager.destroyConnection()

            Log.i(TAG, "destroyConnection: Connection destroy, all connected endpoints are thrown away.")
        } catch (e: Exception) {
            Log.e(TAG, "destroyConnection: Failed to destroy connection", e)
        }
    }

    companion object {
        private val TAG = HostNearbyConnectionRepositoryImpl::class.simpleName
    }
}