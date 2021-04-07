import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{
    private final int serverPort;
    private ArrayList<ClientHandler> clientList = new ArrayList<>();

    public Server(int serverPort){
        this.serverPort = serverPort;
    }
    public List<ClientHandler> getClientList(){return clientList;}

    public void start(){
        try{
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
                clientList.add(clientHandler);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ClientHandler clientHandler){
        clientList.remove(clientHandler);
    }
}
