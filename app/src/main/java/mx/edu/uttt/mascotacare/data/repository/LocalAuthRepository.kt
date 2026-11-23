package mx.edu.uttt.mascotacare.data.repository

import android.util.Base64
import mx.edu.uttt.mascotacare.data.database.dao.UsuarioDao
import mx.edu.uttt.mascotacare.data.database.entity.Usuario
import mx.edu.uttt.mascotacare.domain.repository.AuthRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/** Implementación local del contrato de autenticación usando el DAO de usuarios. */
class LocalAuthRepository @Inject constructor(private val usuarioDao: UsuarioDao) : AuthRepository {
    override suspend fun existeCuenta(emailInput: String): Boolean =
        usuarioDao.obtenerPorEmail(emailInput.trim().lowercase()) != null

    override suspend fun crearCuenta(emailInput: String, password: String): Boolean {
        val email = emailInput.trim().lowercase()
        if (usuarioDao.obtenerPorEmail(email) != null) return false

        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        usuarioDao.insertar(
            Usuario(
                email = email,
                passwordHash = hash(password, salt),
                salt = Base64.encodeToString(salt, Base64.NO_WRAP)
            )
        )
        return true
    }

    override suspend fun iniciarSesion(emailInput: String, password: String): Boolean {
        val usuario = usuarioDao.obtenerPorEmail(emailInput.trim().lowercase()) ?: return false
        val salt = Base64.decode(usuario.salt, Base64.NO_WRAP)
        return hash(password, salt) == usuario.passwordHash
    }

    override suspend fun cambiarPassword(emailInput: String, nuevaPassword: String): Boolean {
        val email = emailInput.trim().lowercase()
        val usuario = usuarioDao.obtenerPorEmail(email) ?: return false
        val nuevaSal = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        usuarioDao.actualizar(
            usuario.copy(
                passwordHash = hash(nuevaPassword, nuevaSal),
                salt = Base64.encodeToString(nuevaSal, Base64.NO_WRAP)
            )
        )
        return true
    }

    override suspend fun eliminarCuenta(emailInput: String): Boolean {
        val usuario = usuarioDao.obtenerPorEmail(emailInput.trim().lowercase()) ?: return false
        usuarioDao.eliminar(usuario)
        return true
    }

    private fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        const val SALT_BYTES = 16
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
    }
}
