package Client;

import Server.Config;
import Server.SocketServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SocketClientTest {
    SocketClient socketClient = new SocketClient(Config.HOST, Config.PORT);
    String s="тест";
    String path_name="test.txt";

    @Test
    void writeToFile() throws IOException {
        Path newFilePath = Paths.get(path_name);
        Files.deleteIfExists(newFilePath);
        Files.createFile(newFilePath);
        assertTrue(socketClient.writeToFile(s,path_name));
    }

    @Test
    void send() throws Exception {
        ByteBuffer byteBuffer= socketClient.readyToSend(s);
        String result = new String(byteBuffer.array(), 0, byteBuffer.limit());
        assertEquals(s, result);

    }

    @Test
    void readyToQuit() {
        String ex = "/exit";
        assertTrue(socketClient.readyToQuit(ex));
    }

}