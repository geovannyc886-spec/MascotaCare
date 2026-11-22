package mx.edu.uttt.mascotacare.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cuenta local. La contraseña se almacena como hash con sal, nunca en texto plano. */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val salt: String,
)
