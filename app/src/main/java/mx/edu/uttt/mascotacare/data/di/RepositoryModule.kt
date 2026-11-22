package mx.edu.uttt.mascotacare.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.edu.uttt.mascotacare.data.repository.CitaRepositoryImpl
import mx.edu.uttt.mascotacare.data.repository.MascotaRepositoryImpl
import mx.edu.uttt.mascotacare.data.repository.VacunaRepositoryImpl
import mx.edu.uttt.mascotacare.domain.repository.CitaRepository
import mx.edu.uttt.mascotacare.domain.repository.MascotaRepository
import mx.edu.uttt.mascotacare.domain.repository.VacunaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindMascotaRepository(impl: MascotaRepositoryImpl): MascotaRepository
    @Binds @Singleton abstract fun bindCitaRepository(impl: CitaRepositoryImpl): CitaRepository
    @Binds @Singleton abstract fun bindVacunaRepository(impl: VacunaRepositoryImpl): VacunaRepository
}
