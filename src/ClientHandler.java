import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Server server;
    private String nick = null;
    private OutputStream outputStream;
    private HashSet<String> roomSet = new HashSet<>();

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            for(String token : tokens){
                System.out.println(token);
            }
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd)) {
                    handleQuit();
                    break;
                } else if ("setnick".equalsIgnoreCase(cmd)) {
                    handleSetNick(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = Arrays.copyOfRange(tokens,1, tokens.length);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown command: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String room = tokens[1];
            roomSet.remove(room);
        }
    }

    public boolean isRoomMember(String room) {
        return roomSet.contains(room);
    }

    private void handleJoin(String[] tokens) throws IOException {
        if (tokens.length > 1) {
            String room = tokens[1];
            roomSet.add(room);
            String outMsg = "You joined " + room + " room\n";
            outputStream.write(outMsg.getBytes());
        }
    }

    // msg login body
    // msg" #room body
    // TODO fix truncated message body
    private void handleMessage(String[] msg) throws IOException {

        String sendTo = msg[0];
        String body = msg[1];

        boolean isRoom = sendTo.charAt(0) == '#';

        List<ClientHandler> clientList = server.getClientList();
        for(ClientHandler client : clientList) {
            if (isRoom) {
                if (client.isRoomMember(sendTo.substring(1))) {
                    String outMsg = "msg " + sendTo + ":" + nick + " " + body + "\n";
                    client.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(client.getNick())) {
                    String outMsg = "msg " + nick + " " + body + "\n";
                    client.send(outMsg);
                }
            }
        }
    }

    private void handleQuit() throws IOException {
        server.removeClient(this);
        List<ClientHandler> clientList = server.getClientList();

        // broadcast current user's status
        String onlineMsg = "offline " + nick + "\n";
        for(ClientHandler client : clientList) {
            if (!nick.equals(client.getNick())) {
                client.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getNick() {
        return nick;
    }

    private void handleSetNick(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String nick = tokens[1];
            List<ClientHandler> clientList = server.getClientList();

            boolean isNickTaken = false;
            for(ClientHandler client : clientList) {
                if (client.getNick() != null) {
                    if (nick.equals(client.getNick())) {
                        isNickTaken = true;
                        break;
                    }
                }
            }

            if ( ! isNickTaken ) {
                String msg = "ok nick\n";
                outputStream.write(msg.getBytes());
                this.nick = nick;
                System.out.println("User logged in successfully: " + nick);

                // send current user all other online logins
                for(ClientHandler client : clientList) {
                    if (client.getNick() != null) {
                        if (!nick.equals(client.getNick())) {
                            String msg2 = "online " + client.getNick() + "\n";
                            send(msg2);
                        }
                    }
                }

                // broadcast other online users current user's status
                String onlineMsg = "online " + nick + "\n";
                for(ClientHandler worker : clientList) {
                    if (!nick.equals(worker.getNick())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "This nickname is already taken, try again.\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if (nick != null) {
            outputStream.write(msg.getBytes());
        }
    }
}