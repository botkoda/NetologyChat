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
            new Thread(new UserInput(this)).start();
            FileChannel.open(Paths.get(FILE_PATH), StandardOpenOption.WRITE).truncate(0).close();
            clientSocketChannel.configureBlocking(false);
        }
        client.register(this.selector, SelectionKey.OP_READ);
    }


    private void read(SelectionKey key) {
        try {
            SocketChannel clientSocketChannel = (SocketChannel) key.channel();
            byteBuffer.clear();
            int num = clientSocketChannel.read(byteBuffer);
            if (num == -1) {
                clientSocketChannel.close();
                key.cancel();
                return;
            } else {
                byteBuffer.flip();
                String msg = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
                writeToFile(msg,FILE_PATH);
                System.out.println(msg);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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


    public ByteBuffer readyToSend(String msg) throws Exception {
        byteBuffer.clear();
        byteBuffer.put(charset.encode(msg));
        byteBuffer.flip();

        return byteBuffer;
    }

    void write(String dateTime, String name, String msg){
        try {
            byteBuffer=readyToSend(dateTime + " " + name + ": " + msg + "\r\n");
            clientSocketChannel.write(byteBuffer);
            if (readyToQuit(msg)) {
                clientSocketChannel.close();
                selector.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

}
