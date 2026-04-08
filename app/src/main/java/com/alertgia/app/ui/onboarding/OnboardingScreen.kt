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
import com.alertgia.app.ui.theme.NavyDeep
import com.alertgia.app.ui.theme.NavyMid

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

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Bienvenido a Alertg")
                        }
                        withStyle(SpanStyle(color = AlertgiaGreen, fontWeight = FontWeight.Bold)) {
                            append("IA")
                        }
                    },
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tu asistente de seguridad alimentaria con inteligencia artificial",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    title = "Detección en tiempo real",
                    description = "Escanea platos y menús para detectar alérgenos al instante"
                )
                FeatureRow(
                    icon = Icons.Default.SmartToy,
                    title = "IA avanzada",
                    description = "Modelos de visión artificial entrenados con más de 344 categorías de alimentos"
                )
                FeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Privacidad primero",
                    description = "Funciona offline. Tus datos nunca salen de tu dispositivo sin tu permiso"
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AlertgiaGreen)
            ) {
                Text("Continuar", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AlertgiaGreen,
            modifier = Modifier.size(28.dp)
        )
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(description, color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun RgpdPage(onNext: () -> Unit, onDecline: () -> Unit) {
    var privacyAccepted by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDeep)
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = AlertgiaGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "  Privacidad y consentimiento",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RGPD · Reglamento General de Protección de Datos",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

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

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = privacyAccepted,
                        onCheckedChange = { privacyAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = AlertgiaGreen)
                    )
                    Text(
                        text = "He leído y acepto la Política de Privacidad y el tratamiento de mis datos conforme al RGPD",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = AlertgiaGreen)
                    )
                    Text(
                        text = "Acepto los Términos y Condiciones de uso de AlertgIA",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNext,
                    enabled = privacyAccepted && termsAccepted,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertgiaGreen)
                ) {
                    Text("Acepto y continuar", modifier = Modifier.padding(vertical = 4.dp))
                }
                TextButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                    Text("No acepto (salir)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RgpdSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AiDisclaimerPage(onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyMid)
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = AlertgiaGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "  Aviso sobre Inteligencia Artificial",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "EU AI Act · Reglamento Europeo de Inteligencia Artificial",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertgiaGreen.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sistema de IA de apoyo a la decisión",
                            fontWeight = FontWeight.Bold,
                            color = AlertgiaGreen,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AlertgIA utiliza sistemas de inteligencia artificial para detectar alérgenos en alimentos. Este sistema está clasificado como IA de alto riesgo conforme al Reglamento (UE) 2024/1689 (EU AI Act) por su impacto potencial en la salud.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                AiDisclaimerSection(
                    title = "Limitaciones del sistema",
                    content = "• La detección de alérgenos mediante IA NO es 100% precisa\n• El modelo puede cometer errores, especialmente con alimentos procesados, mezclas o presentaciones inusuales\n• La precisión actual del modelo es superior al 90% en condiciones estándar\n• Pueden existir falsos negativos (no detectar un alérgeno presente)"
                )

                AiDisclaimerSection(
                    title = "Uso responsable",
                    content = "• AlertgIA es una herramienta de APOYO, no un sustituto del criterio médico\n• Siempre consulta los ingredientes directamente con el establecimiento\n• En caso de alergia grave (anafilaxia), lleva siempre tu medicación de emergencia\n• No tomes decisiones médicas basadas únicamente en esta aplicación"
                )

                AiDisclaimerSection(
                    title = "Responsabilidad",
                    content = "AlertgIA proporciona información orientativa. La empresa no se responsabiliza de las consecuencias derivadas de decisiones tomadas exclusivamente a partir de los resultados de la aplicación. El usuario asume la responsabilidad de verificar la información."
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Si tienes una alergia grave, consulta siempre con el personal del establecimiento y lleva tu autoinyector de epinefrina.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertgiaGreen)
                ) {
                    Text("Entendido, comenzar", modifier = Modifier.padding(vertical = 4.dp))
                }
                TextButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                    Text("No acepto (salir)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AiDisclaimerSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
