package com.pravejxlab.focuspie.ui.host_table

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.domain.HostNearbyConnectionRepository
import com.pravejxlab.focuspie.ui.common.StudentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HostTableViewModel @Inject constructor(
    private val repository: HostNearbyConnectionRepository
) : ViewModel() {

    var connectedStudents by mutableStateOf(setOf<StudentInfo>())
        private set

    init {
        viewModelScope.launch {
            repository.connectedEndpoints.collectLatest { endpoints ->
                connectedStudents += endpoints
            }
        }
    }

    val connectionState = repository.connectionState

    val payloadState = repository.payloadState

    init {
        startAdvertising()
    }

    fun startAdvertising() = viewModelScope.launch {
        repository.startAdvertising()
    }

    fun denyConnection(id: String) = viewModelScope.launch {
        repository.rejectConnection(id)
    }

    fun acceptConnection(id: String) = viewModelScope.launch {
        repository.acceptConnection(id)
    }

    fun startStudy() = viewModelScope.launch {
        repository.stopAdvertising()
        repository.triggerStudyHasStarted()
    }

    override fun onCleared() {
        repository.stopAdvertising()
        repository.destroyConnection()
    }
}