package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dao: CameraSettingsDao) {
    val settingsFlow: Flow<CameraSettings> = dao.getSettingsFlow().map { it ?: CameraSettings() }

    suspend fun getSettings(): CameraSettings {
        return dao.getSettingsDirect() ?: CameraSettings()
    }

    suspend fun updateSettings(settings: CameraSettings) {
        dao.insertOrUpdate(settings)
    }
}
