import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadHubCheck extends Thread {

    ConcurrentHashMap<String, String> hub_status;
    String[] whoami;

    public ThreadHubCheck(ConcurrentHashMap<String, String> _hashmap, String[] _whoami) {
        this.hub_status = _hashmap;
        this.whoami = _whoami;

    }

    public void run() {
        try {
            check_hubs();
        } catch(IOException e) {
            System.out.println("checkingThread error");
        }
    }

    public void check_hubs() throws IOException {
        for (String key : hub_status.keySet()) {
            String ip = key.split(":")[0];
            int port = Integer.valueOf(key.split(":")[1]);
            if (key.equals(whoami[0] + ":" + whoami[1])) {
                //do nothing since we don't want to try to socket to ourself
            }
            else {   //we are not checking ourself
                try {
                    Socket sock = new Socket();
                    InetAddress serveraddress = InetAddress.getByName(ip);
                    InetSocketAddress endpoint = new InetSocketAddress(serveraddress, port );
                    sock.connect(endpoint);
                    sock.getOutputStream().write("HUBS".getBytes("US-ASCII"));
                    byte[] ret = new byte[4];
                    sock.getInputStream().read(ret);
                    String returntype = new String(ret, "US-ASCII");
                    if (returntype.equals("ACPT")) {
                        if (hub_status.get(key).equals("unreachable")) {
                            System.out.println("there is a rechable hub now DEBUG");
                            hub_status.replace(key, "reachable");
                        }
                        else if (hub_status.get(key).equals("reachable")) {
                            //do nothing since it is still reachable
                        }
                    }
                    sock.close();
                } catch (SocketException e) {
                    System.out.println("TESTING socket exceptionm HUB is unavailable " + key);
                    //#TODO maybe we dont need to do this line
                    hub_status.replace(key, "unreachable");
                }

            }

        }
    }

}
