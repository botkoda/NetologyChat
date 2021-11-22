package Client;

public class MainClient {
    public static void main(String[] args) {

        new SocketClient(Config.HOST, Config.PORT).start();
    }
}
