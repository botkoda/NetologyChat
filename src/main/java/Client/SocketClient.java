package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class SocketClient {
    private Selector selector;
    private InetSocketAddress address;
    private SocketChannel clientSocketChannel;
    private String startClient = "клиент запустился..\r\n";
    private String disconnect = "Селектор закрылся, клиент отключился";
    private Charset charset = Charset.forName("UTF-8");
    private String FILE_PATH = "src\\main\\java\\Client\\file.log";
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 16);
    private final String QUIT = "/exit";

    public SocketClient(String host, int port) {
        this.address = new InetSocketAddress(host, port);
    }

    public void start() {
        try {
            clientSocketChannel = SocketChannel.open();
            clientSocketChannel.configureBlocking(false);
            selector = Selector.open();
            clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
            clientSocketChannel.connect(address);
            System.out.println(startClient);
            while (true) {
                selector.select();
                Iterator keys = this.selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = (SelectionKey) keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;
                    if (key.isConnectable()) connect(key);
                    else if (key.isReadable()) read(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            System.out.println(disconnect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        if (client.isConnectionPending()) {
            client.finishConnect();
            new Thread(new UserInputHander(this)).start();
            clientSocketChannel.configureBlocking(false);
            FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.WRITE).truncate(0).close();
        }
        client.register(this.selector, SelectionKey.OP_READ);
    }


    private void read(SelectionKey key) {
        try {
            FileChannel input = FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            SocketChannel clientSocketChannel = (SocketChannel) key.channel();
            byteBuffer.clear();
            int num = clientSocketChannel.read(byteBuffer);
            byteBuffer.flip();
            String msg = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
            input.write(ByteBuffer.wrap(msg.getBytes(charset)));
            if (num == -1) {
                clientSocketChannel.close();
                key.cancel();
                input.close();
                return;
            } else {
                System.out.println(msg);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(String dateTime, String name, String msg) throws Exception {
        if (msg.isEmpty()) {
            return;
        }
        byteBuffer.clear();
        byteBuffer.put(charset.encode(dateTime + " " + name + ": " + msg + "\r\n"));
        byteBuffer.flip();
        //while (byteBuffer.hasRemaining()) {
        clientSocketChannel.write(byteBuffer);
        //  }
        byteBuffer.clear();

        if (readyToQuit(msg)) {
            clientSocketChannel.close();
            selector.close();
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

}
