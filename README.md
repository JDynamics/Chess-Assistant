# Chess Assistant

A chess analysis tool powered by Stockfish with multiple interfaces:

- **GUI Application** - Full graphical interface with board visualization, move history, and auto-play
- **CLI Analyzer** - Analyze board screenshots using Claude Vision
- **FEN Console** - Quick analysis by pasting FEN strings

**Output format:** `Piece(StartPos) to (EndPos)` (e.g., `Knight(g1) to (f3)`)

## Setup

### 1. Install Python dependencies

```bash
pip install -r requirements.txt
```

### 2. Install Stockfish

**Windows:**
- Download from https://stockfishchess.org/download/
- Extract to a folder (e.g., `C:\stockfish\`)
- Set environment variable: `STOCKFISH_PATH=C:\stockfish\stockfish.exe`

**Mac:**
```bash
brew install stockfish
```

**Linux:**
```bash
sudo apt install stockfish
```

### 3. Set up API key

Create a `.env` file in the project directory (copy from `.env.example`):

```
ANTHROPIC_API_KEY=your-api-key-here
STOCKFISH_PATH=C:\path\to\stockfish.exe
```

Or set environment variables directly:

**Windows (PowerShell):**
```powershell
$env:ANTHROPIC_API_KEY = "your-api-key-here"
```

**Windows (Command Prompt):**
```cmd
set ANTHROPIC_API_KEY=your-api-key-here
```

**Mac/Linux:**
```bash
export ANTHROPIC_API_KEY="your-api-key-here"
```

## Usage

### GUI Application (Recommended)

Launch the graphical interface:

```bash
python chess_gui.py
```

Features:
- Interactive chess board with piece highlighting
- Stockfish analysis with evaluation bar
- Save/load games in PGN format
- Auto-play mode
- Move history panel

### FEN Console

Quick analysis by pasting FEN strings:

```bash
python run.py
```

Paste a FEN position (e.g., from chess.com Settings > Game > Copy FEN) and get the best move with a brief explanation.

### CLI Analyzer (Screenshot Analysis)

Analyze board screenshots using Claude Vision.

#### From clipboard (default)

1. Take a screenshot of a chess board
2. Copy it to clipboard (or use snipping tool)
3. Run:

```bash
python chess_analyzer.py
```

#### From file

```bash
python chess_analyzer.py -f board.png
```

#### Playing as black

```bash
python chess_analyzer.py --color black
```

#### Verbose output (shows FEN and move details)

```bash
python chess_analyzer.py -v
```

#### Direct FEN input (skip image analysis)

```bash
python chess_analyzer.py --fen "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
```

## Examples

```
$ python chess_analyzer.py
Knight(g1) to (f3)

$ python chess_analyzer.py -v
Analyzing board position...
Detected FEN: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
Knight(g1) to (f3)

$ python chess_analyzer.py --fen "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
Bishop(c4) to (xf7) [captures Pawn], check
```

## Troubleshooting

**"No image found in clipboard"**
- Make sure you've copied an image (not a file path)
- Try using the snipping tool or Win+Shift+S

**"Error starting Stockfish"**
- Verify Stockfish is installed
- Set the full path: `STOCKFISH_PATH=C:\path\to\stockfish.exe`

**"ANTHROPIC_API_KEY not set"**
- Set your API key as an environment variable (see Setup step 3)

**Incorrect position detection**
- Ensure the board is clearly visible with no obstructions
- Standard piece designs work best
- Specify `--color black` if viewing from black's perspective
