import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//forma errada de fazer coisas não blocantes
public class NIOBlockingServerPolling {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.configureBlocking(false);
        ss.bind(new InetSocketAddress(8080));

        Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

        while(true) {
            SocketChannel s = ss.accept();


            if (s != null ) {
                System.out.println(s);
                s.configureBlocking(false);
                sockets.put(s, ByteBuffer.allocateDirect(80));
                //handle(s); // blocking call
            }

            sockets.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());

            sockets.forEach(NIOBlockingServerPolling::handle);
        }

    }

    private static void handle(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        try {
            int data = socketChannel.read(byteBuffer);

            if (data == -1) {
                close(socketChannel);
            } else if (data != 0){
                byteBuffer.flip();
                transmogrify(byteBuffer);
                while(byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                byteBuffer.compact();
            }

        } catch (IOException e) {
            close(socketChannel);
            throw new UncheckedIOException(e);
        }
    }

    private static void close(SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {

        }
    }

    private static void transmogrify(ByteBuffer data) {
        for (int i = 0; i < data.limit(); i++) {
            data.put(i, (byte) transmogrify(data.get(i)));
        }
    }

    private static int transmogrify(int data) {
        return Character.isLetter(data) ? data ^ ' ' : data;
    }
}
