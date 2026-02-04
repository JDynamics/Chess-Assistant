#!/usr/bin/env python3
"""
Chess Board Analyzer
Analyzes chess board screenshots and suggests the best move using Stockfish.
Output format: Piece(StartPos) to (EndPos)
"""

import sys
import os
import io
import base64
import argparse
from typing import Optional, Tuple

try:
    import chess
    import chess.engine
    from PIL import Image, ImageGrab
    import anthropic
except ImportError as e:
    print(f"Missing dependency: {e}")
    print("Run: pip install -r requirements.txt")
    sys.exit(1)


# Configuration
STOCKFISH_PATH = os.environ.get("STOCKFISH_PATH", "stockfish")
ANTHROPIC_API_KEY = os.environ.get("ANTHROPIC_API_KEY", "")
ANALYSIS_TIME = 0.5  # seconds for Stockfish to analyze
STOCKFISH_DEPTH = 15  # search depth


PIECE_NAMES = {
    chess.PAWN: "Pawn",
    chess.KNIGHT: "Knight",
    chess.BISHOP: "Bishop",
    chess.ROOK: "Rook",
    chess.QUEEN: "Queen",
    chess.KING: "King",
}


def get_image_from_clipboard() -> Optional[Image.Image]:
    """Grab image from system clipboard."""
    try:
        image = ImageGrab.grabclipboard()
        if isinstance(image, Image.Image):
            return image
        return None
    except Exception as e:
        print(f"Error reading clipboard: {e}")
        return None


def get_image_from_file(path: str) -> Optional[Image.Image]:
    """Load image from file path."""
    try:
        return Image.open(path)
    except Exception as e:
        print(f"Error loading image: {e}")
        return None


def image_to_base64(image: Image.Image) -> str:
    """Convert PIL Image to base64 string."""
    buffer = io.BytesIO()
    image.save(buffer, format="PNG")
    return base64.standard_b64encode(buffer.getvalue()).decode("utf-8")


def crop_board_area(image: Image.Image) -> Image.Image:
    """
    Try to crop just the chess board from the screenshot.
    Looks for the 8x8 grid area, excluding UI elements.
    """
    width, height = image.size

    # Chess.com board is typically a square in the center-left of the window
    # Try to find a reasonable crop - assume board is roughly square
    # and takes up most of the height

    # Heuristic: board is usually 70-90% of the shorter dimension
    board_size = int(min(width, height) * 0.75)

    # Center the crop vertically, slight left offset
    left = int(width * 0.05)
    top = int((height - board_size) / 2)
    right = left + board_size
    bottom = top + board_size

    # Make sure we don't go out of bounds
    right = min(right, width)
    bottom = min(bottom, height)

    return image.crop((left, top, right, bottom))


