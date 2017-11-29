import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import javax.xml.bind.DatatypeConverter;

public class Server{
    private int portNum;
    private Map<String,SocketChannel> allUsers;

    public Server(int port){
        portNum = port;
        allUsers =  new ConcurrentHashMap<String,SocketChannel>();
        runServer();
    }

    public void runServer(){
		//TODO: ask about closing threads on exit.
        try{
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress(portNum));
            System.out.println("Server conected...");

            while (true){

                SocketChannel sc = channel.accept();

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        //Code to run in thread
                        runThread(sc);
                    }
                });
                t.start();
            }
        }
        catch(IOException e){
            System.out.println("Exception in the server class.");
            System.out.println(e);
        }
    }

    public void runThread(SocketChannel s){
        SocketChannel sc = s;
        String userName = "";
        try{
            //Get the clients username
            ByteBuffer userBuf = ByteBuffer.allocate(1024);
            sc.read(userBuf);
            userName = new String(userBuf.array());
            userName = userName.trim();
            if (!checkName(sc, userName)){
                return;
            }
			if(userName.equals("dustin") || userName.equals("walt")){
				userBuf = ByteBuffer.wrap("/admin".getBytes());
				sc.write(userBuf);
			}
            //userName = removeSlash(userName);
            System.out.println("User added: " + userName);
            //Add to the map
            allUsers.put(userName, sc);

            boolean connected = true;
            //Send message to all users on the server.
            while (connected){
                ByteBuffer buff = ByteBuffer.allocate(1024);
                sc.read(buff);
                String message = new String(buff.array());
                message.trim();

                //check if message is a command...
                if (!isCommand(userName, sc, message)){
                    //Else send it to all...
                    message = userName + ": " + message.trim();
                    System.out.println(message);
                    buff = ByteBuffer.wrap(message.getBytes());
                    sendMessageAll(buff);
                }
            }

            sc.close();
        }
        catch(IOException e){
            allUsers.remove(userName);
            System.out.println(userName + " has disconnected.");
        }
    }

    public boolean checkName(SocketChannel s, String n){
        for(String i: allUsers.keySet()){
            if (n.equals(i)){
                ByteBuffer b = ByteBuffer.wrap("Username in use!".getBytes());

				try{
					s.write(b);
				}catch(Exception e){
					System.out.println("Error in checkName: " + e);
				}
                exit(s);
                return false;
            }
        }
        return true;
    }


    public String removeSlash(String s){
        boolean remove = false;
        StringBuilder sb = new StringBuilder(s);
        for (int i = s.length()-1; i >= 0; i--){
            if (s.charAt(i) == (' '))
                remove = true;
            if (remove)
                sb.deleteCharAt(i);
        }
        return sb.toString();
    }

    public boolean isCommand(String senderName, SocketChannel sender, String m){
        String[] contents = m.split(" ");
        contents[0] = contents[0].trim();

        switch(contents[0]){
            case "/exit":
                exit(sender);
                return true;
            case "/pm":
                //Get the message of the user and turn it back to a string.
                String temp = "";
                for (int i = 2; i < contents.length; i++){
                    temp += contents[i] + " ";
                }
                temp = temp.trim();
                //Send the message to a specific user.
                sendPM(senderName, sender, contents[1], temp);
                return true;
            case "/kick":
                System.out.println("User to kick: " + contents[1]);
                kick(sender, contents[1]);
                return true;
			case "/list":
				System.out.println(senderName + " requesting user list.");
				sendList(sender);
				return true;
        }

        return false;
    }

    public void sendPM(String sender, SocketChannel s, String user, String m){
        for(String i: allUsers.keySet()){
			if(i.equals(user)){
				String message = "PM from " + sender + ": " + m;
				try{
					ByteBuffer buff = ByteBuffer.wrap(message.getBytes());
					allUsers.get(i).write(buff);
					message = "PM to " + user + ": " + m;
					buff = ByteBuffer.wrap(message.getBytes());
					s.write(buff);
				}catch(Exception e){
					System.out.println("Error sending PM: " + e);
				}
			}
		}
		return;
    }

	public void kick(SocketChannel s, String user){
		//sendPM(user, "You have been kicked by an admin. Deuces.");
        user = user.trim();
		for(String i: allUsers.keySet()){
			if(i.equals(user)){
				SocketChannel kickee = allUsers.get(i);
				ByteBuffer b = ByteBuffer.wrap("You have been kicked by an admin.".getBytes());
				try{
					kickee.write(b);
					kickee.close();
                    System.out.println("Closed: " + user);
				}catch(Exception e){
					System.out.println("Error kicking user: " + e);
				}
				allUsers.remove(i);
                break;
			}
		}
		return;
	}

	public void exit(SocketChannel user){
        System.out.println("exiting...");
		try{
			user.close();
		}catch(Exception e){
			System.out.println("Error on exit: " + e);
		}
		return;
	}

    public void sendMessageAll(ByteBuffer b){
        for (SocketChannel s : allUsers.values()){
            try{
                b.position(0);
                s.write(b);
            }
            catch(Exception e){
                System.out.println("Error in send all.");
                System.out.println(e);
            }
        }

    }

    public void sendList(SocketChannel s){
        String names = "******** Online ********\n";
        for (String n : allUsers.keySet()){
            names += n + "\n";
        }
		names += "******** Online ********";
        try{
            ByteBuffer b = ByteBuffer.wrap(names.getBytes());
    		s.write(b);
        }
        catch(Exception e){
            System.out.println("Error sending list " + e);
        }
    }


    public static void main(String[] args){
        int portNum = 0;
        boolean correct = false;
        boolean first = true;
        while(!correct){
            if (args.length != 1 || !first){
                Console cons = System.console();
                String portString = cons.readLine("PortNum: ");
                portNum = Integer.parseInt(portString);

            }
            else{
                portNum = Integer.parseInt(args[0]);
                first = false;
            }

            if (portNum > 1024 && portNum < 65535)
                correct = true;
            else
                System.out.println("Invalid port. Try again.");
        }

        Server s = new Server(portNum);
    }

}

class SocketKey{
    private SocketChannel chan;
    private SecretKey key;

    public SocketKey(SocketChannel sc, SecretKey sk){
        chan = sc;
        key = sk;
    }

    public SocketChannel getChannel(){
        return sc;
    }

    public SecretKey getSecret(){
        return sk;
    }
}
