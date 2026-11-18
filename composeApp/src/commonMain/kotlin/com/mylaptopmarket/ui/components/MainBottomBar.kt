package com.mylaptopmarket.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.tour.tourTarget

enum class MainTab {
    Home,
    Listings,
    Messages,
    Profile
}

@Composable
fun MainBottomBar(
    selected: MainTab,
    messagesBadge: Int = 0,
    onTabSelected: (MainTab) -> Unit,
    registerTour: Boolean = false
) {
    data class TabItem(val tab: MainTab, val label: String, val icon: ImageVector)

    val items = listOf(
        TabItem(MainTab.Home, "Home", Icons.Default.Home),
        TabItem(MainTab.Listings, "Listings", Icons.AutoMirrored.Filled.List),
        TabItem(MainTab.Messages, "Messages", Icons.AutoMirrored.Filled.Message),
        TabItem(MainTab.Profile, "Profile", Icons.Default.Person)
    )

    NavigationBar(containerColor = SurfaceWhite) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selected == item.tab,
                onClick = { onTabSelected(item.tab) },
                modifier = if (registerTour) {
                    Modifier.tourTarget(
                        when (item.tab) {
                            MainTab.Home -> "tab-home"
                            MainTab.Listings -> "tab-listings"
                            MainTab.Messages -> "tab-messages"
                            MainTab.Profile -> "tab-profile"
                        }
                    )
                } else {
                    Modifier
                },
                icon = {
                    if (item.tab == MainTab.Messages && messagesBadge > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(if (messagesBadge > 9) "9+" else messagesBadge.toString())
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = {
                    Text(
                        item.label,
                        fontWeight = if (selected == item.tab) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
