import java.io.Console;

/** The main program as well as the chess game logic.
 *  The logic of the game is defined here, namely, a move is valid or not, it
 *  is which player's turn, whether the king is in check, etc.
 */
class ChessGame {
	// State variables of the game
	private ChessBoard cb;
	private boolean whiteMove;
	private Console console;
	private boolean[] whitePawnMoved;
	private boolean[] blackPawnMoved;
	private boolean whiteARookMoved;
	private boolean whiteHRookMoved;
	private boolean whiteKingMoved;
	private boolean blackARookMoved;
	private boolean blackHRookMoved;
	private boolean blackKingMoved;
	private boolean inCheck;
	private Location enPassantLocation;
	private Piece captured;

	// Error code of invalid inputs
	private final int INVALID_LOCATION = 1;
	private final int INVALID_INPUT = 2;
	private final int INVALID_PIECE = 3;
	private final int INVALID_DESTINATION = 4;
	private final int INVALID_MOVE = 5;

	/** \returns The location has a piece belong to the current player */
	private boolean ValidFrom(Location loc) {
		Piece p = cb.GetPieceAt(loc);
		return (whiteMove)? p.IsWhite(): p.IsBlack();
	}

	/** \returns The location does not have a piece belong to the current player */
	private boolean ValidTo(Location loc) {
		return (!ValidFrom(loc));
	}

	/** Check if a rook has a lawful move */
	private boolean ValidRookMove(Location from, Location to) {
		if (from.Rank() == to.Rank()) {
			/* Not leap over other pieces if moving vertically */
			int i = Math.min(from.File(), to.File());
			int j = Math.max(from.File(), to.File());
			for (++i; i != j; ++i) {
				if (!cb.EmptyAt(from.Rank(),i)) { return false; }
			}
		} else if (from.File() == to.File()) {
			/* Not leap over other pieces if moving horizontally */
			int i = Math.min(from.Rank(), to.Rank());
			int j = Math.max(from.Rank(), to.Rank());
			for (++i; i != j; ++i) {
				if (!cb.EmptyAt(i, from.File())) { return false; }
			}
		} else {
			/* Rook can move horizontally or vertically */
			return false;
		}
		return true;
	}

	/** Check if a bishop has a lawful move */
	private boolean ValidBishopMove(Location from, Location to) {
		int dr = (to.Rank() > from.Rank()) ? 1 : -1;
		int df = (to.File() > from.File()) ? 1 : -1;
		if (Math.abs(from.Rank() - to.Rank()) != Math.abs(from.File() - to.File())) {
			/* Bishop can only move diagonally */
			return false;
		}
		/* Must not leap over other pieces when moving */
		Location delta = new Location(df, dr);
		for (Location i = Location.Add(from, delta); ! i.IsEqual(to); i = Location.Add(i, delta)) {
			if (!cb.EmptyAt(i)) { return false; }
		}
		return true;
	}

	/** Check if a pawn has a lawful move */
	private boolean ValidPawnMove(Location from, Location to) {
		/* Pawn can only move forward */
		if (to.Rank() < from.Rank() && whiteMove) { return false; }
		if (to.Rank() > from.Rank() && !whiteMove) { return false; }
		/* May advance 2 squares on its first move provided both squares are unoccupied */
		if (Math.abs(from.Rank() - to.Rank()) == 2) {
			if (from.File() != to.File()) { return false; }
			if ((whiteMove) ? whitePawnMoved[to.File()] : blackPawnMoved[to.File()]) {
				return false;
			}
			return (cb.EmptyAt(Location.Middle(from, to)) && cb.EmptyAt(to));
		}
		/* Pawn may capture en passant */
		if (enPassantLocation != null &&
		    enPassantLocation.Rank() == from.Rank() &&
		    enPassantLocation.File() == to.File() &&
		    Math.abs(from.Rank() - to.Rank()) == 1 &&
		    Math.abs(from.File() - to.File()) == 1) {
			return true;
		}
		/* Normally pawn can only move forward for one rank */
		if (Math.abs(from.Rank() - to.Rank()) != 1) { return false; }
		/* Pawn can move forward only if not capture, or diagonally only if capture */
		if (Math.abs(from.File() - to.File()) == 1) {
			return (! cb.EmptyAt(to));
		} else if (Math.abs(from.File() - to.File()) == 0) {
			return cb.EmptyAt(to);
		} else {
			return false;
		}
	}

