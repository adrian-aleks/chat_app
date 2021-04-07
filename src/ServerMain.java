public class ServerMain {
    public static void main(String[] args){
        int port = 6666;
        Server server = new Server(port);
        server.start();
    }
}
