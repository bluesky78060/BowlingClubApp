package com.bowlingclub.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun ThemePreviewLight() {
    BowlingClubTheme(darkTheme = false) {
        ThemeShowcase()
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun ThemePreviewDark() {
    BowlingClubTheme(darkTheme = true) {
        ThemeShowcase()
    }
}

@Composable
private fun ThemeShowcase() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Colors Section
        Text(
            text = "Color Palette",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        ColorPaletteRow(
            label = "Primary",
            color = MaterialTheme.colorScheme.primary,
            onColor = MaterialTheme.colorScheme.onPrimary
        )

        ColorPaletteRow(
            label = "Secondary",
            color = MaterialTheme.colorScheme.secondary,
            onColor = MaterialTheme.colorScheme.onSecondary
        )

        ColorPaletteRow(
            label = "Tertiary",
            color = MaterialTheme.colorScheme.tertiary,
            onColor = MaterialTheme.colorScheme.onTertiary
        )

        ColorPaletteRow(
            label = "Error",
            color = MaterialTheme.colorScheme.error,
            onColor = MaterialTheme.colorScheme.onError
        )

        // Custom Colors Section
        Text(
            text = "Custom Bowling Colors",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorBox("Gold", GoldRank)
            ColorBox("Silver", SilverRank)
            ColorBox("Bronze", BronzeRank)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorBox("Male", MaleColor)
            ColorBox("Female", FemaleColor)
            ColorBox("Active", ActiveStatus)
        }

        // Typography Section
        Text(
            text = "Typography",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        TypographyShowcase(
            label = "Display Large",
            style = MaterialTheme.typography.displayLarge,
            text = "Aa"
        )

        TypographyShowcase(
            label = "Headline Large",
            style = MaterialTheme.typography.headlineLarge,
            text = "Headline Large"
        )

        TypographyShowcase(
            label = "Title Large",
            style = MaterialTheme.typography.titleLarge,
            text = "Title Large"
        )

        TypographyShowcase(
            label = "Body Large",
            style = MaterialTheme.typography.bodyLarge,
            text = "Body Large - Main content text"
        )

        TypographyShowcase(
            label = "Label Large",
            style = MaterialTheme.typography.labelLarge,
            text = "LABEL LARGE"
        )

        // Custom Typography
        Text(
            text = "Bowling Typography",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        TypographyShowcase(
            label = "Score Display",
            style = BowlingTextStyles.ScoreDisplay,
            text = "245"
        )

        TypographyShowcase(
            label = "Frame Score",
            style = BowlingTextStyles.FrameScore,
            text = "25"
        )

        TypographyShowcase(
            label = "Pin Display",
            style = BowlingTextStyles.PinDisplay,
            text = "X  /  -"
        )

        // Buttons Section
        Text(
            text = "Buttons",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {}) {
                Text("Filled")
            }

            FilledTonalButton(onClick = {}) {
                Text("Tonal")
            }

            OutlinedButton(onClick = {}) {
                Text("Outlined")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(onClick = {}) {
                Text("Elevated")
            }

            TextButton(onClick = {}) {
                Text("Text")
            }

            FloatingActionButton(onClick = {}) {
                Text("+")
            }
        }

        // Cards Section
        Text(
            text = "Cards & Shapes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = BowlingShapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Standard Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Medium rounded corners (12dp)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = BowlingCustomShapes.StatCard
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Statistics Card",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Custom rounded corners (16dp)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ColorPaletteRow(
    label: String,
    color: Color,
    onColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = onColor
            )
            Text(
                text = "On$label",
                style = MaterialTheme.typography.bodyMedium,
                color = onColor
            )
        }
    }
}

@Composable
private fun ColorBox(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TypographyShowcase(
    label: String,
    style: TextStyle,
    text: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Individual component previews
@Preview(name = "Score Display Preview", showBackground = true)
@Composable
private fun ScoreDisplayPreview() {
    BowlingClubTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "245",
                    style = BowlingTextStyles.ScoreDisplay,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Perfect Game!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(name = "Rank Colors Preview", showBackground = true)
@Composable
private fun RankColorsPreview() {
    BowlingClubTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GoldRank, BowlingCustomShapes.Badge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", style = BowlingTextStyles.RankNumber)
                    }
                    Text("Gold", style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SilverRank, BowlingCustomShapes.Badge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", style = BowlingTextStyles.RankNumber)
                    }
                    Text("Silver", style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BronzeRank, BowlingCustomShapes.Badge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", style = BowlingTextStyles.RankNumber)
                    }
                    Text("Bronze", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
