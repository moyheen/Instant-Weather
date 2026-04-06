package com.mayokunadeniyi.instantweather.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { SharedPreferenceHelper.getInstance(context) }

    var cacheDuration by remember { mutableStateOf(prefs.getUserSetCacheDuration() ?: "900") }
    var selectedTheme by remember {
        mutableStateOf(
            prefs.getSelectedThemePref() ?: context.getString(R.string.follow_system_value)
        )
    }
    var selectedUnit by remember {
        mutableStateOf(
            prefs.getSelectedTemperatureUnit() ?: context.getString(R.string.temp_unit_celsius)
        )
    }

    SettingsScreenContent(
        cacheDuration = cacheDuration,
        selectedTheme = selectedTheme,
        selectedUnit = selectedUnit,
        onCacheChanged = {
            cacheDuration = it
            prefs.saveCacheDuration(it)
        },
        onThemeChanged = {
            selectedTheme = it
            prefs.saveThemePref(it)
            applyTheme(it, context)
        },
        onUnitChanged = {
            selectedUnit = it
            prefs.saveTemperatureUnit(it)
        }
    )
}

@Composable
private fun SettingsScreenContent(
    cacheDuration: String,
    selectedTheme: String,
    selectedUnit: String,
    onCacheChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showCacheDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cache Settings
            item {
                SettingsCategory(
                    title = stringResource(R.string.cache),
                    icon = Icons.Default.Cached
                )
                SettingsItem(
                    title = stringResource(R.string.cache_string),
                    summary = cacheDuration,
                    onClick = { showCacheDialog = true }
                )
                HorizontalDivider()
            }

            // Theme Settings
            item {
                SettingsCategory(
                    title = stringResource(R.string.preference_theme_title),
                    icon = Icons.Default.Palette
                )
                SettingsItem(
                    summary = selectedTheme,
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider()
            }

            // Temperature Unit Settings
            item {
                SettingsCategory(
                    title = stringResource(R.string.preference_temperature_unit_title),
                    icon = Icons.Default.DeviceThermostat
                )
                SettingsItem(
                    summary = selectedUnit,
                    onClick = { showUnitDialog = true }
                )
            }

            // Rate App
            item {
                SettingsItem(
                    title = stringResource(R.string.rate_app),
                    summary = null,
                    icon = Icons.Default.Star,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=com.mayokunadeniyi.instantweather".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }

        // Background Scrim (Shadow on the rest of the screen)
        if (showThemeDialog || showUnitDialog || showCacheDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { }
            )
        }

        // Cache Dialog
        if (showCacheDialog) {
            var tempCache by remember { mutableStateOf(cacheDuration) }
            AlertDialog(
                onDismissRequest = { showCacheDialog = false },
                containerColor = MaterialTheme.colorScheme.background,
                textContentColor = MaterialTheme.colorScheme.onBackground,
                title = { Text(stringResource(R.string.cache_string)) },
                text = {
                    OutlinedTextField(
                        value = tempCache,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempCache = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onCacheChanged(tempCache)
                        showCacheDialog = false
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCacheDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Theme Dialog
        if (showThemeDialog) {
            val themeOptions = listOf(
                Pair(
                    stringResource(R.string.light_theme_name),
                    stringResource(R.string.light_theme_value)
                ),
                Pair(
                    stringResource(R.string.dark_theme_name),
                    stringResource(R.string.dark_theme_value)
                ),
                Pair(
                    stringResource(R.string.auto_battery_name),
                    stringResource(R.string.auto_battery_value)
                ),
                Pair(
                    stringResource(R.string.follow_system_name),
                    stringResource(R.string.follow_system_value)
                )
            )
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                containerColor = MaterialTheme.colorScheme.background,
                textContentColor = MaterialTheme.colorScheme.onBackground,
                text = {
                    Column {
                        themeOptions.forEach { (name, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onThemeChanged(value)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedTheme == value, onClick = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Unit Dialog
        if (showUnitDialog) {
            val unitOptions = listOf(
                stringResource(R.string.temp_unit_celsius),
                stringResource(R.string.temp_unit_fahrenheit)
            )
            AlertDialog(
                onDismissRequest = { showUnitDialog = false },
                containerColor = MaterialTheme.colorScheme.background,
                textContentColor = MaterialTheme.colorScheme.onBackground,
                text = {
                    Column {
                        unitOptions.forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUnitChanged(unit)
                                        showUnitDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedUnit == unit, onClick = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(unit)
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnitDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
private fun SettingsCategory(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SettingsItem(
    summary: String?,
    title: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(Modifier.size(24.dp)) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.width(16.dp))
        } else {
            Spacer(Modifier.width(40.dp))
        }
        Column {
            title?.let { Text(text = title, fontSize = 16.sp) }
            summary?.let {
                Text(
                    text = summary,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun applyTheme(themeValue: String, context: android.content.Context) {
    val mode = when (themeValue) {
        context.getString(R.string.light_theme_value) -> AppCompatDelegate.MODE_NIGHT_NO
        context.getString(R.string.dark_theme_value) -> AppCompatDelegate.MODE_NIGHT_YES
        context.getString(R.string.auto_battery_value) -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        context.getString(R.string.follow_system_value) -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    InstantWeatherTheme {
        SettingsScreenContent(
            cacheDuration = "900",
            selectedTheme = "follow_system",
            selectedUnit = "Celsius/C",
            onCacheChanged = {},
            onThemeChanged = {},
            onUnitChanged = {}
        )
    }
}
