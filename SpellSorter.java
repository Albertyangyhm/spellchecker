//I have not given nor received any unauthorized help on this assignment
//part two at the very bottom
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.io.*;

public class SpellSorter {
	
	static QuadraticProbingHashTable<String> dictionary = new QuadraticProbingHashTable<String>();
	@SuppressWarnings("unchecked")
	static ArrayList<Word>[] misTable = new ArrayList[52];
	static int misNumb = 0;//used in quick sort to create a fixed sized array of the misspelled words

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
					String SortOutputFile = filename.substring(0, filename.length()-4) + "_sorted.txt";
					
					processFile(filename, outputFile, scan);
					
					//part2
					String[] A = toStringArr(misTable, misNumb);
					A = QuickSort(A, 0, A.length-1);
					writeSorted(A, SortOutputFile);
				}
				
				else if (choice.equals("f"))
					break;
				else if (choice.equals("q"))
				{
					end = "y";
					reEnter = "n";
				}		
			}
		}
		System.out.println("Farewell CS201, enjoyed the ride!");
		scan.close();
	}
	
	//part one
	
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
						misNumb ++;
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
				//System.out.println();
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
	//fills the array with empty ArrayLists
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
			
			//System.out.println(text);
			if (dictionary.contains(text) && !contains(text, currWord.getRepChoices()))
			{
				//if first char upper case, then uppercase
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
				//System.out.println(text);
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
				//System.out.println(text);
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
			//System.out.println(text);
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

			//System.out.println(BandC);
			if (dictionary.contains(b) && dictionary.contains(c))
			{
				if (!contains(b, currWord.getRepChoices()) || !contains(c, currWord.getRepChoices()))
				{
					currWord.addRepChoices(BandC);
				}
				
			}
		}
	}
	
	//part two
	
	//function that creates a String array filled with all the string's of all Words in the misTable
	public static String[] toStringArr(ArrayList<Word>[] misTable, int misNumb)
	{
		String[] A = new String[misNumb];
		int index = 0;
		
		for (int i = 0; i < misTable.length; i++)
		{
			Iterator<Word> j = misTable[i].iterator();
			while (j.hasNext())
			{
				A[index] = j.next().getWord_Str();
				index ++;
			}
		}
		return A;
	}
	//QuickSort main function
	public static String[] QuickSort(String[] A, int first, int last)
	{
		if (last-first < 3)
		{
			InsertionSort(A);
			return A;
		}
		else
		{
			int pivotIndex = MofThree(A, first, last);
			int splitPoint = partition(A, first, last, pivotIndex);
			QuickSort(A, first, splitPoint-1);
			QuickSort(A, splitPoint+1, last);
			return A;
		}
	}
	//Insertion sort for when there are three or less elements
	public static void InsertionSort(String[] A)
	{
	    for (int j = 1; j < A.length; j++) 
	    {  
	    	String key = A[j];  
	    	int i = j-1;  
	    	while ((i > -1) && (A[i].compareTo(key) > 0)) 
	    	{  
	    		A[i+1] = A[i];  
	    		i--;  
	    	}  
	    	A[i+1] = key; 
	    }  
	}
	//function that returns the pivot in a string array using the median of three method
	public static int MofThree(String[] A, int first, int last)
	{
		int middle = (last - first) /2;
		String a = A[first];
		String b = A[middle];
		String c = A[last];
		
		if (b.compareTo(a) > 0 && b.compareTo(c) < 0)
			return middle;
		if (b.compareTo(a) < 0 && b.compareTo(c) > 0)
			return middle;
		if (c.compareTo(a) > 0 && c.compareTo(b) < 0)
			return last;
		if (c.compareTo(a) < 0 && c.compareTo(b) > 0)
			return last;
		if (a.compareTo(b) > 0 && a.compareTo(c) < 0)
			return first;
		if (a.compareTo(b) < 0 && a.compareTo(c) > 0)
			return first;
		return -1;
	}
	//partition method used in QuickSort
	public static int partition(String[] A, int first, int last, int pivotIndex)
	{
		String pivot = A[pivotIndex];
		
		String temp = A[pivotIndex];
		A[pivotIndex] = A[last];
		A[last] = temp;
		
		int i = first;
		int j = last -1;
		boolean loop = true;
		while (loop == true)
		{
			while (A[i].compareTo(pivot) < 0)
				i++;
			while (A[j].compareTo(pivot) > 0)
				j--;
			if (j-i > 0)
			{
				String temp0 = A[i];
				A[i] = A[j];
				A[j] = temp0;
			}
			else
				loop = false;
		}
		String temp1 = A[i];
		A[i] = A[last];
		A[last] = temp1;
		
		return i;
	}
	//function used to print a string array
	public static void print(String[] A)
	{
		for (int i = 0; i<A.length;i++)
		{
			System.out.println(A[i]);
		}
	}
	//function that writes the sorted list of misspelled words
	public static void writeSorted(String[] A, String outputFile) throws IOException
	{
		BufferedWriter out0 = new BufferedWriter(new FileWriter(outputFile));
		for(int i = 0; i<A.length; i++)
			out0.write(A[i] + "\n");
		out0.close();
	}
}
