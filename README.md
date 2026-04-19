![](media/instant_weather_github.png)
# Instant Weather :partly_sunny:

![Android Build](https://github.com/mayokunthefirst/Instant-Weather/workflows/Android%20Build/badge.svg) ![Android Weekly](https://androidweekly.net/issues/issue-413/badge) [![BCH compliance](https://bettercodehub.com/edge/badge/mayokunthefirst/Instant-Weather?branch=master)](https://bettercodehub.com/) ![My twitter](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Ftwitter.com%2Fmayokunadeniyi) ![Shield](https://img.shields.io/badge/contributions-welcome-brightgreen)

An Android weather application modernized using **Jetpack Compose**, **Material 3**, and the **MVVM pattern** with **Unidirectional Data Flow (UDF)**. Instant Weather fetches data from the [OpenWeatherMap API](https://openweathermap.org/api) to provide real-time weather information, managed reactively with **Kotlin Coroutines and Flows**. It also integrates **Algolia Instant Search** with **Paging 3** for seamless location searching.

<a href='https://play.google.com/store/apps/details?id=com.mayokunadeniyi.instantweather&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="280"/></a>

## Architecture
The application follows modern Android development practices:
* **Single-Activity Architecture**: Built entirely with [Jetpack Compose](https://developer.android.com/jetpack/compose).
* **MVVM + UDF**: Uses `UiState` and `UiEvent` patterns for predictable state management.
* **Navigation Compose**: Type-safe navigation between screens.
* **Reactive Data Layer**: Fully powered by Kotlin Coroutines and StateFlow.

<p align="center"><a><img src="https://raw.githubusercontent.com/mayokunthefirst/Instant-Weather/master/media/final-architecture.png" width="700"></a></p>

## Technologies Used:

* [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android's modern toolkit for building native UI.
* [Material 3](https://m3.material.io/) - The latest version of Google's open-source design system.
* [Dagger Hilt](https://dagger.dev/hilt/) - For dependency injection.
* [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
* [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) - For asynchronous programming and reactive data streams.
* [Room](https://developer.android.com/topic/libraries/architecture/room) - A persistence library providing an abstraction layer over SQLite.
* [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) - For navigating between composables while preserving type safety.
* [Algolia InstantSearch](https://www.algolia.com/doc/guides/building-search-ui/getting-started/android-compose/) - Seamless search integration with Compose support.
* [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data) - For loading and displaying pages of data from the network and database.
* [Work Manager](https://developer.android.com/topic/libraries/architecture/workmanager) - To manage background jobs like weather updates.
* [Timber](https://github.com/JakeWharton/timber) - Extensible logging utility.
* [AndroidX Splash Screen](https://developer.android.com/develop/ui/views/launch/splash-screen) - Official API for splash screens across all Android versions.
* [Gradle Version Catalog](https://docs.gradle.org/current/userguide/platforms.html) - Centralized dependency management.

## Installation
Instant Weather requires a minimum API level of 21. Clone the repository. You will need an API key i.e. `API_KEY` from [Open Weather](https://openweathermap.org/) to request data. If you don’t already have an account, you will need to create one in order to request an API Key. Also, you will need to create an app on [Algolia](https://www.algolia.com/doc/). 

Generate a search only API key i.e. `ALGOLIA_API_KEY`, the APP ID i.e. `ALGOLIA_APP_ID` for the app you created and then create an [Index](https://www.algolia.com/doc/faq/basics/what-is-an-index/) under that app, you will need the index name i.e. `ALGOLIA_INDEX_NAME` to setup the search functionality in this application. You can populate the index with records from [here](https://drive.google.com/file/d/1o-btuAm1bxAwKzd41DP8-1mToTc1QQz-/view?usp=sharing). 

Each record follows this structure:

````JSON
{
    "objectID": "ffe74e4cdddbc_dashboard_generated_id",
    "country": "Zimbabwe",
    "geonameid": 1085510,
    "name": "Epworth",
    "subcountry": "Harare"
}
  
````

In your project's root directory, inside the `local.properties` file (create one if unavailable) include the following lines:

````properties
API_KEY = "YOUR_API_KEY"
ALGOLIA_API_KEY = "YOUR_API_KEY"
ALGOLIA_APP_ID = "YOUR_APP_ID"
ALGOLIA_INDEX_NAME = "YOUR_INDEX_NAME"

````
## Contribution
All contributions are welcome. If you are interested in seeing a particular feature implemented in this app, please open a new issue after which you can make a PR!

![Alt](https://repobeats.axiom.co/api/embed/84dfd3cd94832805dbcaa3569ec855d19e5c9401.svg "Repobeats analytics image")

## LICENSE
```
MIT License

Copyright (c) 2020 Mayokun Adeniyi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
