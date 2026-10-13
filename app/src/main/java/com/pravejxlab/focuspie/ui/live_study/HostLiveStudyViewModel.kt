package com.pravejxlab.focuspie.ui.live_study

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.ui.common.StudentInfo
import com.pravejxlab.focuspie.domain.HostNearbyConnectionRepository
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
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
    private val repository: HostNearbyConnectionRepository
) : ViewModel() {

    private val _timeLeft = MutableStateFlow(36_00_000L) // 60 minutes
    val timeLeft = _timeLeft.asStateFlow()

    var connectedStudents by mutableStateOf(setOf<StudentInfo>())
        private set

    init {
        connectedStudents += self

        viewModelScope.launch {
            repository.connectedEndpoints.collectLatest { endpoints ->
                connectedStudents += endpoints
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.triggerStartStudy()
        }
    }

    fun calculateTimeLeft(startTime: Long) = viewModelScope.launch {
        while (_timeLeft.value > 1) {
            _timeLeft.value = 36_00_000L - (System.currentTimeMillis() - startTime)
            delay(500.milliseconds)
        }
    }

    init {
        viewModelScope.launch {
            repository.payloadState.collectLatest { state ->

                when(state) {
                    is PayloadState.Awaiting -> {}

                    is PayloadState.Sent -> {
                        val type = state.payloadType

                        if (type is PayloadType.TriggerStartStudy) {
                            calculateTimeLeft(type.startTime)
                        }

                        if (type is PayloadType.Disconnected) {
                            _timeLeft.value = 0
                        }
                    }

                    is PayloadState.Received -> {
                        val type = state.payloadType

                        if (type is PayloadType.Distracted) {
                            repository.triggerDistracted(type.endpointName)
                            _timeLeft.value = 0
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        repository.stopAdvertising()
        repository.destroyConnection()
    }

    companion object {
        val self = StudentInfo("", android.os.Build.MODEL ?: "Self")
    }
}