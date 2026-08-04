package com.kidshield.agent.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidshield.agent.data.repository.KidShieldRepository
import com.kidshield.agent.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val repository: KidShieldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Idle)
    val uiState: StateFlow<PairingUiState> = _uiState

    fun pairDevice(code: String) {
        if (code.length != Constants.PAIRING_CODE_LENGTH) {
            _uiState.value = PairingUiState.Error("Enter valid 8-digit code")
            return
        }
        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading
            val result = repository.pairDevice(code)
            _uiState.value = if (result.isSuccess) {
                PairingUiState.Success
            } else {
                PairingUiState.Error(result.exceptionOrNull()?.message ?: "Pairing failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = PairingUiState.Idle
    }
}

sealed class PairingUiState {
    object Idle : PairingUiState()
    object Loading : PairingUiState()
    object Success : PairingUiState()
    data class Error(val message: String) : PairingUiState()
}
