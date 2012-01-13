/* ChessBoard.java
 * by Adrian S. Tam <adrian.sw.tam@gmail.com>
 */

/** The chess board.
 *
 *  It holds the 8x8 checker board and the pieces on it. It can print the
 *  current status to the console (System.out) and also allows one to
 *  manipulate the chess board. The manipulation does not verify whether it is
 *  lawful or not. Such logic shall be implemented elsewhere and checked before
 *  invoking the chess board.
 */
class ChessBoard {
	private Piece[][] board = new Piece[8][8]; //< The chess board
	private boolean gameEnd;

	/** Print the Chess board: Whites are in uppercases, black are lowercases */
	public void Print() {
		System.out.println("   A   B   C   D   E   F   G   H");
		System.out.println(" +---+---+---+---+---+---+---+---+");
		for (int i = 8; i >= 1; --i) {
			System.out.print(i + "|");
			for (int j = 0; j < 8; ++j) {
				System.out.print(" " + (board[i-1][j]).GetCode() + " |");
			}
			System.out.println(i + "\n +---+---+---+---+---+---+---+---+");
		}
		System.out.println("   A   B   C   D   E   F   G   H");
	}

	/** \returns whether this game finished */
	public boolean End() { return gameEnd; }

	/** \returns The piece at the specified location of the chessboard */
	public Piece GetPieceAt(int i, int j) {
		return board[i][j];
	};

	/** \returns The piece at the specified location of the chessboard */
	public Piece GetPieceAt(Location loc) {
		return GetPieceAt(loc.Rank(), loc.File());
	};

	/** \returns The square at the specified loation is unoccupied */
	public boolean EmptyAt(Location loc) {
		return GetPieceAt(loc) == Piece.NOTHING;
	}

	/** \returns The square at the specified loation is unoccupied */
	public boolean EmptyAt(int i, int j) {
		return GetPieceAt(i,j) == Piece.NOTHING;
	}

	/** Move a piece */
	public Piece Move(Location from, Location to) {
		Piece captured = board[to.Rank()][to.File()];
		if (Character.toLowerCase(captured.GetCode()) == 'k') {
			gameEnd = true;
		};
		board[to.Rank()][to.File()] = board[from.Rank()][from.File()];
		board[from.Rank()][from.File()] = Piece.NOTHING;
		return captured;
	}

	/** Remove a piece */
	public void RemoveAt(Location loc) {
		board[loc.Rank()][loc.File()] = Piece.NOTHING;
	}

	/** Promote a pawn to a queen, rook, bishop, or knight */
	public boolean PromoteAt(Location loc, char code) {
		// Sanity check, could be avoided as this is done also by class ChessGame
		if (Character.toLowerCase(board[loc.Rank()][loc.File()].GetCode()) != 'p') { return false; }
		// Replace the piece at the chessboard
		if (board[loc.Rank()][loc.File()].IsWhite()) {
			switch (code) {
				case 'q': board[loc.Rank()][loc.File()] = Piece.WQUEEN;
				          return true;
				case 'r': board[loc.Rank()][loc.File()] = Piece.WROOK;
				          return true;
				case 'n': board[loc.Rank()][loc.File()] = Piece.WKNIGHT;
				          return true;
				case 'b': board[loc.Rank()][loc.File()] = Piece.WBISHOP;
				          return true;
				default: return false;
			}
		} else {
			switch (code) {
				case 'q': board[loc.Rank()][loc.File()] = Piece.BQUEEN;
				          return true;
				case 'r': board[loc.Rank()][loc.File()] = Piece.BROOK;
				          return true;
				case 'n': board[loc.Rank()][loc.File()] = Piece.BKNIGHT;
				          return true;
				case 'b': board[loc.Rank()][loc.File()] = Piece.BBISHOP;
				          return true;
				default: return false;
			}
		}
	}

	/** Initialize the chessboard */
	public void Initialize() {
		// Initialize the board
		for (int i = 2; i < 6; ++i) {
			for (int j = 0; j < 8; ++j) {
				board[i][j] = Piece.NOTHING;
			}
		}
		gameEnd = false;
		// Position the pieces: Whites
		board[0][0] = Piece.WROOK;
		board[0][1] = Piece.WKNIGHT;
		board[0][2] = Piece.WBISHOP;
		board[0][3] = Piece.WQUEEN;
		board[0][4] = Piece.WKING;
		board[0][5] = Piece.WBISHOP;
		board[0][6] = Piece.WKNIGHT;
		board[0][7] = Piece.WROOK;
		board[1][0] = Piece.WPAWN;
		board[1][1] = Piece.WPAWN;
		board[1][2] = Piece.WPAWN;
		board[1][3] = Piece.WPAWN;
		board[1][4] = Piece.WPAWN;
		board[1][5] = Piece.WPAWN;
		board[1][6] = Piece.WPAWN;
		board[1][7] = Piece.WPAWN;
		// Position the pieces: Blacks
		board[6][0] = Piece.BPAWN;
		board[6][1] = Piece.BPAWN;
		board[6][2] = Piece.BPAWN;
		board[6][3] = Piece.BPAWN;
		board[6][4] = Piece.BPAWN;
		board[6][5] = Piece.BPAWN;
		board[6][6] = Piece.BPAWN;
		board[6][7] = Piece.BPAWN;
		board[7][0] = Piece.BROOK;
		board[7][1] = Piece.BKNIGHT;
		board[7][2] = Piece.BBISHOP;
		board[7][3] = Piece.BQUEEN;
		board[7][4] = Piece.BKING;
		board[7][5] = Piece.BBISHOP;
		board[7][6] = Piece.BKNIGHT;
		board[7][7] = Piece.BROOK;
	}

	/** Return the end-game of Kasparov vs the world for unit test purpose */
	public static ChessBoard KasparovEnd() {
		ChessBoard cb = new ChessBoard();
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				cb.board[i][j] = Piece.NOTHING;
			}
		}
		cb.board[0][2] = Piece.BKING;
		cb.board[1][5] = Piece.WQUEEN;
		cb.board[3][3] = Piece.BPAWN;
		cb.board[3][4] = Piece.BQUEEN;
		cb.board[5][5] = Piece.WKING;
		cb.board[6][6] = Piece.WPAWN;
		return cb;
	}

	/** Verify if this chess board has the exact appearance with another one */
	public boolean Equals(ChessBoard b) {
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				if (board[i][j] != b.board[i][j]) { return false; }
			}
		}
		return true;
	}
}
