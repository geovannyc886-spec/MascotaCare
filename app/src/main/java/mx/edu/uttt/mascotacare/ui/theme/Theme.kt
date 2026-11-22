package mx.edu.uttt.mascotacare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MascotaColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2EBE0),
    onPrimaryContainer = Color(0xFF00201B),
    secondaryContainer = Color(0xFFE0F2EE),
    onSecondaryContainer = Color(0xFF00201B),
    tertiaryContainer = Color(0xFFFFDDB5),
    onTertiaryContainer = Color(0xFF4A2800),
    background = Color(0xFFF7FAF9),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFF7FAF9),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFEAF1EF),
    onSurfaceVariant = Color(0xFF6F7976),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBFC9C6)
)

@Composable
fun MascotaCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MascotaColors, content = content)
}
