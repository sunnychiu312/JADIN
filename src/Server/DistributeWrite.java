import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class DistributeWrite extends Thread{

  private Socket acpt_sock;
  private ConcurrentHashMap<Long, String > routing_table;
  ConcurrentHashMap<String, String > checked_adr;

  public DistributeWrite(Socket acpt_sock, ConcurrentHashMap<Long, String > routing_table){
    this.acpt_sock = acpt_sock;
    this.routing_table = routing_table;
    checked_adr = new ConcurrentHashMap<String, String > ();
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
    else if(size == 2){
      copy_address = new String [2];
      copy_address[0] = routing_table.get(pings[0]);
      copy_address[1] = routing_table.get(pings[1]);
    }
    else{
      copy_address = new String [1];
      copy_address[0] = routing_table.get(pings[0]);
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

      int firstBracket = content.indexOf('{');
      String fileName =  content.substring(0,firstBracket);

      String [] copy_address = findServers();
      for(String adr: copy_address){
        String [] ip_port = adr.split(":");
        ServerComm copyWrite = new ServerComm(checked_adr, ip_port[0], Integer.valueOf(ip_port[1]), content);
        copyWrite.start();
      }

      ArrayList <String> done_write = new ArrayList <String> ();

      for(String adr: checked_adr.keySet()){
        System.out.println(adr);
        if(checked_adr.get(adr).equals("DONE")){
          done_write.add(adr);
        }
      }
      String writes_done = "DONE" + fileName + done_write.toString();
      byte [] encode = writes_done.getBytes("US-ASCII");
      acpt_sock.getOutputStream().write(encode,0,encode.length);
      acpt_sock.close();
    }
    catch(IOException e){}
  }
}
