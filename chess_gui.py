"""
Chess GUI with Stockfish Analysis - Enhanced Edition
Features: Unicode pieces, save/load games, auto-play, enhanced graphics
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import chess
import chess.engine
import chess.pgn
import threading
import os
import io
from datetime import datetime
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()


class ChessGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Chess Assistant Pro")
        self.root.configure(bg="#2b2b2b")

        self.board = chess.Board()
        self.selected_square = None
        self.engine = None
        self.analysis_text = None
        self.player_color = None
        self.board_flipped = False
        self.last_move = None  # Track last move for highlighting
        self.best_move = None  # Store the current best move for auto-play
        self.auto_play_enabled = False
        self.game_over_shown = False  # Prevent multiple game over popups

        # Unicode chess pieces (much nicer looking!)
        self.piece_unicode = {
            (chess.PAWN, chess.WHITE): '♙',
            (chess.KNIGHT, chess.WHITE): '♘',
            (chess.BISHOP, chess.WHITE): '♗',
            (chess.ROOK, chess.WHITE): '♖',
            (chess.QUEEN, chess.WHITE): '♕',
            (chess.KING, chess.WHITE): '♔',
            (chess.PAWN, chess.BLACK): '♟',
            (chess.KNIGHT, chess.BLACK): '♞',
            (chess.BISHOP, chess.BLACK): '♝',
            (chess.ROOK, chess.BLACK): '♜',
            (chess.QUEEN, chess.BLACK): '♛',
            (chess.KING, chess.BLACK): '♚',
        }

        # Full piece names for descriptive notation
        self.piece_names = {
            chess.PAWN: 'Pawn', chess.KNIGHT: 'Knight', chess.BISHOP: 'Bishop',
            chess.ROOK: 'Rook', chess.QUEEN: 'Queen', chess.KING: 'King'
        }

        # Enhanced color scheme
        self.light_square = "#E8D4B8"
        self.dark_square = "#B58863"
        self.highlight_color = "#7FFF00"
        self.selected_color = "#FFFF00"
        self.last_move_light = "#CDD26A"
        self.last_move_dark = "#AAA23A"
        self.check_color = "#FF6B6B"

        # UI colors
        self.bg_color = "#2b2b2b"
        self.panel_color = "#363636"
        self.text_color = "#E0E0E0"
        self.accent_color = "#4CAF50"

        self.setup_ui()
        self.connect_engine()
        self.root.after(100, self.show_player_selection)

    def setup_ui(self):
        # Configure styles
        style = ttk.Style()
        style.theme_use('clam')
        style.configure('TFrame', background=self.bg_color)
        style.configure('TLabel', background=self.bg_color, foreground=self.text_color)
        style.configure('TButton', padding=6)
        style.configure('Green.TButton', background=self.accent_color)
        style.configure('Panel.TFrame', background=self.panel_color)

        # Main frame
        main_frame = ttk.Frame(self.root, padding="15", style='TFrame')
        main_frame.grid(row=0, column=0, sticky="nsew")

        # Board container
        self.board_container = ttk.Frame(main_frame, style='TFrame')
        self.board_container.grid(row=0, column=0, padx=(0, 20))

        # Coordinate labels storage
        self.rank_labels_left = []
        self.rank_labels_right = []
        self.file_labels_top = []
        self.file_labels_bottom = []

        # Top file labels - each label width matches square width (65px)
        top_files_frame = tk.Frame(self.board_container, bg=self.bg_color)
        top_files_frame.grid(row=0, column=1, pady=(0, 3))
        for col in range(8):
            file_label = tk.Label(top_files_frame, text="", font=("Segoe UI", 10, "bold"),
                                  bg=self.bg_color, fg=self.text_color, width=65, anchor="center")
            file_label.grid(row=0, column=col, ipadx=0, ipady=0)
            file_label.config(width=0)  # Reset width, use fixed pixel container
            # Use a frame to enforce exact width
            lbl_frame = tk.Frame(top_files_frame, width=65, height=20, bg=self.bg_color)
            lbl_frame.grid(row=0, column=col)
            lbl_frame.grid_propagate(False)
            file_label = tk.Label(lbl_frame, text="", font=("Segoe UI", 10, "bold"),
                                  bg=self.bg_color, fg=self.text_color)
            file_label.place(relx=0.5, rely=0.5, anchor="center")
            self.file_labels_top.append(file_label)

        # Left rank labels - each label height matches square height (65px)
        left_ranks_frame = tk.Frame(self.board_container, bg=self.bg_color)
        left_ranks_frame.grid(row=1, column=0, padx=(0, 5))
        for row in range(8):
            lbl_frame = tk.Frame(left_ranks_frame, width=20, height=65, bg=self.bg_color)
            lbl_frame.grid(row=row, column=0)
            lbl_frame.grid_propagate(False)
            rank_label = tk.Label(lbl_frame, text="", font=("Segoe UI", 10, "bold"),
                                  bg=self.bg_color, fg=self.text_color)
            rank_label.place(relx=0.5, rely=0.5, anchor="center")
            self.rank_labels_left.append(rank_label)

        # Board frame with border
        board_border = tk.Frame(self.board_container, bg="#8B4513", padx=3, pady=3)
        board_border.grid(row=1, column=1)
        self.board_frame = tk.Frame(board_border, bg=self.dark_square)
        self.board_frame.pack()

        # Create board squares
        self.squares = {}
        self.labels = {}
        self.square_frames = {}

        for row in range(8):
            for col in range(8):
                color = self.light_square if (row + col) % 2 == 0 else self.dark_square
                frame = tk.Frame(self.board_frame, width=65, height=65, bg=color,
                                highlightthickness=0)
                frame.grid(row=row, column=col, padx=0, pady=0)
                frame.grid_propagate(False)

                label = tk.Label(frame, text="", font=("Segoe UI Symbol", 36),
                               bg=color, fg="black")
                label.place(relx=0.5, rely=0.5, anchor="center")
                self.square_frames[(row, col)] = (frame, label)

        # Right rank labels - each label height matches square height (65px)
        right_ranks_frame = tk.Frame(self.board_container, bg=self.bg_color)
        right_ranks_frame.grid(row=1, column=2, padx=(5, 0))
        for row in range(8):
            lbl_frame = tk.Frame(right_ranks_frame, width=20, height=65, bg=self.bg_color)
            lbl_frame.grid(row=row, column=0)
            lbl_frame.grid_propagate(False)
            rank_label = tk.Label(lbl_frame, text="", font=("Segoe UI", 10, "bold"),
                                  bg=self.bg_color, fg=self.text_color)
            rank_label.place(relx=0.5, rely=0.5, anchor="center")
            self.rank_labels_right.append(rank_label)

        # Bottom file labels - each label width matches square width (65px)
        bottom_files_frame = tk.Frame(self.board_container, bg=self.bg_color)
        bottom_files_frame.grid(row=2, column=1, pady=(3, 0))
        for col in range(8):
            lbl_frame = tk.Frame(bottom_files_frame, width=65, height=20, bg=self.bg_color)
            lbl_frame.grid(row=0, column=col)
            lbl_frame.grid_propagate(False)
            file_label = tk.Label(lbl_frame, text="", font=("Segoe UI", 10, "bold"),
                                  bg=self.bg_color, fg=self.text_color)
            file_label.place(relx=0.5, rely=0.5, anchor="center")
            self.file_labels_bottom.append(file_label)

        self.update_board_orientation()

        # Right panel
        right_panel = tk.Frame(main_frame, bg=self.panel_color, padx=15, pady=15)
        right_panel.grid(row=0, column=1, sticky="nsew")

        # Turn indicator with colored background
        self.turn_frame = tk.Frame(right_panel, bg=self.accent_color, padx=10, pady=8)
        self.turn_frame.pack(fill="x", pady=(0, 15))
        self.turn_label = tk.Label(self.turn_frame, text="White to move",
                                   font=("Segoe UI", 14, "bold"), bg=self.accent_color, fg="white")
        self.turn_label.pack()

        # Analysis section
        analysis_header = tk.Frame(right_panel, bg=self.panel_color)
        analysis_header.pack(fill="x")
        tk.Label(analysis_header, text="♟ Stockfish Analysis",
                font=("Segoe UI", 12, "bold"), bg=self.panel_color, fg=self.text_color).pack(anchor="w")

        self.analysis_text = tk.Text(right_panel, width=38, height=12,
                                     font=("Consolas", 10), bg="#1e1e1e", fg="#00FF00",
                                     insertbackground="white", relief="flat", padx=10, pady=10)
        self.analysis_text.pack(pady=(5, 10), fill="x")
        self.analysis_text.insert("1.0", "Connecting to Stockfish...")
        self.analysis_text.config(state="disabled")

        # Auto-play button (prominent!)
        self.auto_play_btn = tk.Button(right_panel, text="▶ PLAY BEST MOVE",
                                       font=("Segoe UI", 12, "bold"),
                                       bg="#4CAF50", fg="white", activebackground="#45a049",
                                       command=self.play_best_move, relief="flat", pady=8)
        self.auto_play_btn.pack(fill="x", pady=(0, 10))

        # Move history
        tk.Label(right_panel, text="📜 Move History",
                font=("Segoe UI", 12, "bold"), bg=self.panel_color, fg=self.text_color).pack(anchor="w")

        self.history_text = tk.Text(right_panel, width=38, height=6,
                                    font=("Consolas", 9), bg="#1e1e1e", fg=self.text_color,
                                    relief="flat", padx=10, pady=10)
        self.history_text.pack(pady=(5, 10), fill="x")
        self.history_text.config(state="disabled")

        # Button rows
        button_frame1 = tk.Frame(right_panel, bg=self.panel_color)
        button_frame1.pack(fill="x", pady=5)

        tk.Button(button_frame1, text="🎮 New Game", font=("Segoe UI", 10),
                 command=self.new_game, bg="#555", fg="white", relief="flat",
                 padx=15, pady=5).pack(side="left", padx=(0, 5))
        tk.Button(button_frame1, text="↩ Undo", font=("Segoe UI", 10),
                 command=self.undo_move, bg="#555", fg="white", relief="flat",
                 padx=15, pady=5).pack(side="left", padx=5)

        button_frame2 = tk.Frame(right_panel, bg=self.panel_color)
        button_frame2.pack(fill="x", pady=5)

        tk.Button(button_frame2, text="💾 Save Game", font=("Segoe UI", 10),
                 command=self.save_game, bg="#2196F3", fg="white", relief="flat",
                 padx=15, pady=5).pack(side="left", padx=(0, 5))
        tk.Button(button_frame2, text="📂 Load Game", font=("Segoe UI", 10),
                 command=self.load_game, bg="#2196F3", fg="white", relief="flat",
                 padx=15, pady=5).pack(side="left", padx=5)

        # Status bar
        self.status_var = tk.StringVar(value="Click a piece to select it")
        status_frame = tk.Frame(right_panel, bg="#1e1e1e", pady=8)
        status_frame.pack(fill="x", pady=(10, 0))
        tk.Label(status_frame, textvariable=self.status_var,
                font=("Consolas", 10), bg="#1e1e1e", fg="#FFD700").pack()

        self.update_board()

    def connect_engine(self):
        def connect():
            env_path = os.getenv("STOCKFISH_PATH")
            paths = [
                env_path,
                "stockfish",
                "stockfish.exe",
                r"C:\Program Files\Stockfish\stockfish.exe",
                r"C:\stockfish\stockfish.exe",
                "./stockfish/stockfish-windows-x86-64-avx2.exe",
                "/usr/local/bin/stockfish",
                "/usr/bin/stockfish",
            ]
            for path in paths:
                if path is None:
                    continue
                try:
                    self.engine = chess.engine.SimpleEngine.popen_uci(path)
                    self.update_analysis()
                    return
                except Exception:
                    continue
            self.root.after(0, self.show_engine_error)

        threading.Thread(target=connect, daemon=True).start()

    def show_engine_error(self):
        self.analysis_text.config(state="normal")
        self.analysis_text.delete("1.0", "end")
        self.analysis_text.insert("1.0", "⚠ Stockfish not found!\n\n"
                                 "Install Stockfish:\n"
                                 "• Windows: stockfishchess.org\n"
                                 "• Mac: brew install stockfish\n"
                                 "• Linux: apt install stockfish")
        self.analysis_text.config(state="disabled")

    def update_board_orientation(self):
        self.squares = {}
        self.labels = {}

        for i in range(8):
            if self.board_flipped:
                rank_num = str(i + 1)
                file_letter = chr(ord('h') - i)
            else:
                rank_num = str(8 - i)
                file_letter = chr(ord('a') + i)

            self.rank_labels_left[i].config(text=rank_num)
            self.rank_labels_right[i].config(text=rank_num)
            self.file_labels_top[i].config(text=file_letter)
            self.file_labels_bottom[i].config(text=file_letter)

        for visual_row in range(8):
            for visual_col in range(8):
                if self.board_flipped:
                    chess_file = 7 - visual_col
                    chess_rank = visual_row
                else:
                    chess_file = visual_col
                    chess_rank = 7 - visual_row

                square = chess.square(chess_file, chess_rank)
                frame, label = self.square_frames[(visual_row, visual_col)]
                self.squares[square] = frame
                self.labels[square] = label

                frame.bind("<Button-1>", lambda e, sq=square: self.on_square_click(sq))
                label.bind("<Button-1>", lambda e, sq=square: self.on_square_click(sq))

    def show_player_selection(self):
        dialog = tk.Toplevel(self.root)
        dialog.title("Choose Your Color")
        dialog.geometry("350x200")
        dialog.resizable(False, False)
        dialog.configure(bg=self.panel_color)
        dialog.transient(self.root)
        dialog.grab_set()

        dialog.update_idletasks()
        x = self.root.winfo_x() + (self.root.winfo_width() // 2) - 175
        y = self.root.winfo_y() + (self.root.winfo_height() // 2) - 100
        dialog.geometry(f"+{x}+{y}")

        tk.Label(dialog, text="♔ Choose Your Side ♚",
                font=("Segoe UI", 16, "bold"), bg=self.panel_color, fg=self.text_color).pack(pady=20)
        tk.Label(dialog, text="Which color would you like to play?",
                font=("Segoe UI", 11), bg=self.panel_color, fg="#aaa").pack(pady=(0, 20))

        button_frame = tk.Frame(dialog, bg=self.panel_color)
        button_frame.pack(pady=10)

        def select_white():
            self.player_color = chess.WHITE
            self.board_flipped = False
            self.update_board_orientation()
            self.update_board()
            dialog.destroy()
            self.status_var.set("You are playing as White. Your move!")
            self.update_analysis()

        def select_black():
            self.player_color = chess.BLACK
            self.board_flipped = True
            self.update_board_orientation()
            self.update_board()
            dialog.destroy()
            self.status_var.set("You are playing as Black. White moves first.")
            self.update_analysis()

        tk.Button(button_frame, text="♔ White", font=("Segoe UI", 12, "bold"),
                 command=select_white, bg="#f0f0f0", fg="#333", width=10,
                 relief="flat", pady=8).pack(side="left", padx=10)
        tk.Button(button_frame, text="♚ Black", font=("Segoe UI", 12, "bold"),
                 command=select_black, bg="#333", fg="#f0f0f0", width=10,
                 relief="flat", pady=8).pack(side="left", padx=10)

        dialog.protocol("WM_DELETE_WINDOW", lambda: (setattr(self, 'player_color', chess.WHITE),
                                                      dialog.destroy(), self.update_analysis()))

    def move_to_descriptive(self, board, move):
        piece = board.piece_at(move.from_square)
        if piece is None:
            return str(move)

        piece_name = self.piece_names[piece.piece_type]
        from_sq = chess.square_name(move.from_square)
        to_sq = chess.square_name(move.to_square)

        captured = board.piece_at(move.to_square)
        capture_text = ""
        if captured:
            capture_text = f" captures {self.piece_names[captured.piece_type]}"

        if piece.piece_type == chess.KING:
            if move.from_square == chess.E1 and move.to_square == chess.G1:
                return "Castle Kingside (O-O)"
            elif move.from_square == chess.E1 and move.to_square == chess.C1:
                return "Castle Queenside (O-O-O)"
            elif move.from_square == chess.E8 and move.to_square == chess.G8:
                return "Castle Kingside (O-O)"
            elif move.from_square == chess.E8 and move.to_square == chess.C8:
                return "Castle Queenside (O-O-O)"

        promo_text = ""
        if move.promotion:
            promo_text = f" promotes to {self.piece_names[move.promotion]}"

        if piece.piece_type == chess.PAWN and board.is_en_passant(move):
            capture_text = " captures en passant"

        return f"{piece_name} {from_sq} → {to_sq}{capture_text}{promo_text}"

    def get_piece_text(self, piece):
        if piece is None:
            return ""
        return self.piece_unicode.get((piece.piece_type, piece.color), "?")

    def update_board(self):
        in_check = self.board.is_check()
        king_square = self.board.king(self.board.turn) if in_check else None

        for square in chess.SQUARES:
            piece = self.board.piece_at(square)
            chess_rank = chess.square_rank(square)
            chess_file = chess.square_file(square)

            is_light = (chess_file + chess_rank) % 2 == 1
            base_color = self.light_square if is_light else self.dark_square

            # Determine square color
            if square == self.selected_square:
                bg_color = self.selected_color
            elif square == king_square:
                bg_color = self.check_color
            elif self.last_move and square in [self.last_move.from_square, self.last_move.to_square]:
                bg_color = self.last_move_light if is_light else self.last_move_dark
            elif self.selected_square is not None:
                move = chess.Move(self.selected_square, square)
                promo_move = chess.Move(self.selected_square, square, promotion=chess.QUEEN)
                if move in self.board.legal_moves or promo_move in self.board.legal_moves:
                    bg_color = self.highlight_color
                else:
                    bg_color = base_color
            else:
                bg_color = base_color

            self.squares[square].config(bg=bg_color)
            self.labels[square].config(bg=bg_color)

            # Update piece with shadow effect for depth
            text = self.get_piece_text(piece)
            if piece:
                fg = "#FFFFFF" if piece.color == chess.WHITE else "#1a1a1a"
                self.labels[square].config(text=text, fg=fg, font=("Segoe UI Symbol", 38))
            else:
                self.labels[square].config(text="", fg="black")

        # Update turn indicator
        if self.player_color is not None:
            if self.board.turn == self.player_color:
                self.turn_label.config(text="✨ Your turn!")
                self.turn_frame.config(bg=self.accent_color)
                self.turn_label.config(bg=self.accent_color)
            else:
                self.turn_label.config(text="⏳ Opponent's turn")
                self.turn_frame.config(bg="#666")
                self.turn_label.config(bg="#666")
        else:
            turn = "White" if self.board.turn == chess.WHITE else "Black"
            self.turn_label.config(text=f"{turn} to move")

        # Game end states
        if self.board.is_checkmate():
            if self.player_color is not None:
                msg = "💀 Checkmate! You lost." if self.board.turn == self.player_color else "🏆 Checkmate! You won!"
                title = "Defeat!" if self.board.turn == self.player_color else "Victory!"
            else:
                winner = "Black" if self.board.turn == chess.WHITE else "White"
                msg = f"Checkmate! {winner} wins!"
                title = "Checkmate!"
            self.status_var.set(msg)
            self.show_game_over_dialog(title, msg)
        elif self.board.is_stalemate():
            self.status_var.set("🤝 Stalemate - Draw!")
            self.show_game_over_dialog("Draw!", "🤝 Stalemate - The game is a draw!")
        elif self.board.is_insufficient_material():
            self.status_var.set("🤝 Draw - Insufficient material")
            self.show_game_over_dialog("Draw!", "🤝 Insufficient material to checkmate!")
        elif self.board.is_check():
            if self.player_color is not None:
                msg = "⚠ You are in check!" if self.board.turn == self.player_color else "Check!"
            else:
                msg = f"{'White' if self.board.turn == chess.WHITE else 'Black'} is in check!"
            self.status_var.set(msg)

    def show_game_over_dialog(self, title, message):
        """Show a popup dialog when the game ends"""
        if self.game_over_shown:
            return
        self.game_over_shown = True

        dialog = tk.Toplevel(self.root)
        dialog.title(title)
        dialog.geometry("300x180")
        dialog.resizable(False, False)
        dialog.configure(bg=self.panel_color)
        dialog.transient(self.root)
        dialog.grab_set()

        # Center the dialog
        dialog.update_idletasks()
        x = self.root.winfo_x() + (self.root.winfo_width() // 2) - 150
        y = self.root.winfo_y() + (self.root.winfo_height() // 2) - 90
        dialog.geometry(f"+{x}+{y}")

        # Title
        tk.Label(dialog, text=title, font=("Segoe UI", 18, "bold"),
                bg=self.panel_color, fg="#FFD700").pack(pady=(20, 10))

        # Message
        tk.Label(dialog, text=message, font=("Segoe UI", 12),
                bg=self.panel_color, fg=self.text_color, wraplength=260).pack(pady=10)

        # Buttons
        btn_frame = tk.Frame(dialog, bg=self.panel_color)
        btn_frame.pack(pady=20)

        def new_game_and_close():
            dialog.destroy()
            self.new_game()

        def just_close():
            dialog.destroy()

        tk.Button(btn_frame, text="🎮 New Game", font=("Segoe UI", 10, "bold"),
                 command=new_game_and_close, bg=self.accent_color, fg="white",
                 relief="flat", padx=15, pady=5).pack(side="left", padx=10)
        tk.Button(btn_frame, text="Review", font=("Segoe UI", 10),
                 command=just_close, bg="#555", fg="white",
                 relief="flat", padx=15, pady=5).pack(side="left", padx=10)

    def on_square_click(self, square):
        if self.board.is_game_over():
            return

        piece = self.board.piece_at(square)

        if self.selected_square is None:
            if piece and piece.color == self.board.turn:
                self.selected_square = square
                self.status_var.set(f"Selected {chess.square_name(square)} - click destination")
                self.update_board()
        else:
            move = chess.Move(self.selected_square, square)
            if self.board.piece_at(self.selected_square) and \
               self.board.piece_at(self.selected_square).piece_type == chess.PAWN and \
               chess.square_rank(square) in [0, 7]:
                move = chess.Move(self.selected_square, square, promotion=chess.QUEEN)

            if move in self.board.legal_moves:
                self.make_move(move)
            elif piece and piece.color == self.board.turn:
                self.selected_square = square
                self.status_var.set(f"Selected {chess.square_name(square)} - click destination")
                self.update_board()
            else:
                self.selected_square = None
                self.status_var.set("Invalid move - click a piece to select")
                self.update_board()

    def make_move(self, move):
        """Execute a move and update the display"""
        descriptive = self.move_to_descriptive(self.board, move)
        self.board.push(move)
        self.last_move = move
        self.selected_square = None
        self.status_var.set(f"Played: {descriptive}")
        self.update_board()
        self.update_history()
        self.update_analysis()

    def play_best_move(self):
        """Auto-play the best move suggested by Stockfish"""
        if self.best_move and self.board.turn == self.player_color:
            if self.best_move in self.board.legal_moves:
                descriptive = self.move_to_descriptive(self.board, self.best_move)
                self.status_var.set(f"Auto-played: {descriptive}")
                self.make_move(self.best_move)
            else:
                self.status_var.set("No valid move available")
        elif self.board.turn != self.player_color:
            self.status_var.set("Wait for your turn!")
        else:
            self.status_var.set("Analyzing... please wait")

    def update_history(self):
        self.history_text.config(state="normal")
        self.history_text.delete("1.0", "end")

        temp_board = chess.Board()
        moves = list(self.board.move_stack)

        move_text = ""
        for i, move in enumerate(moves):
            is_white_move = (i % 2 == 0)
            if self.player_color is not None:
                who = "You" if (is_white_move == (self.player_color == chess.WHITE)) else "Opp"
            else:
                who = "W" if is_white_move else "B"

            san = temp_board.san(move)
            if i % 2 == 0:
                move_text += f"{i//2 + 1}. "
            move_text += f"[{who}]{san} "
            if i % 2 == 1:
                move_text += "\n"
            temp_board.push(move)

        self.history_text.insert("1.0", move_text.strip())
        self.history_text.config(state="disabled")

    def update_analysis(self):
        if self.engine is None or self.player_color is None:
            return

        def analyze():
            try:
                info = self.engine.analyse(self.board, chess.engine.Limit(depth=18))
                score = info.get("score")
                pv = info.get("pv", [])
                depth = info.get("depth", 0)

                is_player_turn = self.board.turn == self.player_color

                # Store best move for auto-play
                if pv and is_player_turn:
                    self.best_move = pv[0]
                else:
                    self.best_move = None

                # Format score
                if score:
                    if score.is_mate():
                        mate_in = score.white().mate()
                        if self.player_color == chess.WHITE:
                            score_text = f"Mate in {mate_in}!" if mate_in > 0 else f"Opponent mates in {-mate_in}"
                        else:
                            score_text = f"Mate in {-mate_in}!" if mate_in < 0 else f"Opponent mates in {mate_in}"
                    else:
                        cp = score.white().score()
                        if self.player_color == chess.BLACK:
                            cp = -cp
                        eval_score = cp / 100
                        if eval_score > 0.5:
                            score_text = f"You're ahead +{eval_score:.1f}"
                        elif eval_score < -0.5:
                            score_text = f"You're behind {eval_score:.1f}"
                        else:
                            score_text = "≈ Equal position"
                else:
                    score_text = "N/A"

                # Build analysis content
                content = f"📊 {score_text}\n"
                content += f"🔍 Depth: {depth}\n"
                content += "─" * 30 + "\n\n"

                if is_player_turn and pv:
                    desc = self.move_to_descriptive(self.board, pv[0])
                    content += f"💡 BEST MOVE:\n"
                    content += f"   {desc}\n\n"

                    if len(pv) > 1:
                        content += "📈 Expected line:\n"
                        temp = self.board.copy()
                        for i, m in enumerate(pv[:5]):
                            who = "You" if (i % 2 == 0) else "Opp"
                            d = self.move_to_descriptive(temp, m)
                            content += f"   {who}: {d}\n"
                            temp.push(m)
                elif not is_player_turn:
                    content += "⏳ OPPONENT'S TURN\n\n"
                    content += "Enter their move on the board\n"
                    content += "to see your next suggestion."

                def update_ui():
                    self.analysis_text.config(state="normal")
                    self.analysis_text.delete("1.0", "end")
                    self.analysis_text.insert("1.0", content)
                    self.analysis_text.config(state="disabled")

                self.root.after(0, update_ui)

            except Exception as e:
                def show_error():
                    self.analysis_text.config(state="normal")
                    self.analysis_text.delete("1.0", "end")
                    self.analysis_text.insert("1.0", f"Analysis error: {e}")
                    self.analysis_text.config(state="disabled")
                self.root.after(0, show_error)

        threading.Thread(target=analyze, daemon=True).start()

    def save_game(self):
        """Save the current game to a PGN file"""
        filename = filedialog.asksaveasfilename(
            defaultextension=".pgn",
            filetypes=[("PGN files", "*.pgn"), ("All files", "*.*")],
            initialfile=f"chess_game_{datetime.now().strftime('%Y%m%d_%H%M%S')}.pgn"
        )
        if filename:
            game = chess.pgn.Game()
            game.headers["Event"] = "Chess Assistant Game"
            game.headers["Date"] = datetime.now().strftime("%Y.%m.%d")
            game.headers["White"] = "Player" if self.player_color == chess.WHITE else "Opponent"
            game.headers["Black"] = "Opponent" if self.player_color == chess.WHITE else "Player"

            node = game
            temp_board = chess.Board()
            for move in self.board.move_stack:
                node = node.add_variation(move)
                temp_board.push(move)

            with open(filename, "w") as f:
                f.write(str(game))

            self.status_var.set(f"Game saved to {os.path.basename(filename)}")

    def load_game(self):
        """Load a game from a PGN file"""
        filename = filedialog.askopenfilename(
            filetypes=[("PGN files", "*.pgn"), ("All files", "*.*")]
        )
        if filename:
            try:
                with open(filename) as f:
                    game = chess.pgn.read_game(f)

                if game:
                    self.board = game.board()
                    for move in game.mainline_moves():
                        self.board.push(move)

                    if self.board.move_stack:
                        self.last_move = self.board.move_stack[-1]

                    self.selected_square = None
                    self.update_board()
                    self.update_history()
                    self.update_analysis()
                    self.status_var.set(f"Loaded: {os.path.basename(filename)}")
                else:
                    self.status_var.set("Error: Could not read PGN file")
            except Exception as e:
                self.status_var.set(f"Error loading file: {e}")

    def new_game(self):
        self.board = chess.Board()
        self.selected_square = None
        self.player_color = None
        self.board_flipped = False
        self.last_move = None
        self.best_move = None
        self.game_over_shown = False
        self.update_board_orientation()
        self.status_var.set("New game - Choose your color")
        self.update_board()
        self.update_history()
        self.show_player_selection()

    def undo_move(self):
        if self.board.move_stack:
            self.board.pop()
            self.last_move = self.board.move_stack[-1] if self.board.move_stack else None
            self.selected_square = None
            self.status_var.set("Move undone")
            self.update_board()
            self.update_history()
            self.update_analysis()

    def cleanup(self):
        if self.engine:
            self.engine.quit()


def main():
    root = tk.Tk()
    root.resizable(False, False)
    root.configure(bg="#2b2b2b")

    app = ChessGUI(root)

    def on_closing():
        app.cleanup()
        root.destroy()

    root.protocol("WM_DELETE_WINDOW", on_closing)
    root.mainloop()


if __name__ == "__main__":
    main()
