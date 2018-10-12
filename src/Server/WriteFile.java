import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

//javac -cp 'json-simple-1.1.1.jar' WriteFile.java

public class WriteFile extends Thread{

  private Socket acpt_sock;
  private String id;

  public WriteFile(Socket acpt_sock,  String id){
    this.acpt_sock = acpt_sock;
    this.id = id;
  }

  public boolean writeContent(String content){
    int firstBracket = content.indexOf('{');
    String fileName = id + "/" + content.substring(0,firstBracket) + ".json";
    String JSON_content = content.substring(firstBracket, content.length());

    try(FileWriter file = new FileWriter(fileName)){
            file.write(JSON_content);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return true;
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
      String final_result;
      if (writeContent(content)){
        final_result = "DONE";
      }
      else{
        final_result = "FAIL";
      }
      byte [] encode = final_result.getBytes("US-ASCII");
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
