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

@SuppressWarnings("unchecked")
//javac -cp 'json-simple-1.1.1.jar' readFile.java

public class ReadFile extends Thread{

  private Socket acpt_sock;
  private String id;
  private StringBuilder results;

  public ReadFile(Socket acpt_sock, String id){
    this.acpt_sock = acpt_sock;
    this.id = id;
    results = new StringBuilder();
  }

  public boolean readContent(String content){

    String [] dir;
    if(content.contains(".")){
      dir = content.split(".");
    }
    else{
      dir = new String [1];
      dir[0] = content;
    }

    String fileName = id + "/" + dir[0].trim() + ".json";

    JSONParser jsonParser = new JSONParser();

    try (FileReader reader = new FileReader(fileName))
    {
        Object obj = jsonParser.parse(reader);

        JSONArray contentList = (JSONArray) obj;
        System.out.println(contentList);


        if(dir.length > 1){
          contentList.forEach( json_content -> parseJsonObj( (JSONObject) json_content, dir ) );
        }

    } catch (FileNotFoundException e) {
        e.printStackTrace();
        return false;

    } catch (IOException e) {
        e.printStackTrace();
        return false;

    } catch (ParseException e) {
        e.printStackTrace();
        return false;

    }

    return true;
  }

  public void parseJsonObj(JSONObject json_content, String [] dir){

    int nested = 1;
    JSONObject parent = json_content;
    while(nested < dir.length ){
      parent = (JSONObject) parent.get(dir[nested]);
      nested ++;
    }
    results.append((String) parent.get(dir[nested]));
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
      String final_result = results.toString();
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
