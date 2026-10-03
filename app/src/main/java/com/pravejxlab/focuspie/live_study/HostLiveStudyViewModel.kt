package com.pravejxlab.focuspie.live_study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.join_table.StudentInfo
import com.pravejxlab.focuspie.join_table.toStudentsInfo
import com.pravejxlab.focuspie.manager.HostNearbyConnectionManager
import com.pravejxlab.focuspie.manager.PayloadState
import com.pravejxlab.focuspie.manager.PayloadType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HostLiveStudyViewModel @Inject constructor(
    private val manager: HostNearbyConnectionManager
) : ViewModel() {

    private val _timeLeft = MutableStateFlow(36_00_000L) // 60 minutes
    val timeLeft = _timeLeft.asStateFlow()

    var connectedStudents = listOf<StudentInfo>()
        private set

    init {
        viewModelScope.launch {
            manager.connectedEndpoints.collectLatest { endpoints ->
                connectedStudents = endpoints.values.toStudentsInfo()
            }
        }
    }

    init {
        viewModelScope.launch {
            manager.triggerStartStudy()
        }
    }

    fun calculateTimeLeft(startTime: Long) = viewModelScope.launch {
        while (_timeLeft.value > 0) {
            _timeLeft.value = 36_00_000L - (System.currentTimeMillis() - startTime)
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

                    if (type is PayloadType.Disconnected) {
                        _timeLeft.value = 0
                    }
                }
            }
        }
    }
}