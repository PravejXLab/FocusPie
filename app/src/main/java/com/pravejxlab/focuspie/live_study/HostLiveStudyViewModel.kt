package com.pravejxlab.focuspie.live_study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.join_table.StudentInfo
import com.pravejxlab.focuspie.manager.NearbyConnectionManager
import com.pravejxlab.focuspie.manager.PayloadState
import com.pravejxlab.focuspie.manager.PayloadType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HostLiveStudyViewModel @Inject constructor(
    private val manager: NearbyConnectionManager
) : ViewModel() {

    private val _timeLeft = MutableStateFlow(36_00_000L) // 60 minutes
    val timeLeft = _timeLeft.asStateFlow()

    val connectedStudents = manager.connectedEndpoints.map { endpoints ->
        endpoints.map { (endpointId, endpointName, status) ->
            StudentInfo(
                id = endpointId,
                name = endpointName
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            manager.triggerStartStudy(System.currentTimeMillis())
        }
    }

    fun calculateTimeLeft(startTime: Long) = viewModelScope.launch {
        while (true) {
            _timeLeft.value = System.currentTimeMillis() - startTime
            delay(500.milliseconds)
        }
    }

    init {
        viewModelScope.launch {
            manager.payloadState.collectLatest { state ->
                if (state is PayloadState.Sent) {
                    val type = state.payloadType

                    if (type is PayloadType.TriggerStartStudy) {
                        calculateTimeLeft(type.startTime)
                    }
                }
            }
        }
    }
}