import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DistributeWrite extends Thread{

  private Socket acpt_sock;
  private ConcurrentHashMap<Long, String > routing_table;
  private ConcurrentHashMap<String, String > checked_adr;
  private int num_copies;
  private ArrayList <String> failed;
  private ArrayList <String> done_write;

  public DistributeWrite(Socket acpt_sock, ConcurrentHashMap<Long, String > routing_table){
    this.acpt_sock = acpt_sock;
    this.routing_table = routing_table;
    checked_adr = new ConcurrentHashMap<String, String > ();
    num_copies = 3;
    failed = new ArrayList <String> ();
    done_write = new ArrayList <String> ();
  }

  public String [] findServers(){
    int size = routing_table.size();
    Long [] pings = routing_table.keySet().toArray(new Long [routing_table.size()]);
    Arrays.sort(pings);
    String [] copy_address;
    if(size > 2){
      copy_address = new String [num_copies];
      int count = 0;
      for( int indx = 0; indx < size; indx = indx + (size/num_copies)){
        String ip_port = routing_table.get(pings[indx]);
        if(failed.contains(ip_port)){
          for(int ip = 0; ip < size; ip ++){
            String other = routing_table.get(pings[indx]);
            if(! (failed.contains(other) & done_write.contains(other))){
              copy_address[count] = routing_table.get(pings[indx]);
              count ++;
            }
          }
        }
        else{
          copy_address[count] = routing_table.get(pings[indx]);
          count ++;
        }
      }
    }

    else if(size == 2){
      copy_address = new String [2];
      copy_address[0] = routing_table.get(pings[0]);
      copy_address[1] = routing_table.get(pings[1]);
      num_copies = 2;
    }
    else{
      copy_address = new String [1];
      copy_address[0] = routing_table.get(pings[0]);
      num_copies = 1;
    }
    return copy_address;
  }

  public String readInputStream(int bytes) throws IOException{
    byte[] content_rbuf = new byte[bytes];
    int data_length = acpt_sock.getInputStream().read(content_rbuf);
    String content = new String(content_rbuf, "US-ASCII");
    return content;
  }

  public void writeOutputStream(String msg) throws IOException{
    byte [] encode = msg.getBytes("US-ASCII");
    acpt_sock.getOutputStream().write(encode,0,encode.length);
    acpt_sock.close();
  }

  public String copy_file(String name){
    String fileName = "./json_files/" + name + ".json";
    JSONParser jsonParser = new JSONParser();

    try (FileReader reader = new FileReader(fileName))
    {
      Object obj = jsonParser.parse(reader);
      JSONObject json_content = (JSONObject) obj;
      return json_content.toJSONString();

    } catch (IOException | ParseException e) {
      e.printStackTrace();
      return "FAIL";
    }
  }


  public void run(){
    try{
      String content = readInputStream(1024);

      int firstBracket = content.indexOf('{');
      if(firstBracket == -1){
        String [] name_copies = content.trim().split("_");
        num_copies = Integer.valueOf(name_copies[1]);
        content = copy_file(name_copies[0]);
        if(content.equals("FAIL")){
          writeOutputStream("FAIL");
        }
      }
      String fileName =  content.substring(0,firstBracket);

      String [] copy_address = findServers();
      for(String adr: copy_address){
        String [] ip_port = adr.split(":");
        ServerComm copyWrite = new ServerComm(checked_adr, ip_port[0], Integer.valueOf(ip_port[1]), content, routing_table);
        copyWrite.start();
      }

      for(String adr: checked_adr.keySet()){
        if(! done_write.contains(adr)){
          if(checked_adr.get(adr).equals("true") ){
            done_write.add(adr);
            num_copies --;
          }
          else{
            System.out.println("failed adr: " + adr);
            failed.add(adr);
          }
        }
      }

      while(checked_adr.size() < num_copies){
        for(String adr: checked_adr.keySet()){
          if(! done_write.contains(adr)){
            if(checked_adr.get(adr).equals("true") ){ //true, false, live
              done_write.add(adr);
              num_copies --;
            }
            else{
              failed.add(adr);
            }
          }
        }

        if(failed.size() == routing_table.size()){
          writeOutputStream("FAIL");
        }

        if(failed.size()> 0){
          copy_address = findServers();
          for(String adr: copy_address){
            String [] ip_port = adr.split(":");
            ServerComm copyWrite = new ServerComm(checked_adr, ip_port[0], Integer.valueOf(ip_port[1]), content, routing_table);
            copyWrite.start();
          }
        }
      }

      String writes_done = "DONE" + fileName + done_write.toString();
      writeOutputStream(writes_done);
    }
    catch(IOException e){}
  }
}
