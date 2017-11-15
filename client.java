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
	public Client(String ip, int port){
		portNum = port;
		ipAddr = ip;
		runClient();
	}

	public void runClient(){

		String userName = System.console().readLine("Enter your username: ");

		try{
			SocketChannel sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(ipAddr, portNum));
			System.out.println("Connected to Server!");
		}catch(IOException e){
			System.out.println("Got an exception.  Whoops.");
		}
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
}

class ClientThread extends Thread{

	SocketChannel sc;
	ClientThread(SocketChannel channel){
		sc = channel;
	}
	public void run(){
		try{
			ByteBuffer buff = ByteBuffer.allocate(1024);
			sc.read(buff);
			String message = new String(buff.array());
		}catch(IOException e){
			System.out.println("Got an exception in thread");
		}
	}
}
