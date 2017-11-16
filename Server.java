import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

public class Server{
    private int portNum;
    private Map<String,SocketChannel> allUsers;

    public Server(int port){
        portNum = port;
        allUsers =  new ConcurrentHashMap<String,SocketChannel>();
        runServer();
    }

    public void runServer(){
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

    public void runThread(SocketChannel sc){
        try{
            //Get the clients username
            ByteBuffer userBuf = ByteBuffer.allocate(1024);
            sc.read(userBuf);
            String userName = new String(userBuf.array());
            userName = userName.trim();
            userName = removeSlash(userName);
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
                if (message.length() < 1)
                    break;
                message = userName + ": " + message.trim();

                System.out.println(message);

                buff = ByteBuffer.wrap(message.getBytes());
                sendMessageAll(buff);
            }

            sc.close();
            System.out.println(userName + " has left the chat.");
        }
        catch(IOException e){
            System.out.println("Exception in the thread.");
            System.out.println(e);
        }
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

    public void sendMessageAll(ByteBuffer b){
        for (SocketChannel s : allUsers.values()){
            try{
                s.write(b);
                System.out.println(s);
            }
            catch(Exception e){
                System.out.println("Error in send all.");
                System.out.println(e);
            }
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
