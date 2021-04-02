import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{
    static Vector<ClientHandler> active_clients = new Vector<>();
    static int client_counter = 0;

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket serSoc = new ServerSocket(1234);
        Socket soc;

        //infinite loop for handling incoming clients requests
        while (true)
        {
            // Accept the incoming request
            soc = serSoc.accept();

            System.out.println("New client request received : " + soc);

            // obtain input and output streams
            DataInputStream dis = new DataInputStream(soc.getInputStream());
            DataOutputStream dos = new DataOutputStream(soc.getOutputStream());

            System.out.println("Creating a new handler for this client...");

            ClientHandler mtch = new ClientHandler(soc,"client " + client_counter, dis, dos);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            System.out.println("Adding this client to active client list");

            // add this client to active clients list
            active_clients.add(mtch);

            // start the thread.
            t.start();
            client_counter++;

        }
    }
}
