package mx.edu.uttt.mascotacare.data.mapper

import mx.edu.uttt.mascotacare.data.database.entity.Cita as CitaEntity
import mx.edu.uttt.mascotacare.domain.model.Cita

fun CitaEntity.toDomain() = Cita(
    id = id,
    mascotaId = mascotaId,
    motivo = motivo,
    fecha = fecha,
    hora = hora,
    minuto = minuto,
    clinica = clinica,
    recordatorio = recordatorio,
)

fun Cita.toEntity() = CitaEntity(
    id = id,
    mascotaId = mascotaId,
    motivo = motivo,
    fecha = fecha,
    hora = hora,
    minuto = minuto,
    clinica = clinica,
    recordatorio = recordatorio,
)
