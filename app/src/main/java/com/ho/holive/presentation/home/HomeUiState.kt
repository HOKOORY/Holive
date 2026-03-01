package com.ho.holive.presentation.home

import com.ho.holive.domain.model.LivePlatform

data class HomeUiState(
    val query: String = "",
    val platforms: List<LivePlatform> = emptyList(),
    val selectedPlatformAddress: String? = null,
    val isLoadingPlatforms: Boolean = false,
    val isRefreshingRooms: Boolean = false,
    val errorMessage: String? = null,
    val networkConnected: Boolean = true,
)
