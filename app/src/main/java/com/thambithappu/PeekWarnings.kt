package com.thambithappu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF292522)
private val Coral = Color(0xFFFF8A7A)

/** Stand-in for "a protected app" (chat, banking, whatever) used in the demo. */
@Composable
fun DemoSensitiveContent(blurred: Boolean) {
    Box(
        modifier = (if (blurred) Modifier.blurEffect(radiusPx = 40f) else Modifier)
            .fillMaxSize()
            .background(Color(0xFF102040)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Account Balance: $12,480.00\n\nYour messages:\nMom: call me when free\nWork: meeting moved to 3pm",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

/** "Privacy Overlay" mode: opaque full-screen cover with a warning icon. */
@Composable
fun FullScreenProtectOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔒", fontSize = 56.sp)
            Text(
                text = "Screen protected",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SmallActionChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Ink
        )
    }
}

/** Peek level 1: ask before doing anything. */
@Composable
fun PeekWarningDialog(
    title: String,
    message: String,
    onContinue: () -> Unit,
    onProtect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, fontSize = 14.sp, color = Ink.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onContinue) { Text("Continue") }
                    Button(onClick = onProtect) { Text("Protect") }
                }
            }
        }
    }
}

/** Peek level 2: protection is already applied - this is just the stronger banner, tap to dismiss. */
@Composable
fun StrongWarningBanner(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onDismiss() },
            color = Coral
        ) {
            Text(
                text = "🚨 Still peeking - screen is protected. Tap to dismiss.",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Peek level 3: the Tamil meme warning. Tune the text to taste before the demo. */
@Composable
fun TamilMemeWarning(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🙈", fontSize = 64.sp)
            Text(
                text = "\"Yaaru da nee, inspector-ah?\"",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Third time peeking. Screen's protected - move along thambi 😌",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
