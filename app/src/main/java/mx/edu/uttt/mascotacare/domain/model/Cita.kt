package mx.edu.uttt.mascotacare.domain.model

data class Cita(
    val id: Int = 0,
    val mascotaId: Int,
    val motivo: String,
    val fecha: Long,
    val hora: Int,
    val minuto: Int,
    val clinica: String = "",
    val recordatorio: Boolean = true,
)
