package com.example.farmeasy.feature.farm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.Crop
import com.example.farmeasy.domain.model.Farm
import com.example.farmeasy.domain.usecase.ManageFarmUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FarmUiState(
    val farms: List<Farm> = emptyList(),
    val selectedFarm: Farm? = null,
    val cropsForSelectedFarm: List<Crop> = emptyList(),
    // Form fields for Add/Edit
    val farmNameInput: String = "",
    val farmAreaInput: String = "2.5",
    val soilTypeInput: String = "Medium Black Soil",
    val locationNameInput: String = "Plot 1",
    val cropNameInput: String = "Soybean",
    val cropVarietyInput: String = "JS 335",
    val cropStageInput: String = "Vegetative",
    val cropNotesInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val farmToDelete: Farm? = null,
    val cropToDelete: Crop? = null
)

sealed interface FarmUiEvent {
    data class FarmSelected(val farm: Farm) : FarmUiEvent
    data class FarmNameChanged(val name: String) : FarmUiEvent
    data class FarmAreaChanged(val area: String) : FarmUiEvent
    data class SoilTypeChanged(val type: String) : FarmUiEvent
    data class LocationChanged(val location: String) : FarmUiEvent
    object SaveFarm : FarmUiEvent
    data class ConfirmDeleteFarm(val farm: Farm) : FarmUiEvent
    object DismissDeleteFarm : FarmUiEvent
    object DeleteFarmConfirmed : FarmUiEvent

    data class CropNameChanged(val name: String) : FarmUiEvent
    data class CropVarietyChanged(val variety: String) : FarmUiEvent
    data class CropStageChanged(val stage: String) : FarmUiEvent
    data class CropNotesChanged(val notes: String) : FarmUiEvent
    object SaveCrop : FarmUiEvent
    data class ConfirmDeleteCrop(val crop: Crop) : FarmUiEvent
    object DismissDeleteCrop : FarmUiEvent
    object DeleteCropConfirmed : FarmUiEvent
    object DismissError : FarmUiEvent
}

class FarmViewModel(
    private val manageFarmUseCase: ManageFarmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    init {
        loadFarms()
    }

    private fun loadFarms() {
        viewModelScope.launch {
            manageFarmUseCase.getFarms().collect { farms ->
                _uiState.update {
                    it.copy(
                        farms = farms,
                        selectedFarm = it.selectedFarm ?: farms.firstOrNull()
                    )
                }
                _uiState.value.selectedFarm?.let { loadCropsForFarm(it.id) }
            }
        }
    }

    private fun loadCropsForFarm(farmId: String) {
        viewModelScope.launch {
            manageFarmUseCase.getCrops(farmId).collect { crops ->
                _uiState.update { it.copy(cropsForSelectedFarm = crops) }
            }
        }
    }

    fun onEvent(event: FarmUiEvent) {
        when (event) {
            is FarmUiEvent.FarmSelected -> {
                _uiState.update { it.copy(selectedFarm = event.farm) }
                loadCropsForFarm(event.farm.id)
            }
            is FarmUiEvent.FarmNameChanged -> _uiState.update { it.copy(farmNameInput = event.name) }
            is FarmUiEvent.FarmAreaChanged -> _uiState.update { it.copy(farmAreaInput = event.area) }
            is FarmUiEvent.SoilTypeChanged -> _uiState.update { it.copy(soilTypeInput = event.type) }
            is FarmUiEvent.LocationChanged -> _uiState.update { it.copy(locationNameInput = event.location) }

            is FarmUiEvent.SaveFarm -> saveFarm()
            is FarmUiEvent.ConfirmDeleteFarm -> _uiState.update { it.copy(farmToDelete = event.farm) }
            is FarmUiEvent.DismissDeleteFarm -> _uiState.update { it.copy(farmToDelete = null) }
            is FarmUiEvent.DeleteFarmConfirmed -> deleteFarm()

            is FarmUiEvent.CropNameChanged -> _uiState.update { it.copy(cropNameInput = event.name) }
            is FarmUiEvent.CropVarietyChanged -> _uiState.update { it.copy(cropVarietyInput = event.variety) }
            is FarmUiEvent.CropStageChanged -> _uiState.update { it.copy(cropStageInput = event.stage) }
            is FarmUiEvent.CropNotesChanged -> _uiState.update { it.copy(cropNotesInput = event.notes) }
            is FarmUiEvent.SaveCrop -> saveCrop()
            is FarmUiEvent.ConfirmDeleteCrop -> _uiState.update { it.copy(cropToDelete = event.crop) }
            is FarmUiEvent.DismissDeleteCrop -> _uiState.update { it.copy(cropToDelete = null) }
            is FarmUiEvent.DeleteCropConfirmed -> deleteCrop()

            is FarmUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun saveFarm() {
        val s = _uiState.value
        val area = s.farmAreaInput.toDoubleOrNull()
        if (s.farmNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Farm name cannot be empty.") }
            return
        }
        if (area == null || area <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Acreage must be greater than zero.") }
            return
        }

        val farm = Farm(
            name = s.farmNameInput.trim(),
            areaInAcres = area,
            soilType = s.soilTypeInput,
            locationName = s.locationNameInput.ifBlank { "Parcel" }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = manageFarmUseCase.saveFarm(farm)
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        farmNameInput = "",
                        selectedFarm = farm
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message) }
            }
        }
    }

    private fun deleteFarm() {
        val farm = _uiState.value.farmToDelete ?: return
        viewModelScope.launch {
            manageFarmUseCase.deleteFarm(farm.id)
            _uiState.update { it.copy(farmToDelete = null, selectedFarm = null) }
        }
    }

    private fun saveCrop() {
        val s = _uiState.value
        val currentFarm = s.selectedFarm ?: return
        if (s.cropNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Crop name is required.") }
            return
        }

        val crop = Crop(
            farmId = currentFarm.id,
            cropName = s.cropNameInput.trim(),
            variety = s.cropVarietyInput.trim(),
            growthStage = s.cropStageInput,
            notes = s.cropNotesInput.trim()
        )

        viewModelScope.launch {
            manageFarmUseCase.saveCrop(crop)
            _uiState.update { it.copy(cropNameInput = "", cropVarietyInput = "", cropNotesInput = "") }
        }
    }

    private fun deleteCrop() {
        val crop = _uiState.value.cropToDelete ?: return
        viewModelScope.launch {
            manageFarmUseCase.deleteCrop(crop.id)
            _uiState.update { it.copy(cropToDelete = null) }
        }
    }
}
