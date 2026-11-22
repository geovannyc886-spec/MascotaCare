package mx.edu.uttt.mascotacare.data.mapper

import mx.edu.uttt.mascotacare.data.database.entity.Mascota as MascotaEntity
import mx.edu.uttt.mascotacare.domain.model.Mascota

fun MascotaEntity.toDomain() = Mascota(
    id = id,
    nombre = nombre,
    especie = especie,
    raza = raza,
    fechaNacimiento = fechaNacimiento,
    pesoKg = pesoKg,
    fotoUri = fotoUri,
)

fun Mascota.toEntity() = MascotaEntity(
    id = id,
    nombre = nombre,
    especie = especie,
    raza = raza,
    fechaNacimiento = fechaNacimiento,
    pesoKg = pesoKg,
    fotoUri = fotoUri,
)
