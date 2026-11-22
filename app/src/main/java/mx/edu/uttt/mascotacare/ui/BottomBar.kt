package mx.edu.uttt.mascotacare.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle as AccountCircleOutlined
import androidx.compose.material.icons.outlined.CalendarMonth as CalendarMonthOutlined
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import mx.edu.uttt.mascotacare.R

private data class BarItem(
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

private val barItems = listOf(
    BarItem(R.string.nav_home, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    BarItem(R.string.nav_pets, Icons.Outlined.AccountCircleOutlined, Icons.Filled.AccountCircle),
    BarItem(R.string.nav_appointments, Icons.Outlined.CalendarMonthOutlined, Icons.Filled.CalendarMonth),
    BarItem(R.string.nav_profile, Icons.Outlined.PersonOutline, Icons.Filled.Person)
)

/** Barra de navegación inferior compartida por todas las pantallas. */
@Composable
fun MascotaNavigationBar(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        barItems.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = stringResource(item.label)
                    )
                },
                label = { Text(stringResource(item.label)) }
            )
        }
    }
}
