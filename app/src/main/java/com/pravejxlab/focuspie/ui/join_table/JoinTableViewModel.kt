package com.pravejxlab.focuspie.ui.join_table

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravejxlab.focuspie.domain.StudentNearbyConnectionRepository
import com.pravejxlab.focuspie.ui.common.TableInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinTableViewModel @Inject constructor(
    private val repository: StudentNearbyConnectionRepository
) : ViewModel() {

    var availableTables by mutableStateOf(listOf<TableInfo>())
        private set

    init {
        viewModelScope.launch {
            repository.availableTables.collectLatest { endpoints ->
                availableTables = endpoints
            }
        }
    }

    val connectionState = repository.connectionState

    init {
        startDiscovery()
    }

    val payloadState = repository.payloadState

    fun startDiscovery() = viewModelScope.launch {
        repository.startDiscovery()
    }

    fun requestConnection(id: String) = viewModelScope.launch {
        repository.requestConnection(id)
    }

    fun denyConnection(id: String) = viewModelScope.launch {
        repository.rejectConnection(id)
    }

    fun acceptConnection(id: String) = viewModelScope.launch {
        repository.acceptConnection(id)
    }

    fun stopDiscovery() = repository.stopDiscovery()

    override fun onCleared() {
        repository.stopDiscovery()
        repository.destroyConnection()
    }
}