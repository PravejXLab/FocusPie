package com.pravejxlab.focuspie.ui.live_study

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.ui.common.StudentInfo
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
import com.pravejxlab.focuspie.domain.StudentNearbyConnectionRepository
import com.pravejxlab.focuspie.ui.common.TableInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class StudentLiveStudyViewModel @Inject constructor(
    private val repository: StudentNearbyConnectionRepository
) : ViewModel() {

    private var host: TableInfo? = null

    private val _timeLeft = MutableStateFlow(36_00_000L) // 60 minutes
    val timeLeft = _timeLeft.asStateFlow()

    var connectedStudents by mutableStateOf(setOf<StudentInfo>())
        private set

    init {
        viewModelScope.launch {
            repository.availableTables.collectLatest { table ->
                host = table.firstOrNull()
                connectedStudents += table.map { StudentInfo(it.id, it.name) }
            }
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
            repository.payloadState.collectLatest { state ->

                when(state) {
                    is PayloadState.Awaiting -> {}

                    is PayloadState.Sent -> {
                        val type = state.payloadType

                        if (type is PayloadType.Distracted) {
                            _timeLeft.value = 0
                        }
                    }

                    is PayloadState.Received -> {
                        val type = state.payloadType

                        if (type is PayloadType.TriggerStartStudy) {
                            connectedStudents += type.students
                            calculateTimeLeft(type.startTime)
                        }

                        if (type is PayloadType.Disconnected) {
                            _timeLeft.value = 0
                        }
                        if (type is PayloadType.Distracted) {
                            _timeLeft.value = 0
                        }
                    }
                }
            }
        }
    }

    fun triggerDistracted() = viewModelScope.launch {
        repository.triggerDistracted(host?.id, SelfName)
    }

    override fun onCleared() {
        repository.stopDiscovery()
        repository.destroyConnection()
    }

    companion object {
        private val SelfName = android.os.Build.MODEL ?: "Unknown"
    }
}