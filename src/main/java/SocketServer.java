import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
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
    private String FILE_PATH = "src\\main\\java\\file.log";
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 16);

    public SocketServer(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        this.session = new HashSet<>();
    }

    public void start() {
        try {
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
            channel.register(this.selector, SelectionKey.OP_READ);
            this.session.add(channel);
            sendMessageHistory(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int num = channel.read(byteBuffer);
            byteBuffer.flip();
            String stringByteBuffer = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
            if (num == -1) {
                this.session.remove(channel);
                message(disconnect);
                channel.close();
                key.cancel();
                return;
            } else {
                message(stringByteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void message(String s) {
        try {
            FileChannel input = FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            input.write(ByteBuffer.wrap((s).getBytes(charset)));
            input.close();
            byteBuffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session.forEach(socketChannel -> {
            try {
                socketChannel.write(ByteBuffer.wrap(s.getBytes(charset)));
                byteBuffer.flip();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //отправка истории сообщений при подключении клиента
    private void sendMessageHistory(SocketChannel channel) {
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
    }

}
