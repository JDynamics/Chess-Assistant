package com.chessassistant.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chessassistant.R
import com.chessassistant.ui.theme.AccentColor
import com.chessassistant.ui.theme.BackgroundColor
import com.chessassistant.ui.theme.PanelColor

/**
 * Home screen with navigation to different app modes.
 */
@Composable
fun HomeScreen(
    onNavigateToGame: () -> Unit,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToLiveCamera: () -> Unit,
    onNavigateToConsole: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "♔ Chess Assistant",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chess icon
        Text(
            text = "♘",
            fontSize = 80.sp,
            color = AccentColor,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Menu cards
        MenuCard(
            title = stringResource(R.string.home_play_chess),
            description = stringResource(R.string.home_play_chess_desc),
            icon = Icons.Default.Extension,
            onClick = onNavigateToGame
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuCard(
            title = stringResource(R.string.home_analyze_screenshot),
            description = stringResource(R.string.home_analyze_screenshot_desc),
            icon = Icons.Default.Camera,
            onClick = onNavigateToAnalyzer
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuCard(
            title = stringResource(R.string.home_live_camera),
            description = stringResource(R.string.home_live_camera_desc),
            icon = Icons.Default.Videocam,
            onClick = onNavigateToLiveCamera
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuCard(
            title = stringResource(R.string.home_fen_console),
            description = stringResource(R.string.home_fen_console_desc),
            icon = Icons.Default.Terminal,
            onClick = onNavigateToConsole
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version info
        Text(
            text = "Powered by Stockfish & Claude",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}
