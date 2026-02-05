# Chess Assistant Android

A native Android port of the Python Chess Assistant application. Analyze chess positions using Stockfish engine and Claude AI Vision API.

## Features

### 1. Screenshot Analyzer
- Capture chess board photos with camera
- Import images from gallery
- Claude Vision API detects piece positions
- Stockfish calculates the best move
- Support for playing as White or Black

### 2. Full Chess GUI
- Interactive chess board with touch controls
- Legal move highlighting (green squares)
- Last move highlighting
- Check/checkmate indicators
- Real-time Stockfish analysis panel
- "Play Best Move" button
- Move history with descriptive notation
- Save/Load games (PGN format)
- Undo move functionality
- Board auto-flips based on player color

### 3. FEN Console
- Paste FEN strings for quick analysis
- Instant best move calculation
- AI-powered move explanations (requires API key)
- Query history

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Android Studio | Otter 3 (2026) or later | [Download](https://developer.android.com/studio) |
| JDK | 21 | Bundled with Android Studio |
| Android SDK | 36 (Android 16) | Install via SDK Manager |
| Minimum Android Device | API 26 (Android 8.0 Oreo) | ~95% device coverage |

---

## Setup Instructions

### Step 1: Open the Project

1. Launch **Android Studio Otter 3**
2. Select **File > Open**
3. Navigate to `ChessAssistantAndroid` folder
4. Click **OK** and wait for Gradle sync to complete

### Step 2: Install Android SDK 36

1. Go to **Tools > SDK Manager**
2. In the **SDK Platforms** tab, check **Android 16 (API 36)**
3. In the **SDK Tools** tab, ensure these are installed:
   - Android SDK Build-Tools 36
   - Android SDK Platform-Tools
   - Android Emulator
4. Click **Apply** and wait for installation

### Step 3: Download Stockfish Binaries

The app requires Stockfish chess engine binaries for Android. Download pre-built binaries:

1. Go to the official Stockfish Android releases:
   ```
   https://github.com/official-stockfish/Stockfish/releases
   ```

2. Download the Android build (look for `stockfish-android-*.zip`)

3. Extract and copy binaries to these exact locations:
   ```
   app/src/main/assets/stockfish/arm64-v8a/stockfish
   app/src/main/assets/stockfish/armeabi-v7a/stockfish
   ```

4. Your folder structure should look like:
   ```
   app/
   └── src/
       └── main/
           └── assets/
               └── stockfish/
                   ├── arm64-v8a/
                   │   └── stockfish       (64-bit ARM binary)
                   └── armeabi-v7a/
                       └── stockfish       (32-bit ARM binary)
   ```

**Alternative: Build from source**

If you prefer to compile Stockfish yourself:
```bash
git clone https://github.com/official-stockfish/Stockfish.git
cd Stockfish/src

# For arm64-v8a (64-bit)
make -j ARCH=armv8 COMP=ndk build

# For armeabi-v7a (32-bit)
make -j ARCH=armv7 COMP=ndk build
```

### Step 4: Configure Anthropic API Key

The Screenshot Analyzer and FEN Console AI explanations require a Claude API key.

**Option A: local.properties (Recommended for development)**

1. Open or create `local.properties` in the project root (same level as `build.gradle.kts`)
2. Add your API key:
   ```properties
   ANTHROPIC_API_KEY=sk-ant-api03-your-key-here
   ```
3. This file is gitignored and won't be committed

**Option B: In-app Settings**

1. Build and run the app without an API key
2. Navigate to **Settings** screen
3. Enter your API key in the **Claude API Key** field
4. The key is stored encrypted on the device

**Get an API Key:**
1. Visit [console.anthropic.com](https://console.anthropic.com/)
2. Create an account or sign in
3. Navigate to **API Keys**
4. Create a new key with appropriate permissions

### Step 5: Build and Run

**Using Android Studio:**

1. Connect an Android device (USB debugging enabled) or start an emulator
2. Select your device from the device dropdown
3. Click the **Run** button (green play icon) or press `Shift+F10`
4. Wait for the build and installation to complete

**Using Command Line:**

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Step 6: Verify Installation

1. Launch the **Chess Assistant** app
2. From the home screen, test each feature:
   - **Play Chess**: Start a new game, make moves, verify Stockfish analysis appears
   - **Analyze Screenshot**: Take a photo of a chess position, verify it detects pieces
   - **FEN Console**: Paste a FEN string, verify best move is calculated

---

## Project Structure

```
ChessAssistantAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/chessassistant/
│   │   │   ├── ChessAssistantApp.kt       # Application class (Hilt)
│   │   │   ├── MainActivity.kt            # Single Activity entry point
│   │   │   │
│   │   │   ├── di/                        # Dependency Injection
│   │   │   │   ├── AppModule.kt           # App-wide dependencies
│   │   │   │   ├── NetworkModule.kt       # Retrofit/OkHttp setup
│   │   │   │   └── EngineModule.kt        # Stockfish engine
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── theme/                 # Material 3 theming
│   │   │   │   ├── navigation/            # Compose Navigation
│   │   │   │   ├── screens/               # Feature screens
│   │   │   │   │   ├── home/              # Main menu
│   │   │   │   │   ├── game/              # Chess GUI
│   │   │   │   │   ├── analyzer/          # Screenshot analyzer
│   │   │   │   │   ├── console/           # FEN console
│   │   │   │   │   └── settings/          # App settings
│   │   │   │   └── components/            # Reusable composables
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── model/                 # Data classes
│   │   │   │   ├── repository/            # Data access layer
│   │   │   │   └── local/                 # DataStore preferences
│   │   │   │
│   │   │   ├── network/                   # Claude API integration
│   │   │   │   ├── AnthropicApiService.kt
│   │   │   │   ├── VisionAnalyzer.kt
│   │   │   │   └── dto/                   # Request/Response models
│   │   │   │
│   │   │   ├── engine/                    # Chess logic
│   │   │   │   ├── StockfishEngine.kt     # UCI protocol wrapper
│   │   │   │   ├── ChessLogic.kt          # Move validation
│   │   │   │   ├── FenParser.kt           # FEN string parsing
│   │   │   │   └── MoveFormatter.kt       # Human-readable moves
│   │   │   │
│   │   │   └── util/                      # Utilities
│   │   │
│   │   ├── assets/stockfish/              # Engine binaries
│   │   └── res/                           # Android resources
│   │
│   └── build.gradle.kts                   # App dependencies
│
├── gradle/
│   ├── libs.versions.toml                 # Version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties      # Gradle 8.12
│
├── build.gradle.kts                       # Project config
├── settings.gradle.kts
└── local.properties                       # API keys (gitignored)
```

---

## Architecture

The app follows **MVVM** (Model-View-ViewModel) architecture with **Jetpack Compose** for UI.

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ GameScreen  │  │AnalyzerScreen│ │ConsoleScreen│     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
│         │                │                │             │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐     │
│  │GameViewModel│  │AnalyzerVM   │  │ ConsoleVM   │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
└─────────┼────────────────┼────────────────┼─────────────┘
          │                │                │
┌─────────▼────────────────▼────────────────▼─────────────┐
│                    Data Layer                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │              ChessRepository                     │   │
│  └──────────────────┬──────────────────────────────┘   │
│                     │                                   │
│  ┌──────────────────┼──────────────────┐               │
│  │                  │                  │               │
│  ▼                  ▼                  ▼               │
│ StockfishEngine  VisionAnalyzer  SettingsDataStore     │
└─────────────────────────────────────────────────────────┘
```

**Key Components:**

| Component | Responsibility |
|-----------|----------------|
| `ChessRepository` | Coordinates engine analysis, API calls, and data storage |
| `StockfishEngine` | UCI protocol communication with Stockfish binary |
| `VisionAnalyzer` | Sends images to Claude API, parses FEN response |
| `ChessLogic` | Legal move generation, check detection, game state |
| `FenParser` | Bidirectional FEN string parsing |
| `MoveFormatter` | Converts UCI moves to human-readable format |

---

## Move Format

Moves are displayed in the same format as the Python version:

```
Piece(StartSquare) to (EndSquare)
```

**Examples:**
- `Knight(g1) to (f3)`
- `Queen(d1) to (h5)`
- `King(e1) to (g1) [Kingside Castle]`
- `Pawn(e7) to (e8) (promotes to Queen)`

---

## Configuration

### Analysis Depth

Control Stockfish search depth in **Settings**:

| Depth | Speed | Strength | Use Case |
|-------|-------|----------|----------|
| 12 | Fast | Good | Quick suggestions |
| 18 | Medium | Strong | Default, balanced |
| 22 | Slow | Very Strong | Deep analysis |
| 26+ | Very Slow | Expert | Tournament prep |

### Theme Colors

Colors match the Python GUI version:

```kotlin
val LightSquare = Color(0xFFE8D4B8)    // Beige
val DarkSquare = Color(0xFFB58863)     // Brown
val HighlightColor = Color(0xFF7FFF00) // Green (legal moves)
val SelectedColor = Color(0xFFFFFF00)  // Yellow (selected piece)
val CheckColor = Color(0xFFFF6B6B)     // Red (king in check)
val AccentColor = Color(0xFF4CAF50)    // Green (buttons)
```

---

## Troubleshooting

### Stockfish not working

**Symptom:** "Engine initialization failed" or no best move appears

**Solutions:**
1. Verify binaries exist at correct paths in `assets/stockfish/`
2. Check device architecture matches available binary (arm64-v8a or armeabi-v7a)
3. Ensure binaries have execute permissions (should be automatic)
4. Check Logcat for detailed error messages:
   ```
   adb logcat -s StockfishEngine
   ```

### Vision API fails

**Symptom:** "Failed to analyze image" or "API error"

**Solutions:**
1. Verify API key is correctly set in `local.properties` or Settings
2. Check internet connectivity
3. Ensure API key has sufficient credits
4. Check Logcat for API response details:
   ```
   adb logcat -s VisionAnalyzer OkHttp
   ```

### Build fails

**Symptom:** Gradle sync or build errors

**Solutions:**
1. Ensure Android SDK 36 is installed
2. Check JDK version is 21:
   ```
   File > Project Structure > SDK Location > JDK Location
   ```
3. Invalidate caches:
   ```
   File > Invalidate Caches > Invalidate and Restart
   ```
4. Delete `.gradle` and `build` folders, then sync again

### Camera permission denied

**Symptom:** Camera button doesn't work

**Solutions:**
1. Grant camera permission in device settings
2. On Android 13+, ensure you're granting the correct permission
3. Uninstall and reinstall the app to reset permissions

---

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Test Checklist

- [ ] Launch app successfully
- [ ] Navigate to all screens from home
- [ ] Play a complete game to checkmate
- [ ] Undo moves work correctly
- [ ] Save and load a game (PGN)
- [ ] Capture photo and analyze
- [ ] Pick gallery image and analyze
- [ ] FEN console returns correct best move
- [ ] Settings persist after app restart
- [ ] Board flips when playing as Black

---

## Dependencies

| Library | Purpose | Version |
|---------|---------|---------|
| Jetpack Compose | Declarative UI | BOM 2026.01.00 |
| Material 3 | Design system | (via Compose BOM) |
| Hilt | Dependency injection | 2.54 |
| Retrofit | HTTP client | 2.11.0 |
| OkHttp | Networking | 4.13.0 |
| Kotlinx Serialization | JSON parsing | 1.8.0 |
| CameraX | Camera capture | 1.5.0 |
| Coil | Image loading | 3.0.0 |
| DataStore | Preferences storage | 1.2.0 |
| Navigation Compose | Screen navigation | 2.9.0 |
| Accompanist | Permissions handling | 0.37.0 |

---

## License

This project is for personal/educational use. Stockfish is licensed under GPL-3.0.

---

## Credits

- **Stockfish** - Open source chess engine
- **Anthropic Claude** - AI vision and language model
- Original Python implementation by the project author
