package com.alertgia.app.domain.model

enum class AnalysisMode(val displayName: String) {
    AUTO("Auto"),
    ONLINE("Online (Claude)"),
    OFFLINE("Offline (On-device)")
}
