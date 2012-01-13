/** Data structure to record a move. This class is used in unit test. */
class TestMove {
	private String input;
	private boolean whiteNext;
	private boolean check;
	private Piece captured;
	private String promoteTo;

	public TestMove(String input, String promoteTo, boolean whiteNext, boolean check, Piece captured) {
		this.input = input;
		this.promoteTo = promoteTo;
		this.whiteNext = whiteNext;
		this.check = check;
		this.captured = captured;
	};
	public String Input() { return input; };
	public String PromoteTo() { return promoteTo; };
	public boolean WhiteNext() { return whiteNext; };
	public boolean Check() { return check; };
	public Piece Captured() { return captured; };
}
