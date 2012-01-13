/** Location as an ordered pair of integers */
public class Location {
	private int file; // Columns, numbered 0-7
	private int rank; // Rows, numbered 0-7

	public Location(int file_, int rank_) {
		file = file_;
		rank = rank_;
	}

	public int File() { return file; }
	public int Rank() { return rank; }

	public boolean Valid() {
		return (file >= 1 && file <= 7 && rank >=1 && rank <= 7);
	}
	public boolean IsEqual(Location a) {
		return (file == a.File() && rank == a.Rank());
	}
	public static Location Add(Location a, Location b) {
		return new Location(a.File() + b.File(), a.Rank() + b.Rank());
	}
	public static Location Middle(Location a, Location b) {
		return new Location((a.File() + b.File())/2, (a.Rank() + b.Rank())/2);
	}
}
