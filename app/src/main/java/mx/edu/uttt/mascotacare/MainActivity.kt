package mx.edu.uttt.mascotacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.uttt.mascotacare.data.repository.LocalAuthRepository
import mx.edu.uttt.mascotacare.ui.AppNavigation
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: LocalAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MascotaCareTheme {
                AppNavigation(authRepository = authRepository)
            }
        }
    }
}
