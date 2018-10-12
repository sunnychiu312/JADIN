import java.net.Socket;
import java.net.ServerSocket;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class DistributeWrite extends Thread{

  private Socket acpt_sock;
  private ConcurrentHashMap<Long, String > routing_table;
  List<String> checked_adr;

  public DistributeWrite(Socket acpt_sock, ConcurrentHashMap<Long, String > routing_table){
    this.acpt_sock = acpt_sock;
    this.routing_table = routing_table;
    checked_adr = Collections.synchronizedList(new ArrayList<String>());
  }

  public String [] findServers(){
    int size = routing_table.size();

    Long [] pings = routing_table.keySet().toArray(new Long [0]);
    Arrays.sort(pings);

    String [] copy_address;
    if(size > 2){
      int num_copies = 3;
      copy_address = new String [num_copies];
      int count = 0;
      for( int indx = 0; indx < size; indx = indx + (size/num_copies)){
        copy_address[count] = routing_table.get(pings[indx]);
        count ++;

      }
    }
    else{
      copy_address = new String [2];
      copy_address[0] = routing_table.get(pings[0]);
      copy_address[1] = routing_table.get(pings[1]);
    }
    return copy_address;
  }

  public String readInputStream(Socket client, int bytes) throws IOException{
    byte[] content_rbuf = new byte[bytes];
    int data_length = client.getInputStream().read(content_rbuf);
    String content = new String(content_rbuf, "US-ASCII");
    return content;
  }

  public void run(){
    try{
      String content = readInputStream(acpt_sock, 1024);
      String [] copy_address = findServers();
      for(String adr: copy_address){
        String [] ip_port = adr.split(":");
        ServerComm copyWrite = new ServerComm(checked_adr, ip_port[0], Integer.valueOf(ip_port[1]), content);
        copyWrite.start();
      }
      synchronized(checked_adr)
      {
        String writes_done = "DONE" + checked_adr.toString();
        byte [] encode = writes_done.getBytes("US-ASCII");
        acpt_sock.getOutputStream().write(encode,0,encode.length);
        acpt_sock.close();
      }
    }
    catch(IOException e){}
  }
}
