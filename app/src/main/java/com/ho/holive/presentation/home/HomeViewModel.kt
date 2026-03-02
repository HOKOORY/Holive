package com.ho.holive.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ho.holive.core.common.AppResult
import com.ho.holive.core.network.ConnectivityObserver
import com.ho.holive.domain.model.LivePlatform
import com.ho.holive.domain.usecase.GetPlatformsUseCase
import com.ho.holive.domain.usecase.ObserveRoomsUseCase
import com.ho.holive.domain.usecase.RefreshRoomsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeRoomsUseCase: ObserveRoomsUseCase,
    private val getPlatformsUseCase: GetPlatformsUseCase,
    private val refreshRoomsUseCase: RefreshRoomsUseCase,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val queryState = MutableStateFlow("")
    private var roomsRefreshJob: Job? = null
    private var platformsLoadJob: Job? = null

    val pagedRooms = queryState
        .debounce(350)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            observeRoomsUseCase(query)
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            connectivityObserver.observe().collect { connected ->
                val previousConnected = _uiState.value.networkConnected
                _uiState.update { it.copy(networkConnected = connected) }
                val reconnected = !previousConnected && connected
                if (reconnected && _uiState.value.platforms.isEmpty()) {
                    loadPlatforms()
                }
            }
        }
        loadPlatforms()
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        queryState.value = newQuery.trim()
    }

    fun onPlatformSelected(address: String) {
        val state = _uiState.value
        if (state.selectedPlatformAddress == address) return

        val selected = state.platforms.firstOrNull { it.address == address } ?: return
        _uiState.update { it.copy(selectedPlatformAddress = address, errorMessage = null) }
        loadRoomsForPlatform(selected)
    }

    fun refresh() {
        val state = _uiState.value
        val selected = state.platforms.firstOrNull { it.address == state.selectedPlatformAddress }
        if (selected != null) {
            loadRoomsForPlatform(selected)
        } else {
            loadPlatforms()
        }
    }

    private fun loadPlatforms() {
        if (platformsLoadJob?.isActive == true) return

        platformsLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlatforms = true, errorMessage = null) }
            when (val result = getPlatformsUseCase()) {
                is AppResult.Success -> {
                    val onlinePlatforms = result.data.filter { it.onlineCount > 0 }
                    val platforms = onlinePlatforms.ifEmpty { result.data }
                    val preferredAddress = _uiState.value.selectedPlatformAddress
                    val selected = platforms.firstOrNull { it.address == preferredAddress } ?: platforms.firstOrNull()

                    _uiState.update {
                        it.copy(
                            platforms = platforms,
                            selectedPlatformAddress = selected?.address,
                            isLoadingPlatforms = false,
                            errorMessage = null,
                        )
                    }

                    if (selected != null) {
                        loadRoomsForPlatform(selected)
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingPlatforms = false,
                            errorMessage = result.message ?: "unknown error",
                        )
                    }
                }

                AppResult.Loading -> Unit
            }
        }
    }

    private fun loadRoomsForPlatform(platform: LivePlatform) {
        roomsRefreshJob?.cancel()
        roomsRefreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingRooms = true, errorMessage = null) }
            when (val result = refreshRoomsUseCase(platform)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isRefreshingRooms = false) }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshingRooms = false,
                            errorMessage = result.message ?: "unknown error",
                        )
                    }
                }

                AppResult.Loading -> Unit
            }
        }
    }
}
