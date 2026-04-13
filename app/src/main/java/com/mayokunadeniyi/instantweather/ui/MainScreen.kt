package com.mayokunadeniyi.instantweather.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.ui.forecast.ForecastScreen
import com.mayokunadeniyi.instantweather.ui.home.HomeScreen
import com.mayokunadeniyi.instantweather.ui.search.SearchScreen
import com.mayokunadeniyi.instantweather.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object ForecastRoute

@Serializable
object SearchRoute

@Serializable
object SettingsRoute

sealed class BottomNavItem<T : Any>(
    val route: T,
    val titleResId: Int,
    val iconResId: Int
) {
    object Home : BottomNavItem<HomeRoute>(HomeRoute, R.string.home_dashboard, R.drawable.ic_home)
    object Forecast :
        BottomNavItem<ForecastRoute>(ForecastRoute, R.string.forecast, R.drawable.ic_insert_chart)

    object Search : BottomNavItem<SearchRoute>(SearchRoute, R.string.search, R.drawable.ic_search)
    object Settings :
        BottomNavItem<SettingsRoute>(SettingsRoute, R.string.settings, R.drawable.ic_settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Forecast,
        BottomNavItem.Search,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentNavItem = navItems.find { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    val title =
        currentNavItem?.titleResId?.let { stringResource(it) } ?: stringResource(R.string.app_name)
    val showBackButton = currentNavItem != null && currentNavItem !is BottomNavItem.Home

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = {
                            navController.navigate(HomeRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = stringResource(item.titleResId)
                            )
                        },
                        label = {
                            Text(text = stringResource(id = item.titleResId))
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen()
            }
            composable<ForecastRoute> {
                ForecastScreen()
            }
            composable<SearchRoute> {
                SearchScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}
