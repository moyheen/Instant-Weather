package com.mayokunadeniyi.instantweather.data.model

import kotlinx.serialization.Serializable

/**
 * Created by Mayokun Adeniyi on 28/04/2020.
 */

@Serializable
data class SearchResult(
    val name: String,
    val country: String,
    val subcountry: String
)
