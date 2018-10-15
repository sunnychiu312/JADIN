import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
//javac -cp 'json-simple-1.1.1.jar' readFile.java

public class ReadFile extends Thread{

  private Socket acpt_sock;
  private String file_content;
  private String server_id;

  public ReadFile(Socket acpt_sock, String server_id){
    this.acpt_sock = acpt_sock;
    this.server_id = server_id;
  }

  public void readContent(String content){

    String [] dir = content.split("\\.");
    String fileName = "./"+ server_id+"/" + dir[0].trim() + ".json";
    JSONParser jsonParser = new JSONParser();

    try {
        FileReader reader = new FileReader(fileName);
        Object obj = jsonParser.parse(reader);
        JSONObject json_content = (JSONObject) obj;

        if(dir.length > 1){
          parseJsonObj( json_content, dir );
        }
        else{
          file_content = "DONE"+ json_content.toString();
        }

    } catch (IOException | ParseException  e) {
        file_content = "FAIL";
        e.printStackTrace();
    }
  }

  public void parseJsonObj(JSONObject json_content, String [] dir){
    int nested = 1;
    while(nested < dir.length - 1 ){
      json_content = (JSONObject) json_content.get(dir[nested].trim());
      nested ++;
    }
    file_content = "DONE"+ (String) json_content.get(dir[nested].trim());
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
      byte [] encode = file_content.getBytes("US-ASCII");
      acpt_sock.getOutputStream().write(encode,0,encode.length);
      acpt_sock.close();
    }
    catch(IOException e){}
  }
}
