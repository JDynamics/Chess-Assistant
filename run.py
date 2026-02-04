#!/usr/bin/env python3
"""
Chess Assistant - Paste FEN, get best move.
"""

import os
import chess
import anthropic

from dotenv import load_dotenv
load_dotenv()

from chess_analyzer import get_best_move, format_move

ANTHROPIC_API_KEY = os.environ.get("ANTHROPIC_API_KEY", "")


def explain_move(fen, move_san):
    """Get brief strategic explanation."""
    if not ANTHROPIC_API_KEY:
        return None
    try:
        client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)
        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=150,
            messages=[{"role": "user", "content": f"Chess position FEN: {fen}\nBest move: {move_san}\nIn 1-2 sentences, why is this the best move? Be very brief."}]
        )
        return response.content[0].text.strip()
    except:
        return None


def main():
    print("=" * 50)
    print("  CHESS ASSISTANT")
    print("=" * 50)
    print()
    print("Paste FEN from chess.com (Settings > Game > Copy FEN)")
    print()

    while True:
        try:
            fen = input("FEN>>> ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nBye!")
            break

        if not fen:
            continue

        # Validate FEN format
        if '/' not in fen or fen.count('/') < 7:
            print("Invalid FEN format.\n")
            continue

        # Get best move from Stockfish
        result = get_best_move(fen)
        if not result:
            print("Could not calculate. Check FEN.\n")
            continue

        move, board = result
        move_san = board.san(move)
        formatted = format_move(move, board)

        # Show move FIRST
        print(f"\n  >> {formatted}")
        print(f"     ({move_san})\n")

        # Then strategy
        explanation = explain_move(fen, move_san)
        if explanation:
            print(f"Why: {explanation}\n")


if __name__ == "__main__":
    main()
