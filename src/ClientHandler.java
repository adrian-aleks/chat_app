// ClientHandler class
import java.io.*;
import java.util.*;
import java.net.*;
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket soc;
    boolean loggedIn;

    // constructor
    public ClientHandler(Socket soc, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.soc = soc;
        this.loggedIn =true;
    }

    @Override
    public void run() {
        String received;
        while(true)
        {
            try
            {
                // receive the string
                received = dis.readUTF();

                System.out.println(received);

                if(received.equals("logout")){
                    this.loggedIn =false;
                    this.soc.close();
                    break;
                }

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String MsgToSend = st.nextToken();
                String recipient_number = st.nextToken();

                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for(ClientHandler recipient : Server.active_clients)
                {
                    // if the recipient is found, write on its
                    // output stream
                    if (recipient.name.equals(recipient_number) && recipient.loggedIn) {
                        recipient.dos.writeUTF(this.name+" : "+MsgToSend);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}