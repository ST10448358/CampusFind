package com.segotlo.campusfindapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segotlo.campusfindapp.ui.components.GradientButton
import com.segotlo.campusfindapp.ui.theme.*

@Composable
fun OpeningScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // CampusFind Logo
            com.segotlo.campusfindapp.ui.components.CampusFindLogo(
                modifier = Modifier.size(130.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Campus",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Find",
                    color = TealAccent,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Find what you lost. Return what you found.",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureSmallCard(
                    title = "Smart Matching",
                    subtitle = "Automated similarity engine",
                    icon = Icons.Default.Stars,
                    modifier = Modifier.weight(1f)
                )
                FeatureSmallCard(
                    title = "Verified Returns",
                    subtitle = "Ownership verification",
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GradientButton(
                text = "Get Started",
                onClick = onGetStarted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor), // Simple border
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = "Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        // Language switcher at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text("English", color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Setswana", color = TextSecondary)
            Text("isiZulu", color = TextSecondary)
        }
    }
}

@Composable
fun FeatureSmallCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardNavy,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp, lineHeight = 12.sp)
        }
    }
}
