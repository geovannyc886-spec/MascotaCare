package mx.edu.uttt.mascotacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.uttt.mascotacare.ui.AppNavigation
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MascotaCareTheme {
                AppNavigation()
            }
        }
    }
}
