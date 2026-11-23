package mx.edu.uttt.mascotacare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
// Hilt inicializa aquí el contenedor de dependencias de toda la aplicación.
class MascotaCareApp : Application()
