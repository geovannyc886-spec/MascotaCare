package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.uttt.mascotacare.data.repository.LocalAuthRepository

data class AuthUiState(
    val userEmail: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: LocalAuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // UDF: login, registro y cierre de sesión son acciones públicas; Observer: uiState es de solo lectura.
    fun iniciarSesion(email: String, password: String) = viewModelScope.launch {
        ejecutarAutenticacion(
            errorAlFallar = "auth_invalid_credentials",
            operacion = { authRepository.iniciarSesion(email, password) },
            email = email,
        )
    }

    fun crearCuenta(email: String, password: String) = viewModelScope.launch {
        ejecutarAutenticacion(
            errorAlFallar = "auth_account_exists",
            operacion = { authRepository.crearCuenta(email, password) },
            email = email,
        )
    }

    fun limpiarError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun cambiarPassword(nuevaPassword: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val email = _uiState.value.userEmail ?: return@launch
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            if (nuevaPassword.length < 6) {
                _uiState.update { it.copy(errorMessage = "auth_short_password") }
            } else if (authRepository.cambiarPassword(email, nuevaPassword)) onSuccess()
            else _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } finally { _uiState.update { it.copy(isLoading = false) } }
    }

    fun eliminarCuenta(onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val email = _uiState.value.userEmail ?: return@launch
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            if (authRepository.eliminarCuenta(email)) {
                _uiState.value = AuthUiState()
                onSuccess()
            } else _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } finally { _uiState.update { it.copy(isLoading = false) } }
    }

    fun cerrarSesion() {
        _uiState.value = AuthUiState()
    }

    private suspend fun ejecutarAutenticacion(
        errorAlFallar: String,
        operacion: suspend () -> Boolean,
        email: String,
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            if (operacion()) {
                _uiState.update {
                    it.copy(userEmail = email.trim().lowercase(), isAuthenticated = true)
                }
            } else {
                _uiState.update { it.copy(errorMessage = errorAlFallar) }
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(errorMessage = "auth_unavailable")
            }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
