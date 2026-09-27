package com.pravejxlab.focuspie.join_table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.manager.ConnectionState
import com.pravejxlab.focuspie.manager.EndpointState
import com.pravejxlab.focuspie.manager.NearbyConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinTableViewModel @Inject constructor(
    private val manager: NearbyConnectionManager
) : ViewModel() {

    val discoveryState = manager.endpointState.map { state ->
        when(state) {
            is EndpointState.Awaiting -> DiscoveryState.Awaiting
            is EndpointState.AvailableEndpoints -> DiscoveryState.AvailableTables(state.endpoints.toTablesInfo())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiscoveryState.Awaiting
    )

    val advertisementState = manager.connectionState.map { state ->
        when(state) {
            is ConnectionState.Awaiting -> AdvertisementState.Awaiting
            is ConnectionState.Initiated -> AdvertisementState.Initiated(state.endpointId, state.endpointName, state.authDigits)
            is ConnectionState.Connected -> AdvertisementState.Connected(state.endpoints.toStudentsInfo())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdvertisementState.Awaiting
    )

    init {
        startDiscovery()
    }

    fun startDiscovery() = viewModelScope.launch {
        manager.startDiscovery()
    }

    fun requestConnection(id: String) = viewModelScope.launch {
        manager.requestConnection(id)
    }

    fun denyConnection(id: String) = viewModelScope.launch {
        manager.rejectConnection(id)
    }

    override fun onCleared() {
        manager.stopDiscovery()
    }
}