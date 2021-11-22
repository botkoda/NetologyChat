package Server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class SocketServerTest {
    SocketServer serverSocket = new SocketServer(Config.HOST, Config.PORT);
    String s = "тест";
    String path_name = "test.txt";

    @Test
    void writeToFile() throws IOException {
        Path newFilePath = Paths.get(path_name);
        Files.deleteIfExists(newFilePath);
        Files.createFile(newFilePath);
        assertTrue(serverSocket.writeToFile(s, path_name));
    }

    @Test
    void sendMessageHistory() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);
        buffer.put(s.getBytes(StandardCharsets.UTF_8));
        RunTestServer();
        SocketChannel channel = testClinet();
        Path newFilePath = Paths.get(path_name);
        Files.deleteIfExists(newFilePath);
        Files.createFile(newFilePath);
        Files.write(newFilePath, "тест".getBytes(StandardCharsets.UTF_8));
        ByteBuffer byteBuffer = serverSocket.sendMessageHistory(channel, path_name);
        String result = new String(byteBuffer.array(), 0, byteBuffer.limit());
        String expected = new String(buffer.array(), 0, buffer.limit());
        assertEquals(expected, result);
    }

    private void RunTestServer() throws IOException {
        Thread thread = new Thread(() -> {
            try {
                ServerSocketChannel.open().bind(new InetSocketAddress(Config.PORT)).accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private SocketChannel testClinet() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(Config.HOST, Config.PORT);
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(socketAddress);
        return socketChannel;
    }
}