def analyze_board_with_vision(image: Image.Image, playing_as: str = "white") -> Optional[str]:
    """
    Use Claude Vision to analyze the chess board and return FEN notation.
    """
    if not ANTHROPIC_API_KEY:
        print("Error: ANTHROPIC_API_KEY environment variable not set")
        return None

    client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)

    # Try to crop just the board area
    try:
        cropped = crop_board_area(image)
        base64_image = image_to_base64(cropped)
    except:
        base64_image = image_to_base64(image)

    if playing_as == "white":
        coord_note = """You are viewing from WHITE's perspective.
Visual top = rank 8, visual bottom = rank 1
Visual left = file a, visual right = file h"""
    else:
        coord_note = """You are viewing from BLACK's perspective.
Visual top = rank 1, visual bottom = rank 8
Visual left = file h, visual right = file a

When reading coordinates, use the LABELS shown on the board (numbers 1-8 on left, letters h-a on bottom)."""

    prompt = f"""{coord_note}

List EXACTLY what piece is on each square, using the board coordinates shown.
Use: K=white king, Q=white queen, R=white rook, B=white bishop, N=white knight, P=white pawn
     k=black king, q=black queen, r=black rook, b=black bishop, n=black knight, p=black pawn
     . = empty

WHITE pieces are LIGHT colored. BLACK pieces are DARK colored.
Ignore any dots/circles (those are move hints, not pieces).

Output in this EXACT format (8 characters per line, no spaces):
8:????????
7:????????
6:????????
5:????????
4:????????
3:????????
2:????????
1:????????

Replace ? with the piece letter or . for empty.
Example: 8:rnbqkbnr means black's back rank with all pieces."""

    try:
        response = client.messages.create(
            model="claude-opus-4-20250514",
            max_tokens=2000,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": "image/png",
                                "data": base64_image,
                            },
                        },
                        {
                            "type": "text",
                            "text": prompt,
                        },
                    ],
                }
            ],
        )

        raw = response.content[0].text.strip()

        # Debug: show raw response
        print(f"[DEBUG] Raw vision response:\n{raw[:600]}...")

        import re

        # Parse the structured format (8:rnbqkbnr etc)
        ranks = {}
        for line in raw.split('\n'):
            match = re.match(r'^([1-8]):([rnbqkpRNBQKP.]{8})$', line.strip())
            if match:
                rank_num = match.group(1)
                pieces = match.group(2)
                ranks[rank_num] = pieces

        if len(ranks) == 8:
            # Convert to FEN format
            fen_parts = []
            for rank in '87654321':
                rank_str = ranks.get(rank, '........')
                # Convert dots to numbers
                fen_rank = ''
                empty_count = 0
                for char in rank_str:
                    if char == '.':
                        empty_count += 1
                    else:
                        if empty_count > 0:
                            fen_rank += str(empty_count)
                            empty_count = 0
                        fen_rank += char
                if empty_count > 0:
                    fen_rank += str(empty_count)
                fen_parts.append(fen_rank)

            board_fen = '/'.join(fen_parts)
            return f"{board_fen} {playing_as[0]} KQkq - 0 1"

        # Fallback: try to extract FEN directly
        board_pattern = r'([rnbqkpRNBQKP1-8]+/){7}[rnbqkpRNBQKP1-8]+'
        match = re.search(board_pattern, raw)
        if match:
            return f"{match.group(0)} {playing_as[0]} KQkq - 0 1"

        print("[DEBUG] Could not parse board position")
        return None

    except Exception as e:
        print(f"Vision API error: {e}")
        return None


def validate_fen(fen: str) -> bool:
    """Validate FEN string by attempting to create a board."""
    try:
        board = chess.Board(fen)
        return board.is_valid()
    except:
        return False


def get_best_move(fen: str) -> Optional[Tuple[chess.Move, chess.Board]]:
    """
    Use Stockfish to find the best move for the given position.
    Returns (best_move, board) tuple.
    """
    try:
        board = chess.Board(fen)
    except ValueError as e:
        print(f"Invalid FEN: {e}")
        return None

    if not board.is_valid():
        print("Warning: Board position may be invalid")

    engine = None
    try:
        engine = chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH)
        result = engine.play(
            board,
            chess.engine.Limit(time=ANALYSIS_TIME, depth=STOCKFISH_DEPTH)
        )

        if result.move and result.move in board.legal_moves:
            return (result.move, board)
        return None

    except FileNotFoundError:
        print(f"Error: Stockfish not found at: {STOCKFISH_PATH}")
        return None
    except Exception as e:
        print(f"Engine error: {e}")
        return None
    finally:
        if engine:
            try:
                engine.quit()
            except:
                pass


