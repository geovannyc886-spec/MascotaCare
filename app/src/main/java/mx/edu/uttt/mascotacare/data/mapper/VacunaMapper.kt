package mx.edu.uttt.mascotacare.data.mapper

import mx.edu.uttt.mascotacare.data.database.entity.Vacuna as VacunaEntity
import mx.edu.uttt.mascotacare.domain.model.Vacuna

fun VacunaEntity.toDomain() = Vacuna(
    id = id,
    mascotaId = mascotaId,
    nombre = nombre,
    fecha = fecha,
    dosis = dosis,
    aplicada = aplicada,
)

fun Vacuna.toEntity() = VacunaEntity(
    id = id,
    mascotaId = mascotaId,
    nombre = nombre,
    fecha = fecha,
    dosis = dosis,
    aplicada = aplicada,
)
