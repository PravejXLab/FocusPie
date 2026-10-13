package com.pravejxlab.focuspie.data

import android.util.Log
import com.pravejxlab.focuspie.domain.StudentNearbyConnectionRepository
import com.pravejxlab.focuspie.ui.common.TableInfo
import com.pravejxlab.focuspie.ui.common.toTablesInfo
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.Status
import com.pravejxlab.focuspie.ui.common.StudentInfo
import com.pravejxlab.focuspie.ui.common.toStudentsInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentNearbyConnectionRepositoryImpl @Inject constructor(
    private val manager: StudentNearbyConnectionManager
): StudentNearbyConnectionRepository {

    val scope = CoroutineScope(Dispatchers.Main)

    override val availableTables: StateFlow<List<TableInfo>>
        get() = manager.availableEndpoints.map { endpoints ->
            endpoints.values.toTablesInfo()
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override val connectionState: StateFlow<ConnectionState>
        get() = manager.connectionState

    override val payloadState: StateFlow<PayloadState>
        get() = manager.payloadState

    override suspend fun startDiscovery() {
        Log.i(TAG, "startDiscovery: Starting the discovery process...")

        try {
            manager.startDiscovery()

            Log.i(TAG, "startDiscovery: Discovery has started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "startDiscovery: ${e.message}", e)
        }
    }

    override suspend fun acceptConnection(id: String) {
        Log.i(TAG, "acceptConnection: Accepting the connection request with host - $id")

        try {
            manager.acceptConnection(id)

            Log.i(TAG, "acceptConnection: Connection request accepted successfully with host - $id")
        } catch (e: Exception) {
            Log.e(TAG, "acceptConnection: ${e.message}", e)
        }
    }

    override suspend fun rejectConnection(id: String) {
        Log.i(TAG, "rejectConnection: Rejecting the connection with host - $id")

        try {
            manager.rejectConnection(id)

            Log.i(TAG, "rejectConnection: Connection request rejected successfully with host - $id")
        } catch (e: Exception) {
            Log.e(TAG, "rejectConnection: ${e.message}", e)
        }
    }

    override suspend fun requestConnection(id: String) {
        Log.i(TAG, "requestConnection: Requesting connection with host - $id")

        try {
            manager.requestConnection(id)

            Log.i(TAG, "requestConnection: Connection request initiated with host - $id")
        } catch (e: Exception) {
            Log.e(TAG, "requestConnection: ${e.message}", e)
        }
    }

    override suspend fun triggerDistracted(id: String?, name: String) {
        Log.i(TAG, "triggerDistracted: Triggering distracted event to host...")

        try {
            manager.triggerDistracted(id, name)

            Log.i(TAG, "triggerDistracted: Distracted event sent to host.")
        } catch (e: Exception) {
            Log.e(TAG, "triggerDistracted: ${e.message}", e)
        }
    }

    override fun stopDiscovery() {
        try {
            manager.stopDiscovery()

            Log.i(TAG, "stopDiscovery: Discovery stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "stopDiscovery: ${e.message}", e)
        }
    }

    override fun destroyConnection() {
        try {
            manager.destroyConnection()

            Log.i(TAG, "destroyConnection: Connection destroyed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "destroyConnection: Failed to destroy connection", e)
        }
    }

    companion object {
        private val TAG = StudentNearbyConnectionRepositoryImpl::class.simpleName
    }
}