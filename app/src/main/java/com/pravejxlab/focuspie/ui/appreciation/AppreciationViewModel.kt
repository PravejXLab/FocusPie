package com.pravejxlab.focuspie.ui.appreciation

import androidx.lifecycle.ViewModel
import com.pravejxlab.focuspie.domain.HostNearbyConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppreciationViewModel @Inject constructor(
    private val repository: HostNearbyConnectionRepository
) : ViewModel() {

    fun destroyConnection() {
        repository.destroyConnection()
    }

    override fun onCleared() {
        destroyConnection()
    }
}