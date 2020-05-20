//I have not given nor received any unauthorized aid on this assignment
//All five methods of finding replacements have been written
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.io.*;

public class SpellChecker {
	
	static QuadraticProbingHashTable<String> dictionary = new QuadraticProbingHashTable<String>();
	@SuppressWarnings("unchecked")
	static ArrayList<Word>[] misTable = new ArrayList[52];

	public static void main(String[] args) throws IOException
	{	
		String dicFile = args[0];
		Scanner scan = new Scanner(System.in);
	
		fillArr();
		
		System.out.println("Reading in Dictionary...");
		createDictionary(dicFile);
		System.out.println("Dictionary Read");
		
		String end = "n";
		String reEnter = "y";
		
		while (reEnter == "y")
		{
			System.out.println("Please enter a file to spell check>>");
			String filename = scan.nextLine();
			
			while (end.equals("n"))
			{
				System.out.println("Print words (p), enter new file (f), or quit (q) ?");
				String choice = scan.nextLine();
					
				if (choice.equals("p"))
				{
					String outputFile = filename.substring(0, filename.length()-4) + "_corrected.txt";
					processFile(filename, outputFile, scan);
				}
					
				else if (choice.equals("f"))
					break;
					
				else if (choice.equals("q"))
					end = "y";
					reEnter = "n";
					
			}
		}
		System.out.println("Farewell CS201, enjoyed the ride!");
		scan.close();
		
		
	}
	
