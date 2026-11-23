package mx.edu.uttt.mascotacare.domain.repository

/** Contrato de autenticación; presentación depende de domain y no de su almacenamiento local. */
interface AuthRepository {
    suspend fun existeCuenta(emailInput: String): Boolean
    suspend fun crearCuenta(emailInput: String, password: String): Boolean
    suspend fun iniciarSesion(emailInput: String, password: String): Boolean
    suspend fun cambiarPassword(emailInput: String, nuevaPassword: String): Boolean
    suspend fun eliminarCuenta(emailInput: String): Boolean
}
