# Chess Board Analyzer

Analyzes chess board screenshots and suggests the best move using Claude Vision + Stockfish.

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

Set your Anthropic API key as an environment variable:

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

### From clipboard (default)

1. Take a screenshot of a chess board
2. Copy it to clipboard (or use snipping tool)
3. Run:

```bash
python chess_analyzer.py
```

### From file

```bash
python chess_analyzer.py -f board.png
```

### Playing as black

```bash
python chess_analyzer.py --color black
```

### Verbose output (shows FEN and move details)

```bash
python chess_analyzer.py -v
```

### Direct FEN input (skip image analysis)

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
