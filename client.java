import java.io.*;
import java.net.*;
import java.util.*;

public class client {
	static boolean DEBUG = false;

	public static void main(String[] args) throws IOException {

		//TinyGoogle client 

		Scanner scan = new Scanner(System.in);

		String nameServerIP = "";
		String nameServerPort = "";

		BufferedReader reader;

		if(DEBUG){
			System.out.println("DEBUG; getting nameserver information");
		}

		try {
			reader = new BufferedReader(new FileReader("nameserver.txt"));
			String line = null;
			line = reader.readLine();
			String[] tmp = line.split(":");
			nameServerIP = new String(tmp[0]);
			nameServerPort = new String(tmp[1]);
			reader.close();

		} catch (FileNotFoundException e1) {}


		if(DEBUG){
			System.out.println("DEBUG: Nameserver Information:");
			System.out.println("DEBUG: IP Address: "+nameServerIP);
			System.out.println("DEBUG: Port: "+nameServerPort+"\n");
		}


		Socket nameServerSocket = null;  
		PrintWriter out = null;
		BufferedReader in = null;

		if(DEBUG){
			System.out.println("DEBUG: connecting to the nameserver");
		}
		try {
			//connect to the nameserver
			nameServerSocket = new Socket(nameServerIP, Integer.parseInt(nameServerPort));
			out = new PrintWriter(nameServerSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(nameServerSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host nameserver");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to nameserver");
			System.exit(1);
		}

		if(DEBUG){
			System.out.println("DEBUG: sending \"resolve,TinyGoogle\" to connect to TinyGoogle");
		}
		//send the nameserver our query
		out.println("resolve,TinyGoogle");
		String TinyGoogleIP = "";
		String TinyGooglePort = "";
		String inputLine = "";
		while(!inputLine.contains(",")){
			inputLine = in.readLine();

			TinyGoogleIP = inputLine.split(",")[0];
			TinyGooglePort = inputLine.split(",")[1];
			if(DEBUG){
				System.out.println("DEBUG: TinyGoogle IP = "+TinyGoogleIP);
				System.out.println("DEBUG: TinyGoogle Port = "+TinyGooglePort);
			}
		}

		if(DEBUG){
			System.out.println("DEBUG: closing nameserver connection, ");
		}
		//close connection to nameserver
		out.close();
		in.close();
		nameServerSocket.close();

		if(DEBUG){
			System.out.println("DEBUG: setting up connection to TinyGoogle");
		}
		//set up connection to TinyGoogle
		Socket tinyGoogleSocket = null;  
		out = null;
		in = null;
		try {
			//connect to TinyGoogle
			tinyGoogleSocket = new Socket(TinyGoogleIP, Integer.parseInt(TinyGooglePort));
			out = new PrintWriter(tinyGoogleSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(tinyGoogleSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host TinyGoogle");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to TinyGoogle");
			System.exit(1);
		}

		if(DEBUG == true){
			System.out.println("DEBUG: Connected to TinyGoogle, ready to get users command");
		}


		System.out.println("\n/\\/\\/\\/\\/\\Welcome to TinyGoogle!/\\/\\/\\/\\/\\");

		String usersQuery = "";
		while(!usersQuery.equalsIgnoreCase("exit"))
		{
			System.out.println("\nExpected input:\nindex filepathname\nsearch query");
			System.out.println("Type exit to stop:\n");
			
			usersQuery = scan.nextLine();

			if(!usersQuery.equalsIgnoreCase("exit")){
				
				//send users command and arguments to TinyGoogle
				if(DEBUG){
					System.out.println("DEBUG: sending \"" + usersQuery + "\" to TinyGoogle");
				}
				out.println(usersQuery);
				inputLine = in.readLine();
				
				if(inputLine.contains("----")){
					String[] splitLines = inputLine.split("----");
					System.out.println(splitLines[0]);
					for(int i = 1; i < splitLines.length; i++){
						System.out.println(i + ") " + splitLines[i]);
					}
				}
				else{
					System.out.println("\nTinyGoogle says \"" + inputLine + "\"");
				}

				System.out.println("Enter another command:");
				
				//reestablish connection with tinyGoogle
				try {
					//connect to TinyGoogle
					tinyGoogleSocket = new Socket(TinyGoogleIP, Integer.parseInt(TinyGooglePort));
					out = new PrintWriter(tinyGoogleSocket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(tinyGoogleSocket.getInputStream()));
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host TinyGoogle");
					System.exit(1);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to TinyGoogle");
					System.exit(1);
				}
			}
		}
	}           
}