package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
    val successMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: LocalAuthRepository,
) : ViewModel() {
    private var retryAction: (() -> Unit)? = null
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // UDF: login, registro y cierre de sesión son acciones públicas; Observer: uiState es de solo lectura.
    fun iniciarSesion(email: String, password: String): Job = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) { _uiState.update { it.copy(errorMessage = "auth_fields_required") }; return@launch }
        retryAction = { iniciarSesion(email, password) }
        ejecutarAutenticacion(
            errorAlFallar = {
                if (authRepository.existeCuenta(email)) "auth_wrong_password" else "auth_email_not_found"
            },
            operacion = { authRepository.iniciarSesion(email, password) },
            email = email,
        )
    }

    fun crearCuenta(email: String, password: String): Job = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) { _uiState.update { it.copy(errorMessage = "auth_fields_required") }; return@launch }
        if (password.length < 6) { _uiState.update { it.copy(errorMessage = "auth_weak_password") }; return@launch }
        retryAction = { crearCuenta(email, password) }
        ejecutarAutenticacion(
            errorAlFallar = { "auth_account_exists" },
            operacion = { authRepository.crearCuenta(email, password) },
            email = email,
        )
    }

    fun limpiarError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearMessage() { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }
    fun reintentar() { retryAction?.invoke() }

    fun cambiarPassword(nuevaPassword: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val email = _uiState.value.userEmail ?: return@launch
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        try {
            if (nuevaPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "auth_fields_required") }
            } else if (nuevaPassword.length < 6) {
                _uiState.update { it.copy(errorMessage = "auth_weak_password") }
            } else if (authRepository.cambiarPassword(email, nuevaPassword)) {
                _uiState.update { it.copy(successMessage = "auth_password_changed") }
                onSuccess()
            }
            else _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "database_error") }
        } finally { _uiState.update { it.copy(isLoading = false) } }
    }

    fun eliminarCuenta(onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val email = _uiState.value.userEmail ?: return@launch
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            if (authRepository.eliminarCuenta(email)) {
                _uiState.value = AuthUiState(successMessage = "auth_account_deleted")
                onSuccess()
            } else _uiState.update { it.copy(errorMessage = "auth_unavailable") }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "database_error") }
        } finally { _uiState.update { it.copy(isLoading = false) } }
    }

    fun cerrarSesion() {
        _uiState.value = AuthUiState()
    }

    private suspend fun ejecutarAutenticacion(
        errorAlFallar: suspend () -> String,
        operacion: suspend () -> Boolean,
        email: String,
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        try {
            if (operacion()) {
                retryAction = null
                _uiState.update {
                    it.copy(userEmail = email.trim().lowercase(), isAuthenticated = true)
                }
            } else {
                retryAction = null
                _uiState.update { it.copy(errorMessage = errorAlFallar()) }
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(errorMessage = "database_error")
            }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
