package com.pravejxlab.focuspie.host_table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.join_table.AdvertisementState
import com.pravejxlab.focuspie.join_table.StudentInfo
import com.pravejxlab.focuspie.join_table.toStudentsInfo
import com.pravejxlab.focuspie.manager.ConnectionState
import com.pravejxlab.focuspie.manager.HostNearbyConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostTableViewModel @Inject constructor(
    private val manager: HostNearbyConnectionManager
) : ViewModel() {

    var connectedStudents = listOf<StudentInfo>()

    init {
        viewModelScope.launch {
            manager.connectedEndpoints.collectLatest { endpoints ->
                connectedStudents = endpoints.values.toStudentsInfo()
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

    val payloadState = manager.payloadState

    init {
        startAdvertising()
    }

    fun startAdvertising() = viewModelScope.launch {
        manager.startAdvertising()
    }

    fun denyConnection(id: String) = viewModelScope.launch {
        manager.rejectConnection(id)
    }

    fun acceptConnection(id: String) = viewModelScope.launch {
        manager.acceptConnection(id)
    }

    fun startStudy() = viewModelScope.launch {
        manager.broadcastStudyHasStarted()
    }

    override fun onCleared() {
        manager.stopAdvertisement()
    }
}