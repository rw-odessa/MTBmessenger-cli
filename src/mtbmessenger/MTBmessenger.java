package mtbmessenger;

/**
 *
 * @author ua053903 v 0.0.4 2016-02-16 Клиент сообщений
 */
import java.io.*;
import java.net.*;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.json.JSONObject;
import org.json.JSONException;

public class MTBmessenger {

    /**
     * Single static instance of the service class
     */
    private static MTBmessenger serviceInstance = new MTBmessenger();

    /**
     * Static method called by prunsrv to start/stop the service. Pass the
     * argument "start" to start the service, and pass "stop" to stop the
     * service.
     */
    public static void main(String[] args) {

        String cmd = "start";
        String serverAdress;
        int serverPort = 3000;
        //String clientGroup;

        //Опредиление структуры параметров командной строки
        switch (args.length) {
            case 1:
                cmd = args[0];
                if ("stop".equals(cmd)) {
                    serviceInstance.stop();
                } else {
                    System.out.println("Info: One argument must be - STOP");
                    System.exit(1);
                }
                break;
            case 3:
                if ("start".equals(cmd)) {
                    serverAdress = args[1];
                    try {
                        serverPort = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println(args[2] + " - NumberFormatException - " + e);
                        System.out.println("Info: Third argument must be a number");
                        System.exit(1);
                    }
                    clientGroup = "all";
                    serviceInstance.start(serverAdress, serverPort, clientGroup);

                } else {
                    System.out.println("Info: First argument must be - START");
                    System.exit(1);
                }
                break;
            case 4:
                if ("start".equals(cmd)) {
                    serverAdress = args[1];
                    try {
                        serverPort = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println(args[2] + " - NumberFormatException - " + e);
                        System.out.println("Info: Third argument must be a number");
                        System.exit(1);
                    }
                    clientGroup = args[3];
                    serviceInstance.start(serverAdress, serverPort, clientGroup);

                } else {
                    System.out.println("Info: First argument must be - START");
                    System.exit(1);
                }
                break;
            default:
                System.out.println("Info: Need one argument STOP, or four arguments START IP-ADRESS PORT ROLE");
                //System.out.println("Example: ");
                System.exit(1);
        }

    } //end main
    /**
     * Flag to know if this service instance has been stopped.
     */
    private static Socket client = null;
    private static BufferedReader in = null;
    private static PrintWriter output = null;
    private static final int receiveMessageInterval = 500;
    private static final int testConnectionInterval = 60000;
    private static String testConectionString = "OK";
    private static String clientVersion = "MTBmessenger v 0.0.5";
    private static String clientGroup;
    private boolean stopped = false;
    private int testConnectionIntervalCounter = 0;

