import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//forma errada de fazer coisas não blocantes
public class NIOBlockingServerSelector {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {

        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.configureBlocking(false);
        ss.bind(new InetSocketAddress(8080));


        Selector selector = Selector.open();
        ss.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            selector.select();//blocking


            //receber uma lista de chaves de select
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext(); ) {
                SelectionKey key = it.next();

                it.remove();
                try {
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

                sockets.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
            }

        }
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        ByteBuffer bf = sockets.get(sc);

        sc.write(bf);
        if (!bf.hasRemaining()) {
            bf.compact();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private static void read(SelectionKey key) throws IOException {
        System.out.println("arrive");

        SocketChannel sc = (SocketChannel) key.channel();

        ByteBuffer bf = sockets.get(sc);
        int data = sc.read(bf);

        if (data == -1) {
            close(sc);
            sockets.remove(sc);
        }

        bf.flip();
        transmogrify(bf);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ss = (ServerSocketChannel) key.channel();

        SocketChannel s = ss.accept(); //non blocking, but never null
        System.out.println(s);
        s.configureBlocking(false);
        s.register(key.selector(), SelectionKey.OP_READ);
        sockets.put(s,  ByteBuffer.allocateDirect(10_000_000));
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
