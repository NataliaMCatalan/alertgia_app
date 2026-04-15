package com.alertgia.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.res.painterResource
import com.alertgia.app.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.BrandGreen
import com.alertgia.app.ui.theme.SurfaceBg
import com.alertgia.app.ui.theme.SurfaceCard
import com.alertgia.app.ui.theme.SurfaceSubtle
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary


@Composable
fun OnboardingScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    when (currentPage) {
        0 -> WelcomePage(onNext = { currentPage = 1 })
        1 -> RgpdPage(onNext = { currentPage = 2 }, onDecline = onDecline)
        2 -> AiDisclaimerPage(onAccept = onAccept, onDecline = onDecline)
    }
}

// ── Página 1: Bienvenida ────────────────────────────────────────────────────

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App logo — PNG con su propio marco, sin fondo extra
            Icon(
                painter = painterResource(R.drawable.ic_alertgia_logo),
                contentDescription = "AlertgIA",
                tint = Color.Unspecified,
                modifier = Modifier.size(160.dp)
            )

            // Title + subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                            append("Bienvenido a Alertg")
                        }
                        withStyle(SpanStyle(color = AlertgiaGreen, fontWeight = FontWeight.Bold)) {
                            append("IA")
                        }
                    },
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tu asistente de seguridad alimentaria\ncon inteligencia artificial",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Feature list
            Column(
                modifier = Modifier
                    .background(SurfaceCard, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FeatureRow(
                    icon  = Icons.Default.CheckCircle,
                    title = "Detección en tiempo real",
                    desc  = "Escanea platos y menús para detectar alérgenos al instante"
                )
                FeatureRow(
                    icon  = Icons.Default.SmartToy,
                    title = "IA avanzada",
                    desc  = "Modelos de visión entrenados con más de 344 categorías de alimentos"
                )
                FeatureRow(
                    icon  = Icons.Default.Lock,
                    title = "Privacidad primero",
                    desc  = "Funciona offline. Tus datos nunca salen del dispositivo sin tu permiso"
                )
            }

            // CTA button
            Button(
                onClick   = onNext,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertgiaGreen)
            ) {
                Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            // Google for Startups badge
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("G", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = Color(0xFF4285F4))
                Text(
                    buildAnnotatedString {
                        append("Ganadora ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Google for Startups")
                        }
                        append(" 2024 · Creatividad, Innovación e Impacto Social")
                    },
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = AlertgiaGreen, modifier = Modifier.size(26.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

// ── Página 2: RGPD (light mode) ────────────────────────────────────────────

@Composable
private fun RgpdPage(onNext: () -> Unit, onDecline: () -> Unit) {
    var privacyAccepted by remember { mutableStateOf(false) }
    var termsAccepted   by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBg) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(24.dp))
                        Text(
                            text = "  Privacidad y consentimiento",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RGPD · Reglamento General de Protección de Datos",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RgpdSection(
                    title = "¿Qué datos recopilamos?",
                    content = "AlertgIA recopila y almacena localmente en tu dispositivo:\n• Tu perfil de alergias e intolerancias alimentarias\n• Preferencias de configuración\n\nNingún dato personal se envía a servidores externos sin tu consentimiento explícito."
                )
                RgpdSection(
                    title = "Análisis con IA en la nube (opcional)",
                    content = "Si activas el modo de análisis online, las imágenes que captures se envían de forma encriptada a la API de Anthropic (Claude) para su análisis. Las imágenes no se almacenan ni se usan para entrenar modelos. Puedes usar AlertgIA en modo offline sin enviar ningún dato."
                )
                RgpdSection(
                    title = "Tus derechos",
                    content = "Conforme al RGPD tienes derecho a:\n• Acceder a tus datos\n• Rectificarlos o suprimirlos\n• Portabilidad de datos\n• Oposición al tratamiento\n\nPuedes ejercer estos derechos en: privacidad@alertgia.com"
                )
                RgpdSection(
                    title = "Responsable del tratamiento",
                    content = "AlertgIA S.L. · CIF: B-XXXXXXXX\nDirección: España\nContacto DPD: privacidad@alertgia.com"
                )

                HorizontalDivider()

                CheckRow(
                    checked  = privacyAccepted,
                    onChange = { privacyAccepted = it },
                    label    = "He leído y acepto la Política de Privacidad y el tratamiento de mis datos conforme al RGPD"
                )
                CheckRow(
                    checked  = termsAccepted,
                    onChange = { termsAccepted = it },
                    label    = "Acepto los Términos y Condiciones de uso de AlertgIA"
                )
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onNext,
                    enabled  = privacyAccepted && termsAccepted,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(50.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Acepto y continuar", fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                    Text("No acepto (salir)", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CheckRow(checked: Boolean, onChange: (Boolean) -> Unit, label: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Checkbox(
            checked  = checked,
            onCheckedChange = onChange,
            colors   = CheckboxDefaults.colors(checkedColor = BrandGreen)
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun RgpdSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(text = content, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
        }
    }
}

// ── Página 3: Aviso IA (light mode) ────────────────────────────────────────

@Composable
private fun AiDisclaimerPage(onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBg) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(24.dp))
                        Text(
                            text = "  Aviso sobre Inteligencia Artificial",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "EU AI Act · Reglamento Europeo de Inteligencia Artificial",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Green highlight box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Sistema de IA de apoyo a la decisión",
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AlertgIA utiliza sistemas de inteligencia artificial para detectar alérgenos en alimentos. Este sistema está clasificado como IA de alto riesgo conforme al Reglamento (UE) 2024/1689 (EU AI Act) por su impacto potencial en la salud.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                AiSection(
                    title   = "Limitaciones del sistema",
                    content = "• La detección de alérgenos mediante IA NO es 100% precisa\n• El modelo puede cometer errores con alimentos procesados o presentaciones inusuales\n• La precisión actual es superior al 90% en condiciones estándar\n• Pueden existir falsos negativos (no detectar un alérgeno presente)"
                )
                AiSection(
                    title   = "Uso responsable",
                    content = "• AlertgIA es una herramienta de APOYO, no un sustituto del criterio médico\n• Siempre consulta los ingredientes directamente con el establecimiento\n• En caso de alergia grave, lleva siempre tu medicación de emergencia\n• No tomes decisiones médicas basadas únicamente en esta aplicación"
                )
                AiSection(
                    title   = "Responsabilidad",
                    content = "AlertgIA proporciona información orientativa. La empresa no se responsabiliza de las consecuencias derivadas de decisiones tomadas exclusivamente a partir de los resultados de la aplicación."
                )

                // Warning box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "⚠ Si tienes una alergia grave, consulta siempre con el personal del establecimiento y lleva tu autoinyector de epinefrina.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB71C1C),
                        lineHeight = 20.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onAccept,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(50.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Entendido, comenzar", fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                    Text("No acepto (salir)", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AiSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(text = content, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
        }
    }
}
