/** Chess pieces as enum type */
public enum Piece {
	NOTHING,
	WKING,
	WQUEEN,
	WROOK,
	WKNIGHT,
	WBISHOP,
	WPAWN,
	BKING,
	BQUEEN,
	BROOK,
	BKNIGHT,
	BBISHOP,
	BPAWN;

	private static final String code = " KQRNBPkqrnbp"; //< Code to display to represent a piece

	/** Return the one-character code according to my value */
	public char GetCode() { return code.charAt(this.ordinal()); }

	public boolean IsBlack() {
		if (this == NOTHING) { return false; }
		return (this.ordinal() >= BKING.ordinal());
	}
	public boolean IsWhite() {
		if (this == NOTHING) { return false; }
		return (this.ordinal() <= WPAWN.ordinal());
	}
}
