package com.pravejxlab.focuspie.domain

import com.pravejxlab.focuspie.ui.common.TableInfo
import kotlinx.coroutines.flow.StateFlow

interface StudentNearbyConnectionRepository {

    val availableTables: StateFlow<List<TableInfo>>
    val connectionState: StateFlow<ConnectionState>
    val payloadState: StateFlow<PayloadState>

    suspend fun startDiscovery()
    suspend fun requestConnection(id: String)
    suspend fun acceptConnection(id: String)
    suspend fun rejectConnection(id: String)
    suspend fun triggerDistracted(id: String?, name: String)
    fun stopDiscovery()
    fun destroyConnection()
}