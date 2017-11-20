import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
* Encrypted Chat Program Clinet
*
* @author Dustin Thurston
* @author Ryan Walt
**/
class Client{
	int portNum;
	String ipAddr;
	ArrayList<String> commands = new ArrayList<>();
	boolean admin;
	boolean exit;
	public Client(String ip, int port){
		portNum = port;
		ipAddr = ip;
		admin = false;
		exit = false;
		commands.add("/exit");
		commands.add("/pm");
		commands.add("/kick");

		runClient();
	}

	public void setAdmin(){
		admin = true;
		System.out.println("You are now an admin.");
	}

	public void runClient(){
		Console cons = System.console();
		String userName = cons.readLine("Enter your username: ");

		try{
			SocketChannel sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(ipAddr, portNum));

			Thread t = new Thread(new Runnable() {
				public void run() {
					//Code to run in thread
					runThread(sc);
				}
			});
			t.start();

			System.out.println("Connected to Server!");

			//Sends the username to the server and establishes a connection
			ByteBuffer buff = ByteBuffer.wrap(userName.getBytes());
			sc.write(buff);
			while(!exit){

				String message = "";

				while(message.equals("")){
					Thread.sleep(100);
					message = cons.readLine("");
					message = message.trim();
					if(!message.equals("") && validMessage(message)){
						backspace(message);
						break;
					}else if(!message.equals("")){
						System.out.println("Invalid command");
						message = "";
					}
				}
				buff = ByteBuffer.wrap(message.getBytes());
				sc.write(buff);
			}
			//t.close();

		}catch(Exception e){
			System.out.println("Got an exception.  Whoops.");
			System.out.println(e);
		}
	}

	/**
	* Checks to see if messsage or command is valid
	*
	* @param m the message to be checked
	* @return true of false if valid
	*/
	private boolean validMessage(String m){
		if(m.charAt(0) != '/'){
			return true;
		}else{
			String[] contents = m.split(" ");
			switch(contents[0]){
				case "/exit":
					return true;
				case "/pm":
					if(contents.length >= 3)
						return true;
					break;
				case "/kick":
					if(contents.length == 2 && admin == true)
						return true;
					break;
				case "/list":
					if(contents.length == 1)
						return true;
					break;
			}
		}
		return false;
	}

	private void backspace(String s){
		System.out.print(String.format("\033[%dA",1));
		System.out.print("\033[2K");
	}

	public static void main(String[] args){
		int port = 0;
		String ip = "";
		try{
			if(args.length == 2){
				port = Integer.parseInt(args[1]);
				if(!validitycheck(args[0].trim())){
					return;
				}else if(port < 1024 || port > 65535){
					System.out.println("Invalid Port Number. Closing..");
				}else{
					ip = args[0];
					Client c = new Client(ip, port);
				}
			}else{
				Console cons = System.console();
				ip = cons.readLine("Enter IP Address: ");
				if(!validitycheck(ip))
					return;

				port = Integer.parseInt(cons.readLine("Enter Port Number: "));
				if(port < 1024 || port > 65535){
					System.out.println("Invalid Port Number. Closing..");
				}

				Client c = new Client(ip, port);
			}
		}catch(Exception e){
			System.out.println("Invalid IP or Port Number.  Try again.");
		}
	}

	/**
	 * Checks validity of user given IP address
	 *
	 * @param ip user typed IP address\
	 * @return true if valid, false if not
	 * */
	public static boolean validitycheck(String ip){
		try{
			String[] iparray = ip.split("\\.");
			int[] ipintarray = new int[iparray.length];
			for(int i = 0; i < iparray.length; i++){
				ipintarray[i] = Integer.parseInt(iparray[i]);
			}
			if(ipintarray.length != 4){
				throw new NumberFormatException();
			}else{
				return true;
			}
		}catch(NumberFormatException nfe){
			System.out.println("Invalid IP address. Closing program..");
			return false;
		}
	}

	private void runThread(SocketChannel sc){
		while (true){
			if(!sc.isConnected()){
				break;
			}
			try{
				ByteBuffer buff = ByteBuffer.allocate(1024);

				sc.read(buff);
				//System.out.println("read");
				String message = new String(buff.array());
				message = message.trim();

				if(message.equals("/admin")){
					setAdmin();
				}else if (message.length() == 0){
					System.exit(0);
					return;
				}
				else{
					System.out.println(message);
				}
			}catch(Exception e){
				System.out.println("Got an exception in thread");

			}
		}
	}
}
