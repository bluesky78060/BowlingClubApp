# Bowling Club App - Theme System

Professional Jetpack Compose Material3 theme with bowling-specific design language.

## Design Concept

**Blue & Orange Bowling Theme**
- Primary: Professional bowling alley blue (#1565C0)
- Secondary: Bowling ball orange accent (#FF6F00)
- Clean, professional, and energetic aesthetic

## Files

### Color.kt
Complete Material3 color system with:
- **Light/Dark theme colors** - Full primary, secondary, tertiary, error palettes
- **Custom bowling colors**:
  - Rank indicators: `GoldRank`, `SilverRank`, `BronzeRank`
  - Gender indicators: `MaleColor`, `FemaleColor`
  - Status indicators: `ActiveStatus`, `InactiveStatus`, `PendingStatus`
  - Score indicators: `StrikeColor`, `SpareColor`, `SplitColor`
  - Score levels: `ScoreExcellent`, `ScoreGood`, `ScoreAverage`, `ScoreBelowAverage`

### Type.kt
Material3 typography system:
- Standard Material3 text styles (displayLarge → labelSmall)
- **Custom bowling text styles**:
  - `BowlingTextStyles.ScoreDisplay` - Large score numbers (48sp)
  - `BowlingTextStyles.FrameScore` - Frame scores (20sp)
  - `BowlingTextStyles.PinDisplay` - Pin symbols X / - (24sp)
  - `BowlingTextStyles.AverageDisplay` - Average display (32sp)
  - `BowlingTextStyles.RankNumber` - Rank numbers (28sp)
  - `BowlingTextStyles.MemberName` - Member list names (18sp)
  - `BowlingTextStyles.StatLabel` - Statistics labels (13sp)
  - `BowlingTextStyles.StatValue` - Statistics values (24sp)

### Shape.kt
Material3 shape system:
- Standard shapes: small (8dp), medium (12dp), large (16dp)
- **Custom bowling shapes**:
  - `BowlingCustomShapes.ScoreFrame` - Score card frames (4dp)
  - `BowlingCustomShapes.FAB` - Floating action button (16dp)
  - `BowlingCustomShapes.Avatar` - Member avatars (50% rounded)
  - `BowlingCustomShapes.Badge` - Rank/status badges (12dp)
  - `BowlingCustomShapes.TextField` - Input fields (8dp)
  - `BowlingCustomShapes.ScoreButton` - Score input buttons (6dp)
  - `BowlingCustomShapes.StatCard` - Statistics cards (16dp)
  - `BowlingCustomShapes.Drawer` - Navigation drawer
  - `BowlingCustomShapes.BottomBar` - Bottom navigation
  - `BowlingCustomShapes.BracketCard` - Tournament brackets (12dp)

### Theme.kt
Main theme composable:
```kotlin
BowlingClubTheme(
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    // Your app content
}
```
- Auto status bar color management
- Full Material3 integration
- Light/dark theme support

### ThemePreview.kt
Comprehensive preview composables showing:
- All color palettes
- Typography styles
- Button variants
- Card styles
- Custom bowling components

## Usage Examples

### Basic Usage
```kotlin
@Composable
fun MyScreen() {
    BowlingClubTheme {
        Scaffold { padding ->
            // Content
        }
    }
}
```

### Using Theme Colors
```kotlin
// Standard Material3 colors
Text(
    text = "Score",
    color = MaterialTheme.colorScheme.primary
)

// Custom bowling colors
Box(
    modifier = Modifier.background(GoldRank)
) {
    Text("1st Place")
}

Icon(
    imageVector = Icons.Default.Person,
    tint = if (isMale) MaleColor else FemaleColor
)
```

### Using Typography
```kotlin
// Standard typography
Text(
    text = "Member Name",
    style = MaterialTheme.typography.titleLarge
)

// Custom bowling typography
Text(
    text = "245",
    style = BowlingTextStyles.ScoreDisplay,
    color = MaterialTheme.colorScheme.primary
)

Text(
    text = "X",
    style = BowlingTextStyles.PinDisplay,
    color = StrikeColor
)
```

### Using Shapes
```kotlin
// Standard shapes
Card(
    shape = MaterialTheme.shapes.medium
) {
    // Content
}

// Custom bowling shapes
Card(
    shape = BowlingCustomShapes.StatCard
) {
    // Statistics content
}

Box(
    modifier = Modifier
        .clip(BowlingCustomShapes.Avatar)
        .background(MaterialTheme.colorScheme.primaryContainer)
) {
    // Avatar image
}
```

### Score Display Example
```kotlin
@Composable
fun ScoreDisplay(score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = score.toString(),
            style = BowlingTextStyles.ScoreDisplay,
            color = when {
                score >= 200 -> ScoreExcellent
                score >= 150 -> ScoreGood
                score >= 100 -> ScoreAverage
                else -> ScoreBelowAverage
            }
        )
        Text(
            text = "Total Score",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Rank Badge Example
```kotlin
@Composable
fun RankBadge(rank: Int) {
    val backgroundColor = when (rank) {
        1 -> GoldRank
        2 -> SilverRank
        3 -> BronzeRank
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor, BowlingCustomShapes.Badge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            style = BowlingTextStyles.RankNumber
        )
    }
}
```

### Member Status Example
```kotlin
@Composable
fun MemberStatus(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = if (isActive) ActiveStatus else InactiveStatus,
                shape = CircleShape
            )
    )
}
```

## Color Palette Quick Reference

### Primary Colors
- Light: `#1565C0` (Blue)
- Dark: `#9ECAFF` (Light Blue)

### Secondary Colors
- Light: `#FF6F00` (Orange)
- Dark: `#FFB74D` (Light Orange)

### Tertiary Colors
- Light: `#4CAF50` (Green)
- Dark: `#81C784` (Light Green)

### Custom Colors
- Gold: `#FFD700`
- Silver: `#C0C0C0`
- Bronze: `#CD7F32`
- Male: `#42A5F5`
- Female: `#EC407A`
- Strike: `#FF6F00`
- Spare: `#1565C0`

## Best Practices

1. **Use Material3 colors first** - Only use custom colors for bowling-specific UI
2. **Follow theme** - Use `MaterialTheme.colorScheme` for theme-aware colors
3. **Typography consistency** - Use standard styles, custom only for specialized displays
4. **Shape consistency** - Use predefined shapes for consistent corner radius
5. **Dark theme support** - Test both light and dark themes
6. **Accessibility** - Ensure sufficient color contrast for all text

## Preview

To see all theme elements in Android Studio:
1. Open `ThemePreview.kt`
2. View `ThemePreviewLight` and `ThemePreviewDark` previews
3. Check individual component previews like `ScoreDisplayPreview` and `RankColorsPreview`
