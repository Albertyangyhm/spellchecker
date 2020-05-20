//I have neither given nor received any unauthorized aid on this assignment
import java.util.ArrayList;

public class Word{
	
	private String word_str;
	private boolean ignore = false;
	private String replacement;
	private ArrayList<String> repChoices = new ArrayList<String>();
	
	//constructor
	
	public Word(String bird)
	{
		word_str = bird;
	}
	
	
	//setters
	public void setWord_Str(String bird)
	{
		word_str = bird;
	}
	
	public void setIgnore(boolean bird)
	{
		ignore = bird;
	}

	public void setReplacement(String bird)
	{
		replacement = bird;
	}
	
	public void setRepChoices(ArrayList<String> bird)
	{
		repChoices = bird;
	}
	
	//getters
	public String getWord_Str()
	{
		return word_str;
	}
	
	public boolean getIgnore()
	{
		return ignore;
	}
	
	public String getReplacement()
	{
		return replacement;
	}
	
	public ArrayList<String> getRepChoices()
	{
		return repChoices;
	}

	//other

	
	public void addRepChoices(String bird)
	{
		repChoices.add(bird);
	}
	
	
	
	
	
	
}
