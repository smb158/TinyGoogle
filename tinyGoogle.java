import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class tinyGoogle {

	static boolean DEBUG = true;

	public static void main(String args[]) throws IOException {
		
		//TODO: fix lower-case input for searching
		
		if(DEBUG){
			System.out.println("DEBUG: creating the server");
		}
		
		//create the server
		ServerSocket tinyGoogleServerSocket = null;
		Socket clientSocket = null;

		// Try to open a server socket on any available port
		try {
			tinyGoogleServerSocket = new ServerSocket(0);
		}
		catch (IOException e) {
			System.err.println("Could not listen on port.");
			System.exit(-1);
		}   

		//grab our connection details
		String myIP = Inet4Address.getLocalHost().getHostAddress();
		String myPort = Integer.toString(tinyGoogleServerSocket.getLocalPort());

		if(DEBUG){
			System.out.println("DEBUG: registering with the nameserver");
		}
		//register with nameserver
		String nameServerIP = "";
		String nameServerPort = "";

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("nameserver.txt"));
			String line = null;
			line = reader.readLine();
			String[] tmp = line.split(":");
			nameServerIP = new String(tmp[0]);
			nameServerPort = new String(tmp[1]);
			reader.close();

		} catch (FileNotFoundException e1) {}


		if(DEBUG == true){
			System.out.println("DEBUG: Nameserver Information:");
			System.out.println("DEBUG: IP Address: "+nameServerIP);
			System.out.println("DEBUG: Port: " + nameServerPort);
		}


		Socket nameServerSocket = null;  
		PrintWriter out = null;
		BufferedReader in = null;

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
			System.out.println("DEBUG: sending nameserver a register request");
		}
		//send the nameserver our information
		out.println("register,TinyGoogle,"+myIP+","+myPort);

		//END OF CONNECTION TO NAME SERVER
		//END OF SENDING INFORMATION TO THE NAMESERVER

		String inputLine = in.readLine();
		
		if(!inputLine.equals("Success")){
			System.err.println("TinyGoogle was unable to register with the nameserver");
			System.exit(1);
		}
		else{
			if(DEBUG){
				System.out.println("DEBUG: TinyGoogle registered with nameserver!");
			}
		}

		out.close();
		in.close();
		nameServerSocket.close();

		//END OF REGISTERING WITH NAME SERVER

		//infinite loop to keep accepting jobs
		while(true)
		{
			
			if(DEBUG){
				System.out.println("DEBUG: setting up accept client");
			}
			//set up accept socket
			try{
				clientSocket = new Socket();
				clientSocket = tinyGoogleServerSocket.accept();
			}
			catch (IOException e){
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			if(DEBUG){
				System.out.println("DEBUG: creating and starting thread");
			}
			
			final ExecutorService service;
			final Future<String> task;
			
			service = Executors.newFixedThreadPool(1);
			task = service.submit(new tinyGoogleFork(clientSocket));
			
			try{
				String result;
				
				result = task.get();
				
				if(DEBUG){
					System.out.println("DEBUG: result passed from fork is \"" + result + "\"");
				}
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				out.println(result);
			}
			catch(final InterruptedException ex){
				ex.printStackTrace();
			}
			catch(final ExecutionException ex){
				ex.printStackTrace();
			}
			finally{
				//out.close();
			}
			
			service.shutdownNow();
		}

	}
	
	
}