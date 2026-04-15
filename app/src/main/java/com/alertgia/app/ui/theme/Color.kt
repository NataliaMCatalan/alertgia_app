package com.alertgia.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── AlertgIA Brand ─────────────────────────────────────────────────────────
val BrandGreen      = Color(0xFF7DB366)   // leaf green — alertgia.app
val BrandGreenLight = Color(0xFFEEF7EA)   // tint for containers / indicators
val BrandGreenDark  = Color(0xFF5A8C49)   // pressed / active state
val BrandGreenGlow  = Color(0x267DB366)   // 15 % opacity glow

// ── Light Mode Surfaces ─────────────────────────────────────────────────────
val SurfaceBg     = Color(0xFFF8F9FA)   // off-white app background
val SurfaceCard   = Color(0xFFFFFFFF)   // white cards / nav bar
val SurfaceSubtle = Color(0xFFF0F4F8)   // grey-blue chips & secondary containers
val BorderLight   = Color(0xFFE2E8F0)   // 1 dp subtle border
val BorderMedium  = Color(0xFFCBD5E1)   // slightly stronger border

// ── Text ───────────────────────────────────────────────────────────────────
val TextPrimary   = Color(0xFF1A2332)   // dark navy — headings
val TextSecondary = Color(0xFF64748B)   // slate grey — captions / labels
val TextDisabled  = Color(0xFFCBD5E1)   // disabled

// ── Status ─────────────────────────────────────────────────────────────────
val StatusSafe    = Color(0xFF4CAF50)
val StatusWarning = Color(0xFFF59E0B)
val StatusDanger  = Color(0xFFEF4444)

val StatusSafeBg    = Color(0xFFE8F5E9)
val StatusWarningBg = Color(0xFFFFF8E1)
val StatusDangerBg  = Color(0xFFFFEBEE)

// ── Avatar palette ──────────────────────────────────────────────────────────
val AvatarColors = listOf(
    0xFF2563EBL,
    0xFF7C3AEDL,
    0xFFDB2777L,
    0xFFEA580CL,
    0xFF0D9488L,
    0xFF65A30DL,
    0xFFDC2626L,
    0xFF0284C7L,
    0xFF7E22CEL,
    0xFF92400EL,
)

// ── Legacy aliases — all existing component references kept compiling ────────
val AlertgiaGreen      = BrandGreen
val AlertgiaGreenLight = BrandGreenLight
val AlertgiaGreenDark  = BrandGreenDark
val AlertgiaGreenGlow  = BrandGreenGlow
val NavyDeep    = SurfaceBg
val NavyMid     = SurfaceCard
val NavyLight   = SurfaceSubtle
val NavyBorder  = BorderLight
val NavyGlass   = Color(0x0A000000)   // 4 % black
val SafeGreen    = StatusSafe
val WarningAmber = StatusWarning
val DangerRed    = StatusDanger
val SafeGlow    = StatusSafeBg
val WarningGlow = StatusWarningBg
val DangerGlow  = StatusDangerBg
