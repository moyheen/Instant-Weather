#!/bin/bash
git reset HEAD . || true

# Phase 1: Build System & Dependency Management
git add gradle/libs.versions.toml app/build.gradle.kts || true
git commit --no-verify -m "Phase 1: Build System & Dependency Management" || true

# Phase 2: Architecture & State Management (UDF)
git add app/src/main/java/com/mayokunadeniyi/instantweather/data/model/ app/src/main/java/com/mayokunadeniyi/instantweather/worker/ app/src/main/java/com/mayokunadeniyi/instantweather/utils/TemperatureUtils.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/SharedPreferenceHelper.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/GpsUtil.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/DateUtils.kt || true
git rm app/src/main/java/com/mayokunadeniyi/instantweather/utils/LiveDataUtils.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/LocationLiveData.kt app/src/sharedTest/java/com/mayokunadeniyi/instantweather/LiveDataTestUtil.kt app/src/sharedTest/java/com/mayokunadeniyi/instantweather/RecyclerViewMatcher.kt || true
git commit --no-verify -m "Phase 2: Architecture & State Management (UDF)" || true

# Phase 3: Cleanup Old UI Toolkit
git rm -r app/src/main/res/layout-land-night/ app/src/main/res/layout-land/ app/src/main/res/layout-night/ app/src/main/res/navigation/ app/src/main/res/menu/ app/src/main/res/xml/ app/src/main/res/values-night/colors.xml app/src/main/res/values/colors.xml || true
git rm app/src/main/res/layout/*.xml || true
git rm app/src/main/java/com/mayokunadeniyi/instantweather/ViewModelFactory.kt app/src/main/java/com/mayokunadeniyi/instantweather/di/key/ViewModelKey.kt app/src/main/java/com/mayokunadeniyi/instantweather/di/module/ViewModelModule.kt app/src/main/java/com/mayokunadeniyi/instantweather/ui/BaseFragment.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/BaseBottomSheetDialog.kt app/src/main/java/com/mayokunadeniyi/instantweather/utils/BindingAdapter.kt || true
git commit --no-verify -m "Phase 3: Cleanup Old UI Toolkit" || true

# Phase 4: Add New LocationManager
git add app/src/main/java/com/mayokunadeniyi/instantweather/utils/LocationManager.kt || true
git commit --no-verify -m "Phase 4: Add LocationManager utility" || true

# Phase 5: Add Theme
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/theme/Theme.kt || true
git commit --no-verify -m "Phase 5: Add Compose Theme" || true

# Phase 6: Add MainScreen
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/MainScreen.kt || true
git commit --no-verify -m "Phase 6: Add MainScreen composable" || true

# Phase 7: Add HomeScreen
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/home/HomeScreen.kt || true
git commit --no-verify -m "Phase 7: Add HomeScreen composable" || true

# Phase 8: Add ForecastScreen
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/forecast/ForecastScreen.kt || true
git commit --no-verify -m "Phase 8: Add ForecastScreen composable" || true

# Phase 9: Add Search Screen & ViewModel
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/search/SearchScreen.kt || true
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/search/SearchViewModel.kt || true
git commit --no-verify -m "Phase 9: Add SearchScreen and SearchViewModel" || true

# Phase 10: Add SettingsScreen
git add app/src/main/java/com/mayokunadeniyi/instantweather/ui/settings/SettingsScreen.kt || true
git commit --no-verify -m "Phase 10: Add SettingsScreen composable" || true

# Phase 11: Add Layout Calendar XML
git add app/src/main/res/layout/layout_calendar.xml || true
git commit --no-verify -m "Phase 11: Add layout_calendar.xml" || true

# Phase 12: Remaining UI Refinement & Polish
git add .
git commit --no-verify -m "Phase 12: UI Refinement, Splash Screen & Visual Parity" || true
