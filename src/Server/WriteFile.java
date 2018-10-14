import java.io.FileWriter;
import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

//javac -cp 'json-simple-1.1.1.jar' WriteFile.java

@SuppressWarnings("unchecked")
public class WriteFile extends Thread{

  private Socket acpt_sock;

  public WriteFile(Socket acpt_sock){
    this.acpt_sock = acpt_sock;
  }

  public boolean writeContent(String content){
    int firstBracket = content.indexOf('{');
    String fileName =  "./json_files/"+ content.substring(0,firstBracket) + ".json";
    String JSON_content = content.substring(firstBracket, content.length());
    File f = null;
    Boolean exist = true;
    try {
         f = new File(fileName);
         exist = f.isFile();
      } catch(Exception e) {}

    if(exist){
      return false;
    }

    JSONParser parser = new JSONParser();
    try(FileWriter file = new FileWriter(fileName)){

      JSONObject json = (JSONObject) parser.parse(JSON_content);
      file.write(json.toJSONString());
      file.flush();

    } catch (IOException | ParseException e) {
      System.out.println("IOExcept");
      return false;
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
      String finished = Boolean.toString(writeContent(content.trim()));
      byte [] encode = finished.getBytes("US-ASCII");
      acpt_sock.getOutputStream().write(encode,0,encode.length);
      acpt_sock.close();
    }
    catch(IOException e){}
  }
}
