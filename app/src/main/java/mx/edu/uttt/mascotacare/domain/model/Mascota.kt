package mx.edu.uttt.mascotacare.domain.model

data class Mascota(
    val id: Int = 0,
    val nombre: String,
    val especie: String,
    val raza: String = "",
    val fechaNacimiento: Long? = null,
    val pesoKg: Double? = null,
    val fotoUri: String? = null,
)
