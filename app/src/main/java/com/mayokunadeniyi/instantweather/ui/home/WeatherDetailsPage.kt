package com.mayokunadeniyi.instantweather.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator

@Preview
@Composable
fun WeatherDetailsPage() {
    val height8 = Modifier.height(8.dp)
    val height16 = Modifier.height(16.dp)
    val context = LocalContext.current
    val tempValueResId = if (SharedPreferenceHelper.getInstance(context)
            .getSelectedTemperatureUnit() == context.getString(R.string.temp_unit_fahrenheit)
    )
        R.string.temp_value_fahrenheit
    else
        R.string.temp_value_celsius

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = height16)
            Text(
                text = "Shepherds Bush",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = height8)
            Text(
                text = "Monday Nov 7, 02:57 AM",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = height8)
            AndroidView(::WeatherIconView) { iconView ->
                iconView.setIconSize(100)
                iconView.setIconColor(R.color.primaryLightColor)
                WeatherIconGenerator.getIconResources(context, iconView, "Drizzle")
            }
            Spacer(modifier = height16)
            Text(
                text = stringResource(tempValueResId, 36.3),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = height16)
            Text(
                text = "Drizzle",
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = height16)
            Row(
                modifier = Modifier.width(25.dp)
            ) {
                Divider(color = Color(0xFF808080))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            RowItem(
                image = painterResource(R.drawable.ic_humidity),
                description = stringResource(id = R.string.humidity),
                value = stringResource(id = R.string.humidity_value, 92.0)
            )
            RowItem(
                image = painterResource(R.drawable.ic_pressure),
                description = stringResource(id = R.string.pressure),
                value = stringResource(id = R.string.pressure_value, 1006.0)
            )
            RowItem(
                image = painterResource(R.drawable.ic_wind),
                description = stringResource(id = R.string.wind_speed),
                value = stringResource(id = R.string.wind_speed_value, 3.09)
            )
        }
    }
}

@Composable
fun RowItem(image: Painter, description: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = image,
            contentDescription = description,
            modifier = Modifier.height(30.dp),
            colorFilter = ColorFilter.tint(colorResource(id = R.color.primaryLightColor))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}