	//function that reads the dictionary document and creates a quadratic probing hash table
	public static void createDictionary(String filename) throws IOException
	{
		Scanner dicReader = new Scanner(new File(filename));
		while(dicReader.hasNext())
		{
			String currWord = dicReader.next();
			dictionary.insert(currWord);
		}
		dicReader.close();
	}
	//function that handles all interaction and output within the program simultaneously
	public static void processFile(String filename, String outputFile, Scanner scan) throws IOException
	{
		Scanner fileReader = new Scanner(new File(filename));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		
		mainLoop:
		while (fileReader.hasNextLine())
		{
			//Parsing out punctuation
			String x = fileReader.nextLine();
			String[] parse = x.split("[\\s\\p{Punct}]+");//parses out punctuation and turns next word into string array
			
			String[] parseWithPunct = x.split("\\s+");//parsed with punctuation, used for output
			parseWithPunct = spaceAdder(parseWithPunct);
			parseWithPunct[parseWithPunct.length-1] = parseWithPunct[parseWithPunct.length-1]+"\n";
			
			
			//Processing each word
			for(int i = 0; i < parse.length; i++)
			{
				Word currWord = new Word(parse[i]);
				
				if (!dictionary.contains(currWord.getWord_Str()))
				{
					int a = (int) currWord.getWord_Str().charAt(0);
					
					if (a < 91 )//checks if firstChar is upper case or lower case
						a = a - 65;
					else
						a = a - 97 + 26;
	
					//Misspelled word not in misTable
					if (!contains(currWord,misTable[a]))
					{
						//insert repChoices
						swap(currWord);
						insert(currWord);
						delete(currWord);
						replace(currWord);
						split(currWord);
						
						System.out.println("--" + currWord.getWord_Str());
						misTable[a].add(currWord);
						System.out.println("ignore all (i), replace all (r), next(n), or quit (q)?");
						String choice = scan.nextLine();
						
						if (choice.equals("i"))
						{
							out.write(parseWithPunct[i]);
							currWord.setIgnore(true);
						}
							
						else if(choice.equals("r"))
						{
							int p = choiceR(currWord, scan, parseWithPunct, out, i);
							if(p ==1)
								break mainLoop;
						}
						
						else if(choice.equals("n"))
						{
							out.write(parseWithPunct[i]);
							continue;
						}
						
						else if(choice.equals("q"))
						{
							break mainLoop;
						}	
					}
					
					//Misspelled word in misTable
					else 
					{
						currWord = find(currWord, misTable[a]);//replaces currWord with the word in the hash
						
						if (currWord.getIgnore() == true)
						{
							out.write(parseWithPunct[i]);
							continue;
						}
						
						else if( currWord.getReplacement()!= null)
						{
							String replacement = currWord.getReplacement();
							out.write(replacement + parseWithPunct[i].substring(currWord.getWord_Str().length()));
							continue;
						}
						
						else//user chose n last time
						{
							System.out.println("--" + currWord.getWord_Str());
							//misTable[a].add(currWord);
							System.out.println("ignore all (i), replace all (r), next(n), or quit (q)?");
							String choice = scan.nextLine();
							
							if (choice.equals("i"))
							{
								out.write(parseWithPunct[i]);
								currWord.setIgnore(true);
							}
								
							else if(choice.equals("r"))
							{
								choiceR(currWord, scan, parseWithPunct, out, i);
							}
							
							else if(choice.equals("n"))
							{
								out.write(parseWithPunct[i]);
								continue;
							}
							
							else if(choice.equals("q"))
							{
								break mainLoop;
							}
						}
					}	
				}
				else//dictionary does contain the word
				{
					out.write(parseWithPunct[i]);
				}
			}
		}
		if(!fileReader.hasNextLine())
			System.out.println("Spell check complete!");
		fileReader.close();
		out.close();
	}
	//function that handles the replace choice
	public static int choiceR(Word currWord, Scanner scan, String[] parseWithPunct, BufferedWriter out, int i) throws IOException
	{
		ArrayList<String> list = currWord.getRepChoices();
		if(list.isEmpty())
		{
			System.out.println("There are no words to replace it with.");
			out.write(parseWithPunct[i]);
			return 0;
		}

		else
		{
			System.out.print("Replace with  ");
			int number = 1;
			Iterator<String> m = list.iterator();
			while (m.hasNext())
			{
				String z = m.next();
				System.out.print("(" + number +")" + z + " ");
				number ++;
			}
			System.out.print(", next(n), or quit(q)?");
			System.out.println();
			//scan.nextLine();
			String c0 = scan.nextLine();
			char c1 = c0.charAt(0);
			
			if (c1 == 'n')
			{
				out.write(parseWithPunct[i]);
			}
			if (c1 == 'q')
			{
				return 1;
			}
			else
			{
				String replacement = list.get(((int) c1) - 49);
				currWord.setReplacement(replacement);
				out.write(replacement + parseWithPunct[i].substring(currWord.getWord_Str().length()));
				return 0;
			}
		}
	}
	//function that adds a space at the end of each word before outputting the corrected file
	public static String[] spaceAdder(String[] x)
	{
		for (int i=0; i<x.length; i ++)
		{
			x[i] = x[i]+" ";
		}
		return x;
	}
	//function that inserts a Word into the misTable
	public static void insertMisTable(Word current)
	{
		char firstChar = current.getWord_Str().charAt(0);
		int asciiVal = (int) firstChar;
		
		if (asciiVal < 91 )//checks if firstChar is upper case or lower case
		{
			int index = asciiVal - 65;
			misTable[index].add(current);
		}
		else
		{
			int index = asciiVal - 97 + 26;
			misTable[index].add(current);
		}
		
	}
	//function that checks if an ArrayList of words contains a word
	public static boolean contains(Word current, ArrayList<Word> list)
	{
		Iterator<Word> i = list.iterator();
		while (i.hasNext())		
		{
			if (i.next().getWord_Str().equals(current.getWord_Str()))
				return true;
		}
		
		return false;
	}
	//function that checks if an ArrayList of Strings contains a String
	public static boolean contains(String current, ArrayList<String> list)
	{
		Iterator<String> i = list.iterator();
		while (i.hasNext())		
		{
			if (i.next().equals(current))
				return true;
		}
		
		return false;
	}
	//function that finds a Word in an ArrayList of words
	public static Word find(Word current, ArrayList<Word> list)
	//compares the Strings stored within words
	{
		Iterator<Word> i = list.iterator();
		while (i.hasNext())
		{
			Word x = i.next();
			if (x.getWord_Str().equals(current.getWord_Str()))
				return x;
		}
		return null;
	}
	//function that fills the misTable with empty ArrayLists of Words
	public static void fillArr()
	{
		for(int i = 0; i<misTable.length;i++)
		{
			ArrayList<Word> newArr = new ArrayList<Word>();
			misTable[i] = newArr;
		}
			
	}
	//the swap method of finding possible replacement words
	public static void swap(Word currWord)
	{
		String text = currWord.getWord_Str();
		for (int i = 1; i < text.length(); i++)
		{
			text = currWord.getWord_Str();
			if (i == text.length()-1)
				text = text.substring(0,i-1) + text.charAt(i) + text.charAt(i-1);
			else
				text = text.substring(0,i-1) + text.charAt(i) + text.charAt(i-1) + text.substring(i+1);
			
			if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
			{
				currWord.addRepChoices(text);
			}
				
		}
	}
	//the insert method of finding possible replacement words
	public static void insert(Word currWord)
	{
		String text = currWord.getWord_Str();
		
		for (int i = 0; i < text.length(); i++)
		{
			for (int j = 97; j < 123; j++)
			{
				text = currWord.getWord_Str();
				char letter = (char) j;
				text = text.substring(0,i) + letter + text.substring(i);
				if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
				{
					currWord.addRepChoices(text);
				}
			}
		}
		//inserting into the last position
		for (int j = 97; j < 123; j++)
		{
			text = currWord.getWord_Str();
			char letter = (char) j;
			text = text + letter;
			
			if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
			{
				currWord.addRepChoices(text);
			}
				
		}
	}
	//the delete method of finding possible replacement words
	public static void delete(Word currWord)
	{
		String text = currWord.getWord_Str();
		
		for (int i = 0; i < text.length(); i++)
		{
			String a = text;
			if (i == text.length()-1)
				a = text.substring(0, i);
			else
				a = text.substring(0,i) + text.substring(i+1);

			if (dictionary.contains(a) && !contains(a, currWord.getRepChoices()))
			{
				currWord.addRepChoices(a);
			}
		}
	}
	//the replace method of finding possible replacement words
	public static void replace(Word currWord)
	{
		String text = currWord.getWord_Str();
		
		for (int i = 1; i < text.length(); i++)
		{
			for (int j = 97; j < 123; j++)
			{
				text = currWord.getWord_Str();
				char letter = (char) j;
				text = text.substring(0,i-1) + letter + text.substring(i);
				if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
				{
					currWord.addRepChoices(text);
				}
			}
		}
		//inserting into the last position
		for (int j = 97; j < 123; j++)
		{
			text = currWord.getWord_Str();
			char letter = (char) j;
			text = text.substring(0, text.length()-1) + letter;
			if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
			{
				currWord.addRepChoices(text);
			}
				
		}
	}
	//the split method of finding possible replacement words
	public static void split(Word currWord)
	{
		String text = currWord.getWord_Str();
		
		for (int i = 1; i < text.length(); i++)
		{
			String a = text;
			String b = a.substring(0,i);
			String c = a.substring(i);
			String BandC = b + " " + c;

			if (dictionary.contains(b) && dictionary.contains(c))
			{
				if (!contains(b, currWord.getRepChoices()) || !contains(c, currWord.getRepChoices()))
				{
					currWord.addRepChoices(BandC);
				}
				
			}
		}
	}
	
}
