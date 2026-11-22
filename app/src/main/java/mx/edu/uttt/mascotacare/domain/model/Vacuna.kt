package mx.edu.uttt.mascotacare.domain.model

data class Vacuna(
    val id: Int = 0,
    val mascotaId: Int,
    val nombre: String,
    val fecha: Long,
    val dosis: String,
    val aplicada: Boolean,
)
