import java.io.IOException;
import java.net.Socket;

public class GrumpyChump {

    public static void main(String[] args) throws InterruptedException {

        Socket[] sockets = new Socket[3000];

        for (int i = 0; i < sockets.length; i++) {
            try {
                sockets[i] = new Socket("localhost", 8080);
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        Thread.sleep(10_000_0_000);
    }


    /**
     * eventualmente vai ocorrer um outofmemory error
     * pq normalmente nossos processos são limitados aos
     * número de threads que podemos acomodar até mesmo
     * se eles não estiverem fazendo nada, se eles estiverem
     * somente bloqueando, nós não podemos continuar
     */
}
