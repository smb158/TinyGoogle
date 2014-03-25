import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class tinyGoogleFork implements Callable<String> {
	private boolean DEBUG = true;
	protected Socket clientSocket = null;
	ArrayList<String> searchResults = null;
	ArrayList<String> indexFiles = null;

	tinyGoogleFork(Socket clientSocket){
		this.clientSocket = clientSocket;
	}

	public String call() {
		if(DEBUG){
			System.out.println("DEBUG: thread running");
		}

		BufferedReader in = null;

		//set up things so we can read from the socket
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e1) {}

		//get the client's command and arguments
		String commandAndArg = "";

		try {  
			if(DEBUG){
				System.out.println("DEBUG: getting client's command and arguments");
			}
			commandAndArg = in.readLine();

		} catch (IOException e) {}

		if(commandAndArg == null){
			if(DEBUG){
				System.out.println("DEBUG: client exited; terminate connection/thread");
				return "Connection terminated";
			}
		}
		String[] command = commandAndArg.split(" ");

		if(DEBUG){
			System.out.print("DEBUG: client wants to " + command[0]);
			for(int i = 1; i < command.length; i++){
				System.out.print(" " + command[i]);
			}
			System.out.println();
		}

		//Now we have to check if this is an indexing request or a search request
		if(command[0].equalsIgnoreCase("index")){
			if(DEBUG){
				System.out.println("DEBUG: indexing " + command[1] + "...");
			}

			/*TODO:
			 * 
			 * The file exists check has to be modified to look in HDFS
			 * 
			 * 
			 * 
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * 
			 */

			//File f = new File(command[1]);
			//if(f.exists()){

			String result = "";
			try {
				result = index(command[1]);
			} catch (IOException e) {} catch (Exception e) {
				e.printStackTrace();
			}
			if(DEBUG){
				System.out.println("DEBUG: ...and done");
			}

			return result;
			//}
			//else{
			//	return "The file to be indexed doesn't exist!";
			//}
		}
		else if(command[0].equalsIgnoreCase("search")){
			if(DEBUG){
				System.out.print("DEBUG: searching");
				for(int i = 1; i < command.length; i++){
					System.out.print(" " + command[i]);
				}
				System.out.println("...");
			}

			String result = "";
			try {
				result = search(command);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(DEBUG){
				System.out.println("DEBUG: ...and done");
			}

			return result;
		}
		else{
			//sanity check
			System.out.println("DEBUG: failed command is " + command);
			return "Something went wrong here; try entering a valid command";
		}
	}

	public class IndexReduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> 
	{
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException 
		{
			/*
				int sum = 0;
				while (values.hasNext()) 
				{
					sum += values.next().get();
				}
	 			output.collect(key, new IntWritable(sum));
			 */


			if(DEBUG){
				System.out.println("DEBUG: We have all of the intermediate index files");
			}

			//we have all the files
			//read in each and merge with the master index
			BufferedReader in;
			TreeMap<String, Integer> toMerge = new TreeMap<String, Integer>();
			String curLine = "";

			//go through each file
			if(DEBUG){
				System.out.println("DEBUG: indexFiles' size = " + indexFiles.size());
			}
			for(int i = 0; i < indexFiles.size(); i++){
				if(DEBUG){
					System.out.println("DEBUG: Opening the temp hash file "+indexFiles.get(i));
				}
				in = new BufferedReader(new FileReader(indexFiles.get(i)));

				curLine = "";
				while(curLine != null){
					if(DEBUG){
						System.out.println("DEBUG: curLine: " + curLine);
					}
					curLine = in.readLine();

					if(curLine != null && curLine.split("->").length > 0){

						String curKey = curLine.split("->")[0];
						int curFreq = Integer.parseInt(curLine.split("->")[1]);

						if(toMerge.containsKey(curKey)){
							//add its frequency to the existing entry
							int oldFreq = toMerge.get(curKey);

							toMerge.put(curKey, curFreq + oldFreq);
						}
						else{
							toMerge.put(curKey, curFreq);
						}
					}
				}
				in.close();

				new File(indexFiles.get(i)).delete();
			}

			System.out.println("\nAll parts indexed to master hash tree; writing/updating hash tree to master index files");
			//update file(s) with indexed document
			//assumption for speed: hashtable is alphabetically sorted
			//assumption assured due to TreeMap's entrySet()
			ArrayList<String> fileContents = null;
			char curLetter = '-';

			Iterator<Map.Entry<String, Integer>> keyIterator = toMerge.entrySet().iterator();
			while(keyIterator.hasNext()){
				//get the key and the frequency in alphabetical order
				Map.Entry<String, Integer> mapEntry = keyIterator.next();
				String key = mapEntry.getKey();
				int freq = mapEntry.getValue();

				if(DEBUG){
					System.out.println("DEBUG: processing " + key + "->" + freq);
				}

				//check if we need to close the current file and open another
				if(curLetter != key.charAt(0)){

					//close the current file (if any)
					if(curLetter != '-'){

						if(DEBUG){
							System.out.println("DEBUG: have to close " + Character.toString(curLetter) + ".txt");
						}

						File indexLetterFile = new File(Character.toString(curLetter) + ".txt");
						Writer output = new BufferedWriter(new FileWriter(indexLetterFile));

						for(int i = 0; i < fileContents.size(); i++){
							output.write(fileContents.get(i) + "\n");
						}

						output.close();
					}
					//read in the entire file into an ArrayList(one entry per line)
					fileContents = new ArrayList<String>();
					File indexLetterFile = new File(key.substring(0, 1) + ".txt");


					if(indexLetterFile.exists()){
						if(DEBUG){
							System.out.println("DEBUG: reading in " + key.substring(0, 1) + ".txt");
						}
						Scanner readToArray = new Scanner(indexLetterFile);
						curLine = null;
						try{
							while((curLine = readToArray.nextLine()) != null){
								fileContents.add(curLine);
							}
						} catch (NoSuchElementException e){

						}

						readToArray.close();
					}
				}


				//format should be
				//   word:doc1->freq:doc2->freq:doc3->freq\n

				//and either rewrite entries or insert new entries for new lines.
				if(DEBUG){
					System.out.println("DEBUG: making comparisons to check if " + key + " is in file");
				}

				boolean added = false;
				for(int i = 0; i < fileContents.size() && !added; i++){

					//check if the word is before, after, or exactly a match
					int comparison = key.compareTo(fileContents.get(i).split(":")[0]);

					if(comparison == 0){
						//exact match; add the word and build the new line
						if(DEBUG){
							System.out.print("DEBUG: found exact match, ");
						}
						String[] splitByColon = fileContents.get(i).split(":");
						StringBuilder editedLine = new StringBuilder(key);

						boolean written = false;
						for(int j = 1; j < splitByColon.length; j++){
							if(!written){
								int curFreq = Integer.parseInt(splitByColon[j].split("->")[1]);

								if(freq > curFreq){
									//insert doc->freq at j - 1
									editedLine = editedLine.append(":" + filename + "->" + freq);
									if(DEBUG){
										System.out.println("inserted at index " + j);
									}
									written = true;
								}
							}
							editedLine = editedLine.append(":" + splitByColon[j]);
						}

						if(!written){ //it will fit at the end; append it
							if(DEBUG){
								System.out.println("adding to end of line");
							}
							editedLine = editedLine.append(":" + filename + "->" + freq); 
						}

						fileContents.set(i, editedLine.toString());
						added = true;

					}

					else if(comparison < 0){
						//negative; key precedes the current line, so insert it there
						if(DEBUG){
							System.out.println("DEBUG: found negative match; inserting word");
						}
						fileContents.add(i, key + ":" + filename + "->" + freq);
						added = true;
					}

					//else if positive, continue to read through
				}

				if(!added){
					//we didn't add it yet, so just add it here and sort for good measure
					if(DEBUG){
						System.out.println("DEBUG: was not added previously so adding here");
					}
					fileContents.add(key + ":" + filename + "->" + freq);
					Collections.sort(fileContents);
				}

				curLetter = key.charAt(0);
			}

			//close last file :P
			File indexLetterFile = new File(Character.toString(curLetter) + ".txt");
			Writer output = new BufferedWriter(new FileWriter(indexLetterFile));

			for(int i = 0; i < fileContents.size(); i++){
				output.write(fileContents.get(i) + "\n");
			}

			output.close();

			//annnnnnnnnd return
			return filepath + " indexed";
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String index(String filepath) throws Exception{
		indexFiles = new ArrayList<String>();

		if(DEBUG){
			System.out.println("\nDEBUG: TinyGoogleThread: made it to the index method");
		}

		//sanitize filepath
		String filename = filepath.toString();
		if(filepath.contains("/")){
			filename = filename.substring(filename.lastIndexOf("/") + 1);
		}

		if(filename.contains(".")){
			filename = filename.substring(0, filename.indexOf("."));
		}

		if(DEBUG){
			System.out.println("DEBUG: sanitized filepath = " + filename + "; filepath is still " + filepath);
		}


		//split the file to index into 500 word chunks
		//int numberOfJobs = splitFile(filepath, filename);

		JobConf conf = new JobConf(Worker.class);
		conf.setJobName("tinyGoogleIndex");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(IndexReduce.class);
		conf.setReducerClass(IndexReduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);


		//TODO: I dont know what to put at the input and output path because its going to be all of the inverted index files
		FileInputFormat.setInputPaths(conf, new Path(filepath));
		String FakeOutput = null;
		FileOutputFormat.setOutputPath(conf, new Path(FakeOutput));
		JobClient.runJob(conf);

	}

	public class SearchReduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> 
	{
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException 
		{
			//TODO: the main problem with this is when this is called only one worker has responded? I dont know how we would do anything.	
			//we only have the search results for one term (they are stored as a string (key) how do we get all of the responses from the workers
			//so we can aggregate them?


			//TODO: We need this to do the final output. 
			//output.collect(key, new IntWritable(sum));

		}

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String search(String[] query) throws IOException, InterruptedException{
		searchResults = new ArrayList<String>();
		if(DEBUG){
			System.out.println("TinyGoogleThread: got search command");
		}

		//MAKE SURE TO SKIP FIRST ELEMENT (we are sending "search one two three four etc")

		String[] theTerms = new String[query.length-1];

		//create a new string array that only holds the searchable terms and not "search"
		for(int x = 1; x < query.length; x++){
			//sanitize here
			String curWord = query[x].toLowerCase();
			if(curWord.charAt(0) < 'a' || curWord.charAt(0) > 'z'){
				//bad
				return "One or more of your terms were unsearchable";
			}
			theTerms[x-1] = curWord;
		}

		/*
		 * TODO: This is where the mapreduce stuff has to run
		 * 
		 * 
		 */

		JobConf conf = new JobConf(WordCount.class);
		conf.setJobName("wordcount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(WorkerMap.class);
		conf.setCombinerClass(SearchReduce.class);
		conf.setReducerClass(SearchReduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		//TODO: I dont know what these file input and output need to be

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		JobClient.runJob(conf);



		System.out.println("aggregating the results");
		//check to make sure all terms were searched

		if(DEBUG){
			System.out.println("DEBUG: theTerms(" + theTerms.length + "):");
			for(int i = 0; i < theTerms.length; i++){
				System.out.println("\t" + theTerms[i]);
			}

			System.out.println("DEBUG: searchResults(" + searchResults.size() + "):");
			for(int i = 0; i < searchResults.size(); i++){
				System.out.println("\t" + searchResults.get(i));
			}
		}

		if(theTerms.length == searchResults.size()){
			Hashtable<String, Integer> docs = new Hashtable<String, Integer>();
			ArrayList<String> docNames = new ArrayList<String>();

			//loop through and add docs to hashtable as keys
			for(int i = 0; i < searchResults.size(); i++){
				String curSplit[] = searchResults.get(i).split(":");

				//add all docs
				for(int j = 1; j < curSplit.length; j++){
					if(!docs.containsKey(curSplit[j].split("->")[0])){
						if(DEBUG){
							System.out.println("adding " + curSplit[j].split("->")[0]);
						}

						docNames.add(curSplit[j].split("->")[0]);
						docs.put(docNames.get(docNames.size() - 1), new Integer(0));
					}
				}
			}

			//then, we look at relative occurrences
			//herein lies the most disgusting code i've ever written
			for(int i = 0; i < theTerms.length; i++){

				//find the line we need in searchResults
				//j is irrelevant, just trying to find the correct searchResults line
				for(int j = 0; j < searchResults.size(); j++){
					if(searchResults.get(j).split(":")[0].equals(theTerms[i])){
						//we got it
						//look through and populate the array

						String curSplit[] = searchResults.get(j).split(":");

						//for each relevent document, add its array spot
						//k is current array spot
						for(int k = 1; k < curSplit.length; k++){

							String curEntry = curSplit[k];
							String curDoc = curEntry.split("->")[0];

							if(DEBUG){
								System.out.println(curEntry.split("->")[0] + " is relevant at array spot " + k);
							}

							//find relevant document's name
							for(int l = 0; l < docNames.size(); l++){
								if(docNames.get(l).equals(curDoc)){
									Integer curNum = docs.get(docNames.get(l));
									System.out.println("curNum = " + curNum);

									curNum += k;
									if(DEBUG){
										System.out.println(docNames.get(l) + "'s current relevancy: " + curNum);
									}

									docs.put(docNames.get(l), curNum);
									break;
								}
							}
						}
					}
				}
			}

			//finally, we add scores and decide which is better
			//the lowest score is the most relevant
			//also, make sure to factor in 0's

			Iterator<Entry<String, Integer>> iterate = docs.entrySet().iterator();

			ArrayList<String> bestDocs = new ArrayList<String>(10);
			ArrayList<Integer> bestScores = new ArrayList<Integer>(10);

			for(int i = 0; i < 10; i++){
				bestScores.add(Integer.MAX_VALUE);
			}

			for(int i = 0; i < 10; i++){
				bestDocs.add("----");
			}


			//loop through all docs
			boolean hasAResult = false;

			while(iterate.hasNext()){
				boolean relevant = true;
				Entry<String, Integer> curDoc = iterate.next();
				if(DEBUG){
					System.out.println("ranking " + curDoc.getKey());
				}

				//add score
				int score = curDoc.getValue();

				if(relevant){
					//add doc to bestDocs and score to bestScores

					//find out the place it belongs in
					boolean added = false;

					for(int i = 0; i < bestScores.size() && !added; i++){
						if(score < bestScores.get(i)){
							if(DEBUG){
								System.out.println("DEBUG: Score:"+score+" < bestScores["+i+"]:"+bestScores.get(i));
							}

							//insert the new score at the index
							bestScores.add(i, score);
							bestScores.remove(bestScores.size() - 1);
							bestDocs.add(i, curDoc.getKey());
							bestDocs.remove(bestScores.size() - 1);

							added = true;
							hasAResult = true;
						}
					}
				}
			}

			if(hasAResult){
				//send back bestDocs to TinyGoogle and ultimately the user/client
				StringBuilder aggregatedResults = new StringBuilder();
				aggregatedResults = aggregatedResults.append("Documents containing your query according to relevancy:----");
				for(int i = 0; i < bestScores.size(); i++){
					if(bestDocs.get(i) != null && !bestDocs.get(i).equals("----")){
						aggregatedResults = aggregatedResults.append(bestDocs.get(i) + "----");
					}
				}

				return aggregatedResults.toString();
			}

		}

		//else, either the docs did not contain all or any of the phrases entered
		return "Sorry, no document contains all of the terms you searched for.";

	}

	private int splitFile(String filePath, String fileName) throws Exception {
		int MAX_WORDS = 500;

		if(DEBUG){
			System.out.println("DEBUG: beginning file splitting");
		}

		File dir = new File("./temp/");
		if (!dir.exists()){
			if (!dir.mkdirs()){
				System.out.println("Cant create the temp directory");
				System.exit(1);
			}
		}
		else if(!dir.isDirectory()){
			System.out.println("temp is not a directory");
			System.exit(1);
		}

		int i = 1;
		Scanner in = new Scanner( new FileInputStream( filePath ) );

		// read in a line, count the words until the file is donezo
		int totalWordCount = 0;
		PrintStream ps = new PrintStream( new FileOutputStream( "./temp/" + fileName + i ) );

		while( in.hasNextLine() ) {
			String line = in.nextLine();

			/*if(DEBUG){
				System.out.println("DEBUG: line = " + line);
			}*/

			String[] conts = line.split( " " );

			ps.println(line);
			totalWordCount += conts.length;
			if( totalWordCount > MAX_WORDS ) {

				/*if(DEBUG){
					System.out.println("DEBUG: reached 500 words; creating new file");
				}*/

				i++;
				totalWordCount = 0;
				ps.close();
				ps = new PrintStream( new FileOutputStream( "./temp/" + fileName + i ) );
			}
		}

		if(DEBUG){
			System.out.println("DEBUG: splitFile has completed. The file is in "+i+" parts");	
		}
		return i;
	}



}