import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOBlockingServer {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress(8080));

        while(true) {
            SocketChannel s = ss.accept(); // blocking until someone get in
            System.out.println(s);
            handle(s);
        }

    }

    private static void handle(SocketChannel s) {
        try {
            ByteBuffer buff = ByteBuffer.allocateDirect(80); //suficiente para uma linha

            int data;

            while ((data = s.read(buff)) != -1) {
                buff.flip(); //now we can read the data inside the buffer
                transmogrify(buff);

                while(buff.hasRemaining()){
                    s.write(buff);
                }

                buff.compact(); // set position to zero and capacity to eighty
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
