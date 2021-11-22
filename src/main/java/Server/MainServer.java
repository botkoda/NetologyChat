package Server;

public class MainServer {
    public static void main(String[] args) {
        
        new SocketServer(Config.HOST, Config.PORT).start();
    }
}
