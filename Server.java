import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Server{
    private int portNum;

    public Server(int port){
        portNum = port;
        runServer();
    }

    public void runServer(){
        try{
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress(portNum));
            System.out.println("Server conected...");

            while (true){
                SocketChannel sc = channel.accept();
                TcpServerThread t = new TcpServerThread(sc);
                t.start();
            }
        }
        catch(IOException e){
            System.out.println("Exception in the server class.");
            System.out.println(e);
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

class TcpServerThread extends Thread{
    SocketChannel sc;

    TcpServerThread(SocketChannel s){
        sc = s;
        System.out.println("Thread created");
    }

    public void run(){
        try{
            //Get the clients username
            ByteBuffer userBuf = ByteBuffer.allocate(1024);
            sc.read(userBuf);
            String userName = new String(userBuf.array());

            boolean connected = true;
            while (connected){
                ByteBuffer buff = ByteBuffer.allocate(1024);
                sc.read(buff);
                String message = new String(buff.array());
                message = message.trim();
                System.out.println(message);
                buff.flip();
                sc.write(buff);
            }

            sc.close();
        }
        catch(IOException e){
            System.out.println("Exception in the thread class.");
            System.out.println(e);

        }
    }
}
