package com.pravejxlab.focuspie.join_table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.manager.ConnectionState
import com.pravejxlab.focuspie.manager.StudentNearbyConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinTableViewModel @Inject constructor(
    private val manager: StudentNearbyConnectionManager
) : ViewModel() {

    var availableTables = listOf<TableInfo>()

    init {
        viewModelScope.launch {
            manager.availableEndpoints.collectLatest { endpoints ->
                availableTables = endpoints.values.toTablesInfo()
            }
        }
    }

    val advertisementState = manager.connectionState.map { state ->
        when(state) {
            is ConnectionState.Awaiting -> AdvertisementState.Awaiting
            is ConnectionState.Initiated -> AdvertisementState.Initiated(state.endpointId, state.endpointName, state.authDigits)
            is ConnectionState.Connected -> AdvertisementState.Connected
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdvertisementState.Awaiting
    )

    init {
        startDiscovery()
    }

    val payloadState = manager.payloadState

    fun startDiscovery() = viewModelScope.launch {
        manager.startDiscovery()
    }

    fun requestConnection(id: String) = viewModelScope.launch {
        manager.requestConnection(id)
    }

    fun denyConnection(id: String) = viewModelScope.launch {
        manager.rejectConnection(id)
    }

    fun acceptConnection(id: String) = viewModelScope.launch {
        manager.acceptConnection(id)
    }

    override fun onCleared() {
        manager.stopDiscovery()
    }
}