    /**
     * Start this service instance
     */
    public void start(String serverAdress, int serverPort, String clientGroup) {

        stopped = false;
        String msgStr;
        String groupFromJson = null;
        String msgFromJson = null;
        boolean error = true;
        boolean parseJsonSuccess = false;

        try {
            client = new Socket(serverAdress, serverPort);
            output = new PrintWriter(client.getOutputStream(), true); //listenToServer()
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            while (!stopped) {

                if (in.ready()) {
                    msgStr = in.readLine();

                    //JSON Psrse
                    try {
                        JSONObject incJson = new JSONObject(msgStr);
                        if (incJson.has("Group")) {
                            groupFromJson = incJson.getString("Group");
                        } else {
                            groupFromJson = "all";
                        }
                        //groupFromJson = incJson.getString("Group");

                        if (incJson.has("Msg")) {
                            msgFromJson = incJson.getString("Msg");
                        } else {
                            Iterator incJsonKeys = incJson.keys();
                            String compositeMsg = "";
                            while (incJsonKeys.hasNext()) {
                                String jsonKey = incJsonKeys.next().toString();
                                compositeMsg = compositeMsg
                                        + jsonKey
                                        + ": "
                                        + incJson.getString(jsonKey)
                                        + ", ";
                            }
                            msgFromJson = compositeMsg.trim();

                        }
                        //msgFromJson = incJson.getString("Msg");

                        parseJsonSuccess = true;
                    } catch (JSONException je) {
                        System.out.println("JSONException - " + je);
                    } finally {
                        if (!parseJsonSuccess) {
                            new NewMessage(msgStr);
                        } else {
                            if (groupFromJson.equals("all") || (groupFromJson.equals(clientGroup))) {
                                new NewMessage(msgFromJson);
                            }
                        }
                        parseJsonSuccess = false;
                    }
                    //System.out.println(new java.util.Date() + " Message: " + msgStr + " - received");
                    //send("Message: " + msgStr + " - received");



                }

                synchronized (this) {
                    this.wait(receiveMessageInterval); // wait
                }


                if (testConnectionIntervalCounter == 0) {
                    send(testConectionString);
                    if (output.checkError()) {
                        in.close();
                        output.close();
                        client.close();
                        System.out.println("Connection Error");

                        synchronized (this) {
                            this.wait(testConnectionInterval); // wait
                        }

                        client = new Socket(serverAdress, serverPort);
                        output = new PrintWriter(client.getOutputStream(), true); //listenToServer()
                        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    }
                } // if (testConnectionIntervalCounter == 0)

                if ((testConnectionIntervalCounter * receiveMessageInterval) > testConnectionInterval) {
                    testConnectionIntervalCounter = 0;
                } else {
                    testConnectionIntervalCounter++;
                }

            } // end while
            error = false;

        } catch (InterruptedException ie) {
            //error = true;
            System.out.println("InterruptedException - " + ie);
        } catch (SocketTimeoutException te) {
            //error = true;
            System.out.println("SocketTimeoutException - " + te);
        } catch (SocketException se) {
            //error = true;
            System.out.println("SocketException - " + se);
        } catch (UnknownHostException he) {
            //error = true;
            System.out.println("UnknownHostException - " + he);
        } catch (IOException ioe) {
            //error = true;
            System.out.println("IOException - " + ioe);
        } catch (Exception e) {
            //error = true;
            System.out.println("Exception - " + e);
        } finally {

            try {
                if (in != null) {
                    in.close();
                }
                if (output != null) {
                    output.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                error = true;
                System.out.println("IOException in finally section - " + e);
            }
        } //end finally

        if (error) {
            System.out.println("MTBmessenger Service Finished with error " + new java.util.Date());
            System.exit(1);
        } else {
            System.out.println("MTBmessenger Service Finished " + new java.util.Date());
            System.exit(0);
        }

    } //end start()

    /**
     * Stop this service instance
     */
    public void stop() {
        stopped = true;
        synchronized (this) {
            this.notify();
        }
    } //end stop()

    public static void send(String msg) {
        if (output != null) {
            //output.println(new java.util.Date() + " " + client.getLocalAddress() + ":" + client.getLocalPort() + " " + msg);
            /*   
             * {
             * "myDateTime":new java.util.Date(),
             * "myIP":client.getLocalAddress(),
             * "myPort":client.getLocalPort(),
             * "myMsg":msg
             * }
             **/
            /*
             output.println(
             "{"
             + "\"myVersion\":" + "\"" + clientVersion + "\", "
             + "\"myIP\":" + "\"" + client.getLocalAddress() + "\", "
             + "\"myPort\":" + "\"" + client.getLocalPort() + "\", "
             + "\"myGroup\":" + "\"" + clientGroup + "\", "
             + "\"myMsg\":" + "\"" + msg + "\", "
             + "\"myDateTime\":" + "\"" + new java.util.Date() + "\""
             + "}");
             */
            JSONObject res = new JSONObject();
            res.put("myVersion", clientVersion);
            res.put("myIP", client.getLocalAddress());
            res.put("myPort", client.getLocalPort());
            res.put("myGroup", clientGroup);
            res.put("myMsg", msg);
            res.put("myDateTime", new java.util.Date());
            output.println(res.toString());

        }
    }
}

// Create multiple threads.
class NewMessage implements Runnable {

    Thread t;
    String message, rawMessage; // message for thread

    NewMessage(String threadMessage) {
        rawMessage = threadMessage;
        try {
            message = new String(threadMessage.getBytes(), "UTF8");
        } catch (UnsupportedEncodingException ue) {
            System.out.println("UnsupportedEncodingException - " + ue);
            message = threadMessage;
        }
        t = new Thread(this);
        //System.out.println(new java.util.Date() + " Created new thread: " + t + " Message: " + rawMessage);
        System.out.println(new java.util.Date() + " Message: " + rawMessage + " - received");
        MTBmessenger.send("Message: " + rawMessage + " - received");
        t.start(); // Start the thread
    }

    // This is the entry point for thread.
    @Override
    public void run() {
        JOptionPane.showMessageDialog(null, message);
        //System.out.println("Thread: " + t + " Message: " + rawMessage + " - readed");
        System.out.println(new java.util.Date() + " Message: " + rawMessage + " - readed");
        MTBmessenger.send("Message: " + rawMessage + " - readed");
    }
}
