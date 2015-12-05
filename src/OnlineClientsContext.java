import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by Georgi on 15.11.2015 г..
 */
public class OnlineClientsContext {
    private static HashMap<Integer, BufferedWriter> onlineClients = new HashMap<>();
    private static Integer clientCount = 0;

    public synchronized static Integer getClientId() {
        Integer newId = clientCount;
        clientCount++;
        return clientCount;
    }

    public static synchronized BufferedWriter getClientOutputStream(Integer clientID) {
        return onlineClients.get(clientID);
    }

    public static synchronized void setClientOutputStream(Integer clientID, BufferedWriter outputStream) {
        onlineClients.put(clientID, outputStream);
    }

    public static Integer[] getAllClients() {
        return (Integer[]) onlineClients.keySet().toArray(new Integer [onlineClients.size()]);
    }

    public static boolean hasClient(Integer clientID){
        return onlineClients.containsKey(clientID);
    }

    public static void closeStream(Integer clientId) {
        try {
            getClientOutputStream(clientId).close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            onlineClients.remove(clientId);
        }
    }

    public static void closeAllStreams() {
        onlineClients.values().forEach((bufferedWriter) -> {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
