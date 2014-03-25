import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>{

	static boolean DEBUG = false;
	static TreeMap<String, Integer> words;
	static int workerNumber = 0;

	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> throws IOException
	{
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException 
		{

			//key is going to be request (search/index)
			//the value is going to be the term to search or filename to index

			//THIS IS NOT GOING TO WORK, THESE NEED TO BE CONVERTED TO THE CORRECT TYPES SOMEHOW
			String command = key;
			String argument = value;

			String result = null;

			if(command.equalsIgnoreCase("index")){
				result = index(argument);

			}
			else if(command.equalsIgnoreCase("search")){
				result = search(argument);
				word.set(result);
				output.collect(word, one);
			}
			else{
				//sanity check
				System.out.println("failed command is " + command + "; terminating connection");
				break;
			}

		}

		public static String search(String word) throws IOException{
			System.out.println("searching for " + word);

			//read the first letter to access the correct alphabetical file
			char letter = word.charAt(0);
			boolean found = false;
			String line = null;

			System.out.println("opening " + Character.toString(letter) + ".txt");

			String uri = Character.toString(letter) + ".txt";

			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			InputStream in = null;

			//search lines until we find the word
			//format should be
			//   word:doc1->freq:doc2->freq:doc3->freq\n

			try{
				in = fs.open(new Path(uri));
				//IOUtils.copyBytes(in, System.out, 4096, false);


				//read in the Character.txt and try to find the respective row

				BufferedReader buffIn = new BufferedReader(new InputStreamReader(in));

				StringBuilder responseData = new StringBuilder();
				while((line = buffIn.readLine()) != null && !found) {
					String[] splitLine = line.split(":");

					if(DEBUG){
						System.out.println("DEBUG: evaluating line for " + splitLine[0]);
					}
					if(word.equals(splitLine[0])){
						found = true;
					}
				}

				if(found){
					System.out.println(word + " found; returning \"" + line + "\"");
					return line;
				}
				else{
					System.out.println(word + " not found :<");
					return null;
				}


			} finally {
				//IOUtils.closeStream(in);
			}
		}

		private static int getPart(String input){
			final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

			Matcher matcher = lastIntPattern.matcher(input);

			if(matcher.find()){
				String someNumberStr = matcher.group(1);
				return Integer.parseInt(someNumberStr);
			}

			return -1;
		}

		public static String index(String pathName) throws IOException{
			words = new TreeMap<String, Integer>();

			//read the file to be indexed
			System.out.println("reading file " + pathName + " to be indexed");

			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(pathName), conf);
			InputStream in = null;

			//search lines until we find the word
			//format should be
			//   word:doc1->freq:doc2->freq:doc3->freq\n

			try{
				in = fs.open(new Path(pathName));
				//IOUtils.copyBytes(in, System.out, 4096, false);
				BufferedReader buffIn = new BufferedReader(new InputStreamReader(in));

				while((line = buffIn.readLine()) != null) {


					//split the line by spaces
					String[] splitLine = line.split(" ");

					//check for punctuation
					for(int i = 0; i < splitLine.length; i++){

						StringBuilder curWord = new StringBuilder(splitLine[i].toLowerCase());

						//go through each letter
						for(int j = 0; j < curWord.length(); j++){
							char curLetter = curWord.charAt(j);

							//if the letter isn't a-z, get rid of it
							if((curLetter < 'a' || curLetter > 'z')){
								curWord.deleteCharAt(j);
								j--; //to account for the removed letter
							}
						}

						if(DEBUG && !curWord.toString().equals("")){
							System.out.println("DEBUG: curWord = " + curWord.toString());
						}

						//add curWord to list of words
						String wordToAdd = curWord.toString();

						/*
						 * Everything below commented out is wrong, we wouldn't do this word frequency hash garbage
						 * send back each word found as map  <word, 1>
						 */
						if(!wordToAdd.equals("")){
							words.put(wordToAdd, words.get(wordToAdd) + 1);
							word.set(result);
							output.collect(word, one);
						}
					}
				}
				buffIn.close();

				/*
			    Path path = new Path(filePath);
			    if (!fileSystem.exists(path)) {
			        System.out.println("File does not exists");
			    }

			    // Delete file
			    fileSystem.delete(new Path(file), true);


				System.out.println("file is processed; writing to hashtable");

				//write hashtable to temp file
				String newName = pathName.substring(pathName.lastIndexOf("/") + 1);
				// Check if the file already exists
			    Path path = new Path("./temp/" + newName + "_hash" + getPart(pathName));


				if (fileSystem.exists(path)) {
				     System.out.println("File already exists");
				     return;
				}

				 // Create a new file and write data to it.
			    FSDataOutputStream out = fileSystem.create(path);
			    InputStream in = new BufferedInputStream(new FileInputStream(
			        new File(source)));


				BufferedWriter Writeout = new BufferedWriter(new OutputStreamWriter(in));

				Iterator<Map.Entry<String, Integer>> keyIterator = words.entrySet().iterator();
				while(keyIterator.hasNext()){

					//get the key and the frequency in alphabetical order
					Map.Entry<String, Integer> mapEntry = keyIterator.next();
					String key = mapEntry.getKey();
					int freq = mapEntry.getValue();

					Writeout.write(key + "->" + freq + "\n");
					if(DEBUG){
						System.out.println("DEBUG: wrote " + key + "->" + freq);
					}
				}

				Writeout.close();


				System.out.println("hashtable written; sending " + tempFile.getAbsolutePath() + " back");


				return tempFile.getAbsolutePath();
			} finally {}
				 */
			}
		}
