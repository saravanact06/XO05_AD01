package com.thambithappu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFFFFBF2)
private val Ink = Color(0xFF292522)
private val Coral = Color(0xFFFF8A7A)
private val Yellow = Color(0xFFFFD966)
private val Mint = Color(0xFFB8E6D2)
private val Lavender = Color(0xFFD9CCFF)

/**
 * settings/onSettingsChange are hoisted (no more internal `remember`) so that
 * ProtectedDemoScreen - a separate screen - sees the exact same Protection Mode,
 * sensitivity, and protected-app list picked here.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    settings: ProtectionSettings,
    onSettingsChange: (ProtectionSettings) -> Unit,
    onOpenDemo: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {

        DecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 22.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "THAMBI",
                color = Ink,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = "THAPPU!",
                color = Coral,
                fontSize = 37.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Don't peek. It's thambi thappu.",
                color = Ink.copy(alpha = 0.65f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            PhoneIllustration()

            Spacer(modifier = Modifier.height(20.dp))

            // MAIN PROTECTION CARD
            AppCard {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Privacy Protection",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (settings.protectionEnabled)
                                "Watching for shoulder surfers"
                            else
                                "Protection is currently off",
                            fontSize = 13.sp,
                            color = Ink.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = settings.protectionEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(protectionEnabled = it))
                        }
                    )
                }

                if (settings.protectionEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenDemo,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "▶ Open Protected Demo Screen")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PROTECTED APPS
            AppCard {

                SectionTitle(
                    emoji = "📱",
                    title = "Protected Apps"
                )

                Text(
                    text = "Choose where Thambi Thappu should protect you.",
                    color = Ink.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppChoice(
                    name = "WhatsApp",
                    emoji = "💬",
                    selected = settings.protectedApps.contains("WhatsApp")
                ) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "WhatsApp")))
                }

                AppChoice(
                    name = "Banking",
                    emoji = "🏦",
                    selected = settings.protectedApps.contains("Banking")
                ) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Banking")))
                }

                AppChoice(
                    name = "Gallery",
                    emoji = "🖼️",
                    selected = settings.protectedApps.contains("Gallery")
                ) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Gallery")))
                }

                AppChoice(
                    name = "Email",
                    emoji = "✉️",
                    selected = settings.protectedApps.contains("Email")
                ) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Email")))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PROTECTION MODES
            AppCard {

                SectionTitle(
                    emoji = "🛡️",
                    title = "Protection Mode"
                )

                Text(
                    text = "Choose what happens when someone peeks.",
                    color = Ink.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModeRow(
                    title = "Warning only",
                    description = "Show a warning, don't hide anything",
                    selected = settings.mode == ProtectionMode.WARNING_ONLY
                ) {
                    onSettingsChange(settings.copy(mode = ProtectionMode.WARNING_ONLY))
                }

                ModeRow(
                    title = "Blur / Protect",
                    description = "Blur the whole screen, with a warning icon",
                    selected = settings.mode == ProtectionMode.BLUR
                ) {
                    onSettingsChange(settings.copy(mode = ProtectionMode.BLUR))
                }

                ModeRow(
                    title = "Privacy Overlay",
                    description = "Cover the screen entirely (opaque)",
                    selected = settings.mode == ProtectionMode.OVERLAY
                ) {
                    onSettingsChange(settings.copy(mode = ProtectionMode.OVERLAY))
                }

                ModeRow(
                    title = "Force Switch",
                    description = "Instantly jump to the home screen - fastest reaction",
                    selected = settings.mode == ProtectionMode.FORCE_SWITCH
                ) {
                    onSettingsChange(settings.copy(mode = ProtectionMode.FORCE_SWITCH))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SENSITIVITY
            AppCard {

                SectionTitle(
                    emoji = "🎯",
                    title = "Detection Sensitivity"
                )

                Text(
                    text = when {
                        settings.sensitivity < 0.35f -> "Low"
                        settings.sensitivity < 0.7f -> "Medium"
                        else -> "High"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Coral
                )

                Slider(
                    value = settings.sensitivity,
                    onValueChange = {
                        onSettingsChange(settings.copy(sensitivity = it))
                    },
                    valueRange = 0f..1f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Less sensitive",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "More sensitive",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // WARNING ESCALATION
            AppCard {

                SectionTitle(
                    emoji = "🚨",
                    title = "Warning Escalation"
                )

                WarningRow(
                    number = "1",
                    title = "First Peek",
                    description = "Someone may be peeking into your phone."
                )

                WarningRow(
                    number = "2",
                    title = "Second Peek",
                    description = "Stronger warning + protection applied automatically"
                )

                WarningRow(
                    number = "3",
                    title = "Third Peek",
                    description = "Tamil meme / custom warning"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // QUICK SETTINGS
            AppCard {

                SectionTitle(
                    emoji = "⚙️",
                    title = "Quick Settings"
                )

                SettingRow(
                    title = "Warning sound",
                    enabled = settings.soundEnabled
                ) {
                    onSettingsChange(settings.copy(soundEnabled = it))
                }

                SettingRow(
                    title = "Vibration",
                    enabled = settings.vibrationEnabled
                ) {
                    onSettingsChange(settings.copy(vibrationEnabled = it))
                }

                SettingRow(
                    title = "Third-warning meme",
                    enabled = settings.thirdWarningMemeEnabled
                ) {
                    onSettingsChange(settings.copy(thirdWarningMemeEnabled = it))
                }

                // Not wired to real persistence (DataStore/SharedPreferences) yet -
                // settings currently live only in memory for the session.
                SettingRow(
                    title = "Save preferences",
                    enabled = true,
                    onCheckedChange = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FULL SETTINGS BUTTON
            Button(
                onClick = {
                    onSettingsClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {

                Text(
                    text = "Open Full Settings",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your screen. Your privacy.",
                color = Ink.copy(alpha = 0.45f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AppCard(
    content: @Composable () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                Ink,
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(
    emoji: String,
    title: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = emoji,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun AppChoice(
    name: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = emoji,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = if (selected) "✓" else "○",
            color = if (selected)
                Color(0xFF32805B)
            else
                Ink.copy(alpha = 0.35f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (selected) "●" else "○",
            color = if (selected)
                Coral
            else
                Ink.copy(alpha = 0.4f),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun WarningRow(
    number: String,
    title: String,
    description: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when (number) {
                        "1" -> Mint
                        "2" -> Yellow
                        else -> Coral
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = number,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = Ink,
            fontSize = 14.sp
        )

        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun toggleApp(
    apps: Set<String>,
    app: String
): Set<String> {

    return if (apps.contains(app)) {
        apps - app
    } else {
        apps + app
    }
}

@Composable
private fun PhoneIllustration() {

    Box(
        modifier = Modifier.size(
            width = 180.dp,
            height = 150.dp
        ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(
                    width = 90.dp,
                    height = 145.dp
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Ink)
                .padding(6.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp))
                    .background(Mint),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "PRIVATE",
                        color = Ink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🔒",
                        fontSize = 30.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(62.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(Coral)
                .border(
                    3.dp,
                    Ink,
                    CircleShape
                )
        ) {

            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Ink)
                )

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Ink)
                )
            }
        }

        Text(
            text = "👀",
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 15.dp)
        )
    }
}

@Composable
private fun DecorativeBackground() {

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        drawCircle(
            color = Yellow.copy(alpha = 0.45f),
            radius = 70f,
            center = Offset(
                size.width * 0.9f,
                size.height * 0.08f
            )
        )

        drawCircle(
            color = Lavender.copy(alpha = 0.45f),
            radius = 55f,
            center = Offset(
                size.width * 0.05f,
                size.height * 0.75f
            )
        )

        drawLine(
            color = Coral,
            start = Offset(
                size.width * 0.08f,
                size.height * 0.14f
            ),
            end = Offset(
                size.width * 0.17f,
                size.height * 0.10f
            ),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}
