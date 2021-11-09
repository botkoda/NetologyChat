public class Main {
    public static void main(String[] args) {
        new SocketServer(Config.HOST,Config.PORT).start();
    }
}
