package mx.edu.uttt.mascotacare.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.edu.uttt.mascotacare.data.database.AppDatabase
import mx.edu.uttt.mascotacare.data.database.dao.CitaDao
import mx.edu.uttt.mascotacare.data.database.dao.MascotaDao
import mx.edu.uttt.mascotacare.data.database.dao.UsuarioDao
import mx.edu.uttt.mascotacare.data.database.dao.VacunaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mascotacare.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides fun provideMascotaDao(database: AppDatabase): MascotaDao = database.mascotaDao()
    @Provides fun provideCitaDao(database: AppDatabase): CitaDao = database.citaDao()
    @Provides fun provideUsuarioDao(database: AppDatabase): UsuarioDao = database.usuarioDao()
    @Provides fun provideVacunaDao(database: AppDatabase): VacunaDao = database.vacunaDao()
}
