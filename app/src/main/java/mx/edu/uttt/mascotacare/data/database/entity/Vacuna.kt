package mx.edu.uttt.mascotacare.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vacunas",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascota_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mascota_id")],
)
data class Vacuna(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "mascota_id")
    val mascotaId: Int,

    val nombre: String,
    val fecha: Long,
    val dosis: String,
    val aplicada: Boolean,
)
