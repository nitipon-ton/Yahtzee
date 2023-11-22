// Class that creates a dice with "sides" number of sides
public class Dice
{
	//Attributes of our dice
	private int sides;
	
	//Constructor that builds the dice
	public Dice(int s)
	{
		sides=s;
	}
	//End Constructor
	
	//Methods for Dice
	public int roll()
	{
		return (int)((Math.random()*sides))+1;
	}
	
}