def format_move(move: chess.Move, board: chess.Board) -> str:
    """
    Format move as: Piece(StartPos) to (EndPos)
    Example: Knight(g1) to (f3)
    """
    piece = board.piece_at(move.from_square)
    if piece is None:
        return f"({chess.square_name(move.from_square)}) to ({chess.square_name(move.to_square)})"

    piece_name = PIECE_NAMES.get(piece.piece_type, "Piece")
    from_square = chess.square_name(move.from_square)
    to_square = chess.square_name(move.to_square)

    # Handle promotion
    promotion = ""
    if move.promotion:
        promo_name = PIECE_NAMES.get(move.promotion, "")
        promotion = f" (promotes to {promo_name})"

    # Handle castling
    if board.is_castling(move):
        if board.is_kingside_castling(move):
            return f"King({from_square}) to ({to_square}) [Kingside Castle]"
        else:
            return f"King({from_square}) to ({to_square}) [Queenside Castle]"

    return f"{piece_name}({from_square}) to ({to_square}){promotion}"


def analyze_position(image: Image.Image, playing_as: str = "white", verbose: bool = False) -> Optional[str]:
    """
    Full analysis pipeline: image -> FEN -> best move.
    Returns formatted move string.
    """
    if verbose:
        print("Analyzing board position...")

    # Step 1: Vision analysis
    fen = analyze_board_with_vision(image, playing_as)
    if not fen:
        print("Failed to analyze board image")
        return None

    if verbose:
        print(f"Detected FEN: {fen}")

    # Step 2: Validate FEN
    if not validate_fen(fen):
        print(f"Warning: FEN validation failed, attempting analysis anyway")

    # Step 3: Get best move from Stockfish
    result = get_best_move(fen)
    if not result:
        print("Failed to calculate best move")
        return None

    move, board = result

    # Step 4: Format output
    formatted = format_move(move, board)

    if verbose:
        # Show additional info
        info = get_move_info(move, board)
        if info:
            print(f"Move details: {info}")

    return formatted


def get_move_info(move: chess.Move, board: chess.Board) -> str:
    """Get additional move information."""
    info_parts = []

    if board.is_capture(move):
        captured = board.piece_at(move.to_square)
        if captured:
            info_parts.append(f"captures {PIECE_NAMES.get(captured.piece_type, 'piece')}")

    # Check what happens after the move
    board_copy = board.copy()
    board_copy.push(move)

    if board_copy.is_checkmate():
        info_parts.append("CHECKMATE!")
    elif board_copy.is_check():
        info_parts.append("check")

    return ", ".join(info_parts) if info_parts else ""


def main():
    parser = argparse.ArgumentParser(
        description="Analyze chess board and suggest best move",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python chess_analyzer.py                    # Analyze from clipboard
  python chess_analyzer.py -f board.png       # Analyze from file
  python chess_analyzer.py --color black      # Playing as black
  python chess_analyzer.py -v                 # Verbose output
        """
    )
    parser.add_argument(
        "-f", "--file",
        help="Path to chess board image file"
    )
    parser.add_argument(
        "-c", "--color",
        choices=["white", "black"],
        default="white",
        help="Color you are playing as (default: white)"
    )
    parser.add_argument(
        "-v", "--verbose",
        action="store_true",
        help="Show detailed analysis info"
    )
    parser.add_argument(
        "--fen",
        help="Directly provide FEN string instead of image"
    )

    args = parser.parse_args()

    # Direct FEN input mode
    if args.fen:
        result = get_best_move(args.fen)
        if result:
            move, board = result
            print(format_move(move, board))
        else:
            print("Failed to analyze position")
            sys.exit(1)
        return

    # Get image
    if args.file:
        image = get_image_from_file(args.file)
        if not image:
            print(f"Could not load image: {args.file}")
            sys.exit(1)
    else:
        image = get_image_from_clipboard()
        if not image:
            print("No image found in clipboard")
            print("Copy a chess board screenshot to clipboard and try again")
            sys.exit(1)

    # Analyze
    result = analyze_position(image, args.color, args.verbose)

    if result:
        print(result)
    else:
        print("Analysis failed")
        sys.exit(1)


if __name__ == "__main__":
    main()
