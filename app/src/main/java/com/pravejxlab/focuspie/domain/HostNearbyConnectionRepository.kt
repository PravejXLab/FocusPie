package com.pravejxlab.focuspie.domain

import com.pravejxlab.focuspie.ui.common.StudentInfo
import kotlinx.coroutines.flow.StateFlow

interface HostNearbyConnectionRepository {

    val connectedEndpoints: StateFlow<List<StudentInfo>>
    val connectionState: StateFlow<ConnectionState>
    val payloadState: StateFlow<PayloadState>

    suspend fun startAdvertising()
    suspend fun acceptConnection(id: String)
    suspend fun rejectConnection(id: String)
    suspend fun triggerStudyHasStarted()
    suspend fun triggerStartStudy()
    suspend fun triggerDisconnected(id: String)
    suspend fun triggerDistracted(name: String)
    fun stopAdvertising()
    fun destroyConnection()
}