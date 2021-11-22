package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SocketServer {
    private Selector selector;
    private InetSocketAddress address;
    private Set<SocketChannel> session;
    private String startServ = "сервер запустился..\r\n";
    private String disconnect = "отключился..\r\n";
    private Charset charset = Charset.forName("UTF-8");
    private String FILE_PATH = "src\\main\\java\\Server\\file.log";
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 16);

    public SocketServer(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        this.session = new HashSet<>();
    }

    public void start() {
        try {
            Path newFilePath = Paths.get(FILE_PATH);
            if (!Files.exists(newFilePath)) {
                Files.createFile(newFilePath);
            }
            this.selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(address);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println(startServ);
            while (true) {
                this.selector.select();
                Iterator keys = this.selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = (SelectionKey) keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) accept(key);
                    else if (key.isReadable()) read(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            session.add(channel);
            sendMessageHistory(channel, FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int num = channel.read(byteBuffer);
            if (num == -1) {
                session.remove(channel);
                message(disconnect);
                channel.close();
                key.cancel();
                return;
            } else {
                byteBuffer.flip();
                String stringByteBuffer = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
                message(stringByteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void message(String s) {
        writeToFile(s, FILE_PATH);
        session.forEach(socketChannel -> {
            try {
                socketChannel.write(ByteBuffer.wrap(s.getBytes(charset)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean writeToFile(String s, String FILE_PATH) {
        try {
            FileChannel input = FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            input.write(ByteBuffer.wrap(s.getBytes(charset)));
            input.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ByteBuffer sendMessageHistory(SocketChannel channel, String FILE_PATH) {
        try {
            FileChannel output = FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.READ);
            byteBuffer.clear();
            while (output.read(byteBuffer) != -1) {
                byteBuffer.flip();
                channel.write(byteBuffer);
                byteBuffer.compact();
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer;
    }
}
