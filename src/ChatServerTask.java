import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Georgi on 15.11.2015 г..
 */
public class ChatServerTask implements Runnable {
    private final Socket clientSocket;
    private String message;

    public ChatServerTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Process INIT command
     *
     * @param clientId Id for client requesting initialization
     * @return HashMap with messages to be sent
     */
    private HashMap<Integer, String> commandINIT(String clientId) {
        HashMap<Integer, String> ret = new HashMap<>();
        StringBuffer messageBuffer = new StringBuffer();
        StringBuffer newClientINITMsg = new StringBuffer();

        /* set new client's output stream in context */
        Integer clientIdInt = Integer.parseInt(clientId);
        if (!OnlineClientsContext.hasClient(clientIdInt)) {
            try {
                OnlineClientsContext.setClientOutputStream(clientIdInt, new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* prepare messages to notify ALL other online clients that a new client is online */
        for (Integer destinationClientId : OnlineClientsContext.getAllClients()) {
            if (!destinationClientId.equals(Integer.parseInt(clientId))) {
                //append source client id
                messageBuffer.append(clientId).append("&");
                //append destination client id
                messageBuffer.append(destinationClientId).append("&");
                //append message body
                messageBuffer.append("ADD");

                ret.put(destinationClientId, messageBuffer.toString());
                messageBuffer.delete(0, messageBuffer.length());

                //append source clients ids
                newClientINITMsg.append(destinationClientId).append(";");
            }
        }

        /* prepare INIT message for new client */
        if(newClientINITMsg.length() != 0) {
            newClientINITMsg.deleteCharAt(newClientINITMsg.length()-1);
        }
        newClientINITMsg.append("&");
        //append destination client id
        newClientINITMsg.append(clientId).append("&");
        //append message body
        newClientINITMsg.append("INIT");

        ret.put(Integer.parseInt(clientId), newClientINITMsg.toString());

        return ret;
    }


    /**
     * Process DISCONNECT command
     *
     * @param clientId Id to be disconnected
     * @return HashMap with messages to be sent
     */
    private HashMap<Integer, String> commandDISCONNECT(String clientId) {
        HashMap<Integer, String> ret = new HashMap<>();
        StringBuffer messageBuffer = new StringBuffer();

        /* remove client's output stream from context */
        OnlineClientsContext.closeStream(Integer.parseInt(clientId));

        /* prepare messages to notify ALL other online clients that a new client is online */
        for (Integer destinationClientId : OnlineClientsContext.getAllClients()) {
                //append source client id
                messageBuffer.append(clientId).append("&");
                //append destination client id
                messageBuffer.append(destinationClientId).append("&");
                //append message body
                messageBuffer.append("REMOVE");

                ret.put(destinationClientId, messageBuffer.toString());
                messageBuffer.delete(0, messageBuffer.length());
        }

        return ret;
    }

    /**
     * Send message to client. Method is synchronous
     *
     * @param clientId Id for destination client
     * @param message  Message to be sent
     */
    private synchronized void sendMessage(Integer clientId, String message) {
        System.out.println("Msg sent: " + message + " to " + clientId.toString());
        try {
            OnlineClientsContext.getClientOutputStream(clientId).write(message);
            OnlineClientsContext.getClientOutputStream(clientId).newLine();
            OnlineClientsContext.getClientOutputStream(clientId).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            message = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Msg rec: " + message);
        /*
            Part1: Source client id OR NADA
            Part2: Destination client(s) id(s) OR "COMMAND"
            Part3: Message body OR specific command
         */
        String[] clientMessageParts = message.split("&");
        HashMap<Integer, String> messages = null;
        if (clientMessageParts[1].equals("COMMAND")) {
            //process command
            switch (clientMessageParts[2]) {
                case "INIT": {
                    messages = commandINIT(OnlineClientsContext.getClientId().toString());
                    break;
                }

                case "DISCONNECT": {
                    messages = commandDISCONNECT(clientMessageParts[0]);
                    break;
                }
            }
        } else {
            //prepare new messages for destination clients
            messages = new HashMap<>();
            StringBuffer messageBuffer = new StringBuffer();
            for (String destinationClientId : clientMessageParts[1].split(";")) {
                messages.put(Integer.parseInt(destinationClientId), message);
            }
        }

        //all your message are send
        for (Integer destinationClientId : messages.keySet()) {
            sendMessage(destinationClientId, messages.get(destinationClientId));
        }
    }
}
