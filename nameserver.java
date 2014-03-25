import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//TinyGoogle NameServer
//Steven Bauer & Bret Gourdie

public class nameserver {

	//Create the nameserver database
	public static String[][] database = new String[100][3];

	static boolean DEBUG = false;
	public static void main(String args[]) throws UnknownHostException {

		System.out.println("Initializing database");
		//initialize all spots in the database to null
		for(int i = 0; i < 100; i++){
			database[i][0] = null;
		}
		
		System.out.println("Creating the server");
		//create the server
		ServerSocket nameServerSocket = null;
		Socket clientSocket = null;

		// Try to open a server socket on any available port
		try {
			nameServerSocket = new ServerSocket(0);
		}
		catch (IOException e) {
			System.err.println("Could not listen on port.");
			System.exit(-1);
		}   

		System.out.println("Writing connection details to the file");
		//write our connection details to the file
		String myIP = Inet4Address.getLocalHost().getHostAddress();
		String myPort = Integer.toString(nameServerSocket.getLocalPort());
		try {
			BufferedWriter Writeout = new BufferedWriter(new FileWriter(new File("nameserver.txt")));
			Writeout.write(myIP+":"+myPort+"\n");
			Writeout.newLine();
			Writeout.close();
		} catch (IOException e) {}


		//infinite loop to keep accepting new jobs
		System.out.println("Waiting for new connections...\n");
		while(true)
		{
			if(DEBUG){
				System.out.println("DEBUG: Setting up accept socket");
			}
			//set up accept socket
			try{
				clientSocket = nameServerSocket.accept();
			}
			catch (IOException e){
				System.err.println("Accept failed");
				System.exit(-1);
			}

			//set up things so we can read and write from the socket
			PrintWriter out = null;
			BufferedReader in = null;
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e1) {}
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e1) {}

			String inputLine;
			try {
				//while ((inputLine = in.readLine()) != null) {
				inputLine = in.readLine();
					if(DEBUG){
						System.out.println("DEBUG: we got : " + inputLine);
					}

					String[] userInput = inputLine.split(",");

					if(userInput[0].equals("register")){
						//userInput[1] is the service name
						//userInput[2] is the ip address
						//userInput[3] is the port

						if(DEBUG){
							System.out.println("DEBUG: We are going to register " + userInput[1]);
						}

						if(registerName(userInput[1], userInput[2], userInput[3]) == 1){
							System.out.println("Registered " + userInput[1]);
							//send success string to the client
							out.println("Success");
						}
						else{
							if(DEBUG){
								System.out.println("DEBUG: registration " + userInput[1] + " failed.");
							}
							//send failure to the client
							out.println("Failed");
						}
					}
					else if(userInput[0].equals("resolve")){
						//userInput[1] is service name to resolve

						if(DEBUG){
							System.out.println("DEBUG: Attempting to resolve " + userInput[1]);
						}

						String connectionDetails = "";
						connectionDetails = resolveName(userInput[1]);
						//send details to the requester
						if(connectionDetails != null){
							System.out.println("Resolving " + userInput[1] + " at "+connectionDetails);
							out.println(connectionDetails);
						}
						//this service has not been registered with the name server
						else{
							if(DEBUG){
								System.err.println("Failed getting connection details");
							}
							out.println("Failed");
						}
					}
					else{
						if(DEBUG){
							System.err.println("DEBUG: " + userInput[0] + " is an invalid command");
						}
					}

					if(DEBUG){
						System.out.println("DEBUG: command handled; ready to accept new command\n");
					}
				//}
			} catch (IOException e) {}
		}
	}

	public static int registerName(String serviceName, String IPAddress, String Port){
		for(int i = 0; i < 100; i++){
			if(database[i][0] == null){
				if(DEBUG){
					System.out.println("DEBUG: " + serviceName + " is not registered; adding it at " + IPAddress + " at port " + Port);
				}
				//add to the database
				database[i][0] = serviceName;
				database[i][1] = IPAddress;
				database[i][2] = Port;

				return 1;
			}
			else if(database[i][0].equals(serviceName)){
				if(DEBUG){
					System.out.println("DEBUG: " + serviceName + " already registered");
				}
				return -1;
			}
		}
		return -1;
	}
	public static String resolveName(String serviceName){
		if(DEBUG){
			System.out.println("DEBUG: inside resolve");
		}
		for(int i = 0; i < 100; i++){
			if(DEBUG){
				if(database[i][0] != null){
					System.out.println("DEBUG: database[" + i + "][0] = " + database[i][0]);
				}
			}
			if(database[i][0] == null){
				if(DEBUG){
					System.out.println("DEBUG: " + serviceName + " not found");
				}

				return null;
			}
			else if(database[i][0].equals(serviceName)){
				if(DEBUG){
					System.out.println("DEBUG: " + serviceName + " was found at " + database[i][1] + " at port " + database[i][2]);
				}

				//build return string
				String toReturn = database[i][1] + "," + database[i][2];
				return toReturn;
			}
		}
		return null;
	}
}