	/** Check if a king has a lawful move */
	private boolean ValidKingMove(Location from, Location to) {
		/* King can move to any of the 8 neighbouring square */
		if (Math.abs(from.Rank() - to.Rank()) <= 1 &&
		    Math.abs(from.File() - to.File()) <= 1) {
			return true;
		}
		/* King may also do castling */
		if (from.Rank() == to.Rank() && Math.abs(from.File() - to.File()) == 2) {
			/* The king must not been moved */
			if (whiteMove && whiteKingMoved) { return false; };
			if (!whiteMove && blackKingMoved) { return false; };
			/* The rook must not been moved */
			Location delta = new Location(0,0); // mute compiler
			if (to.File() == 2) { // King from E to C
				if (whiteMove && whiteARookMoved) { return false; };
				if (!whiteMove && blackARookMoved) { return false; };
				delta = new Location(-1,0);
			} else if (to.File() == 6) { // King from E to G
				if (whiteMove && whiteHRookMoved) { return false; };
				if (!whiteMove && blackHRookMoved) { return false; };
				delta = new Location(1,0);
			}
			/* Nothing shall be between the king and rook */
			for (Location i = Location.Add(from, delta); ! i.IsEqual(to); i = Location.Add(i, delta)) {
				if (!cb.EmptyAt(i)) { return false; }
			}
			/* King must not in check nor to be in check, nor pass through the square under attack */
			if (inCheck == true || UnderAttack(to) || UnderAttack(Location.Middle(from, to))) {
				return false;
			};
			return true;
		}
		return false;
	}

	/** Check, for each type of piece, the move is lawful */
	private boolean ValidMove(Location from, Location to) {
		Piece p = cb.GetPieceAt(from);
		switch (Character.toLowerCase(p.GetCode())) {
			case 'k':
				return ValidKingMove(from, to);
			case 'q':
				/* Queen can move like a rook or a bishop */
				return (ValidRookMove(from, to) ||
				        ValidBishopMove(from, to));
			case 'r':
				return ValidRookMove(from, to);
			case 'b':
				return ValidBishopMove(from, to);
			case 'n':
				/* Knight can move two rank & one file or two file & one rank */
				int RankDelta = Math.abs(from.Rank() - to.Rank());
				int FileDelta = Math.abs(from.File() - to.File());
				return ((RankDelta * FileDelta == 2) && (RankDelta + FileDelta == 3));
			case 'p':
				return ValidPawnMove(from, to);
		}
		return false; // mute compiler
	}

	/** Parse the input and check if the move is good.
	 *  \returns 0 if the move is good, or an error code otherwise
	 */
	private int Validate(String move) {
		String[] tokens = move.split("\\s+");
		String fromStr = "", toStr = ""; // init to mute compiler
		int tokensCount = 0;
		/* Tokenize the input into locations */
		for (String s : tokens) {
			if (s.length() > 0) {
				/* Verify location s is in correct format */
				if (! s.matches("[a-hA-H][1-8]")) { return INVALID_LOCATION; }
				if (0 == tokensCount++) {
					fromStr = s.toLowerCase();
				} else {
					toStr = s.toLowerCase();
				}
			}
		}
		if (tokensCount != 2) { return INVALID_INPUT; };
		Location from = new Location(fromStr.charAt(0) - 'a', fromStr.charAt(1) - '1');
		Location to = new Location(toStr.charAt(0) - 'a', toStr.charAt(1) - '1');
		/* Verify the `from' location has a piece that belongs to this player */
		if (!ValidFrom(from)) { return INVALID_PIECE; }
		/* Verify the `to' location is empty or belong to an opponent */
		if (!ValidTo(to)) { return INVALID_DESTINATION; }
		/* Verify the move follows the rule */
		if (!ValidMove(from, to)) { return INVALID_MOVE; }
		return 0;
	}

	/** \returns if the provided location is under attack by the opponent  */
	private boolean UnderAttack(Location loc) {
		Location nextFile = new Location(1,0);
		Location cursor = new Location(0,0);
		for (;cursor.Rank() < 8; cursor = new Location(0, cursor.Rank() + 1)) {
			for (; cursor.File() < 8; cursor = Location.Add(cursor, nextFile)) {
				if (cb.EmptyAt(cursor) ||
				    cb.GetPieceAt(cursor).IsWhite() == whiteMove) {
					// Check next square if it is empty or belong to same player
					continue;
				} else if (ValidMove(cursor, loc)) {
					// The piece in cursor belongs to the opponent, and it is a valid move
					return true;
				}
			}
		}
		return false;
	}

	/** \returns the location of the opponent's king */
	private Location OpponentKing() {
		Location nextFile = new Location(1,0);
		Location king = new Location(0,0);
		boolean found = false;
		for (;king.Rank() < 8; king = new Location(0, king.Rank() + 1)) {
			for (; king.File() < 8; king = Location.Add(king, nextFile)) {
				// Scan every square for the king
				if (Character.toLowerCase(cb.GetPieceAt(king).GetCode()) == 'k' &&
				    cb.GetPieceAt(king).IsWhite() != whiteMove) {
					// Opponent's king found
					found = true;
					break;
				} else {
					continue;
				}
			}
			if (found) break;
		}
		return king;
	}

