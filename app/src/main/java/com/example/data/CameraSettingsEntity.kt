package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camera_settings")
data class CameraSettings(
    @PrimaryKey val id: Int = 1,
    val fps: Int = 24,
    val resolution: String = "1085p", // Let's use 1080p but default it to standard "1080p"
    val resolutionValue: String = "1080p", 
    val colorProfile: String = "REC709", // "REC709" or "LOG"
    val gridVisible: Boolean = true,
    val zebraPatternEnabled: Boolean = false,
    val focusPeakingEnabled: Boolean = false,
    val iso: Int = 400,
    val ev: Float = 0.0f,
    val manualFocus: Float = 0.5f,
    val aeAfLocked: Boolean = false,
    val levelerEnabled: Boolean = true
)
