package mx.edu.uttt.mascotacare.data.di

import android.content.Context
import androidx.room.Room
import mx.edu.uttt.mascotacare.data.database.AppDatabase

/** Proveedor central de la base de datos y sus DAO. */
object DatabaseModule {
    @Volatile
    private var instancia: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase =
        instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mascotacare.db"
            ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .build().also { instancia = it }
        }
}
