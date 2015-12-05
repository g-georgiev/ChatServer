import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Georgi on 15.11.2015 г..
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final int port = 8008;

    public ChatServer() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        for (; ; ) {
            try {
                clientSocket = serverSocket.accept();
                executor.execute(new ChatServerTask(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*executor.shutdown();
        if (inputStream != null) {
            try {
                inputStream.close();
                OnlineClientsContext.closeAllStreams();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public static void main(String[] args) {
        new ChatServer().listen();
    }
}
