public class Dice
{
	private int sides;

	public Dice(int s) {
		sides = s;
	}

	public int roll() {
		return (int)((Math.random() * sides)) + 1;
	}
}