	/** \returns whether the current player made a checkmate */
	private boolean Checkmate() {
		Location nextFile = new Location(1,0);
		Location cursor = new Location(0,0);
		Location king = OpponentKing();
		for (;cursor.Rank() < 8; cursor = new Location(0, cursor.Rank() + 1)) {
			for (; cursor.File() < 8; cursor = Location.Add(cursor, nextFile)) {
				// Check if the current player has a piece at cursor and it can attack the king
				if (!cb.EmptyAt(cursor) &&
				    cb.GetPieceAt(cursor).IsWhite() == whiteMove &&
				    ValidMove(cursor, king)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Parse the input and check if the move is good.
	 *  \returns 0 if the move is good, or an error code otherwise
	 */
	private void MakeMove(String move) { MakeMove(move, ""); }
	private void MakeMove(String move, String promoteTo) {
		String[] tokens = move.split("\\s+");
		String fromStr = "", toStr = ""; // init to mute compiler
		int tokensCount = 0;
		/* Tokenize the input into locations */
		for (String s : tokens) {
			if (s.length() > 0) {
				if (0 == tokensCount++) {
					fromStr = s.toLowerCase();
				} else {
					toStr = s.toLowerCase();
				}
			}
		}
		Location from = new Location(fromStr.charAt(0) - 'a', fromStr.charAt(1) - '1');
		Location to = new Location(toStr.charAt(0) - 'a', toStr.charAt(1) - '1');
		/* Ask the chess board to move it */
		captured = cb.Move(from, to);
		/* Special handling for pawns */
		char code = cb.GetPieceAt(to).GetCode();
		if (Character.toLowerCase(code) == 'p') {
			/* Handle en passant */
			if (enPassantLocation != null && from.File() != to.File()) {
				if (to.File() == enPassantLocation.File() && from.Rank() == enPassantLocation.Rank()) {
					cb.RemoveAt(enPassantLocation);
				}
			}
			enPassantLocation = null;
			/* Mark the first moves */
			if (code == 'p' && from.Rank() == 6) {
				blackPawnMoved[from.File()] = true;
			} else if (code == 'P' && from.Rank() == 1) {
				whitePawnMoved[from.File()] = true;
			}
			/* Handle promotion */
			if (to.Rank() == 0 || to.Rank() == 7) {
				for (;;) {
					String p;
					if (promoteTo.length() == 1) {
						p = promoteTo;
					} else {
						p = console.readLine("Promote the pawn to a [Q]ueen, [R]ook, [B]ishop, or k[N]ight? ").toLowerCase();
					}
					if (p.equals("q") || p.equals("r") || p.equals("b") || p.equals("n")) {
						cb.PromoteAt(to, p.charAt(0));
						break;
					} else {
						console.printf("Input only `Q', `R', `B', or `N', not %s\n", p);
					}
				}
			}
			/* Opponent may make en passant on next move */
			if (from.File() == to.File() && Math.abs(from.Rank() - to.Rank()) == 2) {
				enPassantLocation = to;
			}
		} else {
			enPassantLocation = null;
		}
		/* Special handling for castling */
		if (code == 'k') {
			blackKingMoved = true;
			if (Math.abs(from.File() - to.File()) == 2) {
				if (to.File() == 2) {
					cb.Move(new Location(0,7), new Location(3,7));
					blackARookMoved = true;
				} else if (to.File() == 6) {
					cb.Move(new Location(7,7), new Location(5,7));
					blackHRookMoved = true;
				}
			}
		} else if (code == 'K') {
			whiteKingMoved = true;
			if (Math.abs(from.File() - to.File()) == 2) {
				if (to.File() == 2) {
					cb.Move(new Location(0,0), new Location(3,0));
					whiteARookMoved = true;
				} else if (to.File() == 6) {
					cb.Move(new Location(7,0), new Location(5,0));
					whiteHRookMoved = true;
				}
			}
		}
		/* Mark initial rook moves */
		if (code == 'n') {
			if (from.File() == 0) { blackARookMoved = true; }
			if (from.File() == 7) { blackHRookMoved = true; }
		} else if (code == 'N') {
			if (from.File() == 0) { whiteARookMoved = true; }
			if (from.File() == 7) { whiteHRookMoved = true; }
		}
	}

	/** Constructor to set up console */
	private ChessGame () {
		console = System.console();
		if (console == null) {
			System.err.println("No console.");
			System.exit(1);
		}
	}

	/** High-level controller of the chess game.
	 *  It takes input from user, validate it, do the move, and repeat.
	 */
	private void StartGame() {
		// Prepare the chess board
		cb = new ChessBoard();
		// Initialization
		cb.Initialize();
		whitePawnMoved = new boolean[8];
		blackPawnMoved = new boolean[8];
		for (int i=0; i<8; ++i) {
			whitePawnMoved[i] = blackPawnMoved[i] = false;
		}
		blackKingMoved = false;
		blackARookMoved = false;
		blackHRookMoved = false;
		whiteKingMoved = false;
		whiteARookMoved = false;
		whiteHRookMoved = false;
		inCheck = false;
		whiteMove = true;
		captured = Piece.NOTHING;
		// Repeat until game finished
		while(! cb.End()) {
			cb.Print();
			if (captured.GetCode() != ' ') {
				console.printf("Captured %c\n", captured.GetCode());
			}
			if (inCheck) {
				console.printf("Checkmate!\n");
			}
			String move = console.readLine((whiteMove?"White":"Black") + " player, type your move (e.g. 'a2 a3'): ");
			switch (Validate(move)) {
				case 0:
					MakeMove(move);
					inCheck = Checkmate();
					whiteMove = ! whiteMove;
					break;
				case INVALID_LOCATION:
					console.printf("invalid location format\n");
					break;
				case INVALID_INPUT:
					console.printf("invalid input\n");
					break;
				case INVALID_PIECE:
					console.printf("you have to move a piece that belongs to you\n");
					break;
				case INVALID_DESTINATION:
					console.printf("you cannot capture your own piece\n");
					break;
				case INVALID_MOVE:
					console.printf("unlawful move\n");
					break;
			}
		}
		console.printf("Game finished." + (whiteMove?"Black":"White") + " won.");
	}

	/** Verify the game running correctly using Kasparov vs the World.
	 *  Source: http://en.wikipedia.org/wiki/Kasparov_versus_the_World
	 *  This function has the same structure as StartGame().
	 */
	public boolean UnitTest() {
		// Prepare the chess board
		cb = new ChessBoard();
		// Initialization
		cb.Initialize();
		whitePawnMoved = new boolean[8];
		blackPawnMoved = new boolean[8];
		for (int i=0; i<8; ++i) {
			whitePawnMoved[i] = blackPawnMoved[i] = false;
		}
		blackKingMoved = false;
		blackARookMoved = false;
		blackHRookMoved = false;
		whiteKingMoved = false;
		whiteARookMoved = false;
		whiteHRookMoved = false;
		inCheck = false;
		whiteMove = true;
		captured = Piece.NOTHING;
		// The moves of Kasparov vs the World
		TestMove[] moves = {
			new TestMove("e2 e4", "", false, false, Piece.NOTHING), // 1
			new TestMove("c7 c5", "",  true, false, Piece.NOTHING),
			new TestMove("g1 f3", "", false, false, Piece.NOTHING), // 2
			new TestMove("d7 d6", "",  true, false, Piece.NOTHING),
			new TestMove("f1 b5", "", false,  true, Piece.NOTHING), // 3
			new TestMove("c8 d7", "",  true, false, Piece.NOTHING),
			new TestMove("b5 d7", "", false,  true, Piece.BBISHOP), // 4
			new TestMove("d8 d7", "",  true, false, Piece.WBISHOP),
			new TestMove("c2 c4", "", false, false, Piece.NOTHING), // 5
			new TestMove("b8 c6", "",  true, false, Piece.NOTHING),
			new TestMove("b1 c3", "", false, false, Piece.NOTHING), // 6
			new TestMove("g8 f6", "",  true, false, Piece.NOTHING),
			new TestMove("e1 g1", "", false, false, Piece.NOTHING), // 7
			new TestMove("g7 g6", "",  true, false, Piece.NOTHING),
			new TestMove("d2 d4", "", false, false, Piece.NOTHING), // 8
			new TestMove("c5 d4", "",  true, false, Piece.WPAWN),
			new TestMove("f3 d4", "", false, false, Piece.BPAWN),   // 9
			new TestMove("f8 g7", "",  true, false, Piece.NOTHING),
			new TestMove("d4 e2", "", false, false, Piece.NOTHING), // 10
			new TestMove("d7 e6", "",  true, false, Piece.NOTHING),
			new TestMove("c3 d5", "", false, false, Piece.NOTHING), // 11
			new TestMove("e6 e4", "",  true, false, Piece.WPAWN),
			new TestMove("d5 c7", "", false,  true, Piece.NOTHING), // 12
			new TestMove("e8 d7", "",  true, false, Piece.NOTHING),
			new TestMove("c7 a8", "", false, false, Piece.BROOK),   // 13
			new TestMove("e4 c4", "",  true, false, Piece.WPAWN),
			new TestMove("a8 b6", "", false,  true, Piece.NOTHING), // 14
			new TestMove("a7 b6", "",  true, false, Piece.WKNIGHT),
			new TestMove("e2 c3", "", false, false, Piece.NOTHING), // 15
			new TestMove("h8 a8", "",  true, false, Piece.NOTHING),
			new TestMove("a2 a4", "", false, false, Piece.NOTHING), // 16
			new TestMove("f6 e4", "",  true, false, Piece.NOTHING),
			new TestMove("c3 e4", "", false, false, Piece.BKNIGHT), // 17
			new TestMove("c4 e4", "",  true, false, Piece.WKNIGHT),
			new TestMove("d1 b3", "", false, false, Piece.NOTHING), // 18
			new TestMove("f7 f5", "",  true, false, Piece.NOTHING),
			new TestMove("c1 g5", "", false, false, Piece.NOTHING), // 19
			new TestMove("e4 b4", "",  true, false, Piece.NOTHING),
			new TestMove("b3 f7", "", false, false, Piece.NOTHING), // 20
			new TestMove("g7 e5", "",  true, false, Piece.NOTHING),
			new TestMove("h2 h3", "", false, false, Piece.NOTHING), // 21
			new TestMove("a8 a4", "",  true, false, Piece.WPAWN),
			new TestMove("a1 a4", "", false, false, Piece.BROOK),   // 22
			new TestMove("b4 a4", "",  true, false, Piece.WROOK),
			new TestMove("f7 h7", "", false, false, Piece.BPAWN),   // 23
			new TestMove("e5 b2", "",  true, false, Piece.WPAWN),
			new TestMove("h7 g6", "", false, false, Piece.BPAWN),   // 24
			new TestMove("a4 e4", "",  true, false, Piece.NOTHING),
			new TestMove("g6 f7", "", false, false, Piece.NOTHING), // 25
			new TestMove("b2 d4", "",  true, false, Piece.NOTHING),
			new TestMove("f7 b3", "", false, false, Piece.NOTHING), // 26
			new TestMove("f5 f4", "",  true, false, Piece.NOTHING),
			new TestMove("b3 f7", "", false, false, Piece.NOTHING), // 27
			new TestMove("d4 e5", "",  true, false, Piece.NOTHING),
			new TestMove("h3 h4", "", false, false, Piece.NOTHING), // 28
			new TestMove("b6 b5", "",  true, false, Piece.NOTHING),
			new TestMove("h4 h5", "", false, false, Piece.NOTHING), // 29
			new TestMove("e4 c4", "",  true, false, Piece.NOTHING),
			new TestMove("f7 f5", "", false,  true, Piece.NOTHING), // 30
			new TestMove("c4 e6", "",  true, false, Piece.NOTHING),
			new TestMove("f5 e6", "", false,  true, Piece.BQUEEN),  // 31
			new TestMove("d7 e6", "",  true, false, Piece.WQUEEN),
			new TestMove("g2 g3", "", false, false, Piece.NOTHING), // 32
			new TestMove("f4 g3", "",  true, false, Piece.WPAWN),
			new TestMove("f2 g3", "", false, false, Piece.BPAWN),   // 33
			new TestMove("b5 b4", "",  true, false, Piece.NOTHING),
			new TestMove("g5 f4", "", false, false, Piece.NOTHING), // 34
			new TestMove("e5 d4", "",  true,  true, Piece.NOTHING),
			new TestMove("g1 h1", "", false, false, Piece.NOTHING), // 35
			new TestMove("b4 b3", "",  true, false, Piece.NOTHING),
			new TestMove("g3 g4", "", false, false, Piece.NOTHING), // 36
			new TestMove("e6 d5", "",  true, false, Piece.NOTHING),
			new TestMove("g4 g5", "", false, false, Piece.NOTHING), // 37
			new TestMove("e7 e6", "",  true, false, Piece.NOTHING),
			new TestMove("h5 h6", "", false, false, Piece.NOTHING), // 38
			new TestMove("c6 e7", "",  true, false, Piece.NOTHING),
			new TestMove("f1 d1", "", false, false, Piece.NOTHING), // 39
			new TestMove("e6 e5", "",  true, false, Piece.NOTHING),
			new TestMove("f4 e3", "", false, false, Piece.NOTHING), // 40
			new TestMove("d5 c4", "",  true, false, Piece.NOTHING),
			new TestMove("e3 d4", "", false, false, Piece.BBISHOP), // 41
			new TestMove("e5 d4", "",  true, false, Piece.WBISHOP),
			new TestMove("h1 g2", "", false, false, Piece.NOTHING), // 42
			new TestMove("b3 b2", "",  true, false, Piece.NOTHING),
			new TestMove("g2 f3", "", false, false, Piece.NOTHING), // 43
			new TestMove("c4 c3", "",  true, false, Piece.NOTHING),
			new TestMove("h6 h7", "", false, false, Piece.NOTHING), // 44
			new TestMove("e7 g6", "",  true, false, Piece.NOTHING),
			new TestMove("f3 e4", "", false, false, Piece.NOTHING), // 45
			new TestMove("c3 c2", "",  true, false, Piece.NOTHING),
			new TestMove("d1 h1", "", false, false, Piece.NOTHING), // 46
			new TestMove("d4 d3", "",  true, false, Piece.NOTHING),
			new TestMove("e4 f5", "", false, false, Piece.NOTHING), // 47
			new TestMove("b2 b1","q",  true, false, Piece.NOTHING),
			new TestMove("h1 b1", "", false, false, Piece.BQUEEN),  // 48
			new TestMove("c2 b1", "",  true, false, Piece.WROOK),
			new TestMove("f5 g6", "", false, false, Piece.BKNIGHT), // 49
			new TestMove("d3 d2", "",  true, false, Piece.NOTHING),
			new TestMove("h7 h8","q", false, false, Piece.NOTHING), // 50
			new TestMove("d2 d1","q",  true, false, Piece.NOTHING),
			new TestMove("h8 h7", "", false, false, Piece.NOTHING), // 51
			new TestMove("b7 b5", "",  true, false, Piece.NOTHING),
			new TestMove("g6 f6", "", false,  true, Piece.NOTHING), // 52
			new TestMove("b1 b2", "",  true, false, Piece.NOTHING),
			new TestMove("h7 h2", "", false,  true, Piece.NOTHING), // 53
			new TestMove("b2 a1", "",  true, false, Piece.NOTHING),
			new TestMove("h2 f4", "", false, false, Piece.NOTHING), // 54
			new TestMove("b5 b4", "",  true, false, Piece.NOTHING),
			new TestMove("f4 b4", "", false, false, Piece.BPAWN),   // 55
			new TestMove("d1 f3", "",  true,  true, Piece.NOTHING),
			new TestMove("f6 g7", "", false, false, Piece.NOTHING), // 56
			new TestMove("d6 d5", "",  true, false, Piece.NOTHING),
			new TestMove("b4 d4", "", false,  true, Piece.NOTHING), // 57
			new TestMove("a1 b1", "",  true, false, Piece.NOTHING),
			new TestMove("g5 g6", "", false, false, Piece.NOTHING), // 58
			new TestMove("f3 e4", "",  true, false, Piece.NOTHING),
			new TestMove("d4 g1", "", false,  true, Piece.NOTHING), // 59
			new TestMove("b1 b2", "",  true, false, Piece.NOTHING),
			new TestMove("g1 f2", "", false,  true, Piece.NOTHING), // 60
			new TestMove("b2 c1", "",  true, false, Piece.NOTHING),
			new TestMove("g7 f6", "", false, false, Piece.NOTHING), // 61
			new TestMove("d5 d4", "",  true, false, Piece.NOTHING),
			new TestMove("g6 g7", "", false, false, Piece.NOTHING), // 62
		};
		// Repeat until game finished
		for (int i=0; i < moves.length; ++i) {
			cb.Print();
			if (captured.GetCode() != ' ') {
				System.out.printf("Captured %c\n", captured.GetCode());
			}
			if (inCheck) {
				System.out.printf("Checkmate!\n");
			}
			System.out.println((whiteMove?"White":"Black") + " player, type your move (e.g. 'a2 a3'): " + moves[i].Input());
			switch (Validate(moves[i].Input())) {
				case 0:
					MakeMove(moves[i].Input(), moves[i].PromoteTo());
					inCheck = Checkmate();
					whiteMove = ! whiteMove;
					break;
				case INVALID_LOCATION:
					console.printf("invalid location format\n");
					break;
				case INVALID_INPUT:
					console.printf("invalid input\n");
					break;
				case INVALID_PIECE:
					console.printf("you have to move a piece that belongs to you\n");
					break;
				case INVALID_DESTINATION:
					console.printf("you cannot capture your own piece\n");
					break;
				case INVALID_MOVE:
					console.printf("unlawful move\n");
					break;
			}
			if (whiteMove != moves[i].WhiteNext() ||
			    inCheck != moves[i].Check() ||
			    captured != moves[i].Captured()) {
				System.out.printf("Test failed at move %d\n", i);
				return false;
			}
		}
		// Check end state
		if (! cb.Equals(ChessBoard.KasparovEnd())) {
			System.out.println("Test failed at final state");
			return false;
		}
		return true;
	}

	/** main program for console execution */
	public static void main(String[] args) {
		ChessGame game = new ChessGame();
		if (args.length == 1 && args[0].equals("unittest")) {
			System.out.println(game.UnitTest()?"Success":"Failed");
			return;
		}
		game.StartGame();
	}

}
