package mx.edu.uttt.mascotacare.data.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import mx.edu.uttt.mascotacare.data.database.dao.CitaDao
import mx.edu.uttt.mascotacare.data.database.dao.MascotaDao
import mx.edu.uttt.mascotacare.data.database.dao.UsuarioDao
import mx.edu.uttt.mascotacare.data.database.dao.VacunaDao
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.data.database.entity.Usuario
import mx.edu.uttt.mascotacare.data.database.entity.Vacuna

@Database(
    entities = [Mascota::class, Cita::class, Usuario::class, Vacuna::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mascotaDao(): MascotaDao
    abstract fun citaDao(): CitaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun vacunaDao(): VacunaDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `usuarios` (" +
                        "`email` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`salt` TEXT NOT NULL, PRIMARY KEY(`email`))"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `vacunas` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`mascota_id` INTEGER NOT NULL, " +
                        "`nombre` TEXT NOT NULL, " +
                        "`fecha` INTEGER NOT NULL, " +
                        "`dosis` TEXT NOT NULL, " +
                        "`aplicada` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`mascota_id`) REFERENCES `mascotas`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vacunas_mascota_id` " +
                        "ON `vacunas` (`mascota_id`)"
                )
            }
        }

    }
}
