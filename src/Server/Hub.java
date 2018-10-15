
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.ConnectException;


public class Hub {
    private String null_mesg = "\0";

    public Long UDP_PingTime(String address, int port) throws IOException, UnsupportedEncodingException {
        DatagramSocket sock;
        InetAddress server_address;

        // Setup the server side connection data
        try {
            server_address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            //System.exit(1);
            return null;
        }

        // Make the socket, with a 1 second timeout
        try {
            sock = new DatagramSocket();
            sock.setSoTimeout(1000);
        } catch(SocketException e) {
            System.err.println("hub Cannot create the socket.");
            //System.exit(1);
            return null;
        }

        // Prepare the null message
        DatagramPacket packet = new DatagramPacket(null_mesg.getBytes("US-ASCII"),1,server_address,port);
        int capacity = 3;
        long sum = Long.valueOf(0);
        int counter = 0;
        while(counter != capacity){
            Long time = pingTime(sock, packet);
            if(time > Long.valueOf(0)){
                sum += time;
                counter ++;
            }
        }
        return sum / Long.valueOf(capacity);
    }

    public Long pingTime(DatagramSocket sock, DatagramPacket packet) throws IOException{
        Long startTime = System.nanoTime();
        sock.send(packet);

        // Wait up to 1 second for null message
        Long endTime = Long.valueOf(0);
        int retries=0;
        while( retries<4 ) {
            try {
                sock.receive(packet);
                endTime = System.nanoTime();
                break;
            } catch(SocketTimeoutException ste) {
                // packet was probably lost, retry
                sock.send(packet);
                retries++;
            }
        }
        if(retries>3) {
            System.err.println("Server does not exist");
            //TODO: server does not exist
            sock.close();
            System.exit(1);
        }

        // Check the response string
        String pong = new String(packet.getData(), "US-ASCII");

        // Check that the response is expected
        if(pong.equals(null_mesg)) {
            return endTime - startTime;
        }
        return null;
    }

   public void TCP_operation(String address, int port) throws IOException, UnsupportedEncodingException {
       Socket sock;
       InetAddress server_address;
       InetSocketAddress endpoint;

       // Setup the server side connection data
       server_address = InetAddress.getByName(address);
       endpoint = new InetSocketAddress(server_address, port);

       //// Make the TCP connection
       try {
           sock = new Socket();
           sock.setSoTimeout(5000);
       } catch(SocketException e) {
           System.err.println("Cannot create the socket.");
           System.exit(1);
           return;
       }

       // Make the connection
       try {
           sock.connect(endpoint);
       } catch(ConnectException e) {
           System.err.println("Cannot connect to server.");
           System.exit(1);
           return;
       }

      String content = "{\"mean\":\"10\", \"school\":\"CC\"}"; //all entries new a \"
      String message = "RITE" + "Sunny" + content;

       //String message = "READSunny.school";
       //String message = "READSunny";

       byte [] encode = message.getBytes("US-ASCII");
       // Send the HELO
       sock.getOutputStream().write(encode,0,encode.length);

       //// Wait up to 5 seconds for PONG
       while(true){
         byte[] rbuf = new byte[10];
         int ret = sock.getInputStream().read(rbuf);

         // Check the response string
         if(ret > 0){
           String pong = new String(rbuf, "US-ASCII");
           System.out.println(pong);
         }

       }

   }
}
