import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import java.util.concurrent.ConcurrentHashMap;
import java.lang.StringBuilder;
import java.util.Arrays;

@SuppressWarnings("unchecked")
//javac -cp 'json-simple-1.1.1.jar' readFile.java

public class ReadFile extends Thread{

  private Socket acpt_sock;
  private String file_content;
  // private String [] dir;

  public ReadFile(Socket acpt_sock){
    this.acpt_sock = acpt_sock;
  }

  public boolean readContent(String content){

    System.out.println(content);

    //
    // if(content.contains(".")){
    //   System.out.println("contains .");
    //   System.out.println(content);
    //   dir = content.split(".");
    //   System.out.println(dir[0]);
    //   for(String i : dir){
    //     System.out.println("wtf"+i);
    //   }
    // }
    // else{
    //   System.out.println("not contains .");
    //   dir = new String [1];
    //   dir[0] = content;
    // }
    //
    // System.out.println(Arrays.toString(dir));

    String [] dir = content.split("\\.");

    String fileName = "./json_files/" + dir[0].trim() + ".json";

    JSONParser jsonParser = new JSONParser();

    try (FileReader reader = new FileReader(fileName))
    {
        Object obj = jsonParser.parse(reader);

        JSONObject json_content = (JSONObject) obj;
        System.out.println(json_content);

        //file_content = json_content.toString();
        if(dir.length > 1){
          parseJsonObj( json_content, dir );
        }
        else{
          file_content = json_content.toString();
        }

    } catch (IOException | ParseException e) {
        e.printStackTrace();
        return false;
    }
    return true;
  }

  public void parseJsonObj(JSONObject json_content, String [] dir){
    System.out.println("here");
    int nested = 1; //0 is the file name
    System.out.println(dir[nested]);
    String firstName = (String) json_content.get("school");
    System.out.println(firstName);
    while(nested < dir.length - 1 ){
      json_content = (JSONObject) json_content.get(dir[nested]);
      nested ++;
    }
    System.out.println(nested);
    file_content = (String) json_content.get(dir[nested]);
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
      readContent(content);
      System.out.println("finished read");
      System.out.println(file_content);
      byte [] encode = file_content.getBytes("US-ASCII");
      acpt_sock.getOutputStream().write(encode,0,encode.length);
    }
    catch(IOException e){

    }
  }
}

/*
hub sends write to server it is offical connect too
offical server picks main and other servers to duplicate
offical server pass along query, need flag to distribute or not

once query is recieved

1) parse file name and json
2) create file in server id folder with json named with the file name
3) send back task completed

*/
