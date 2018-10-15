import java.io.FileWriter;
import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.SecurityException;

//javac -cp 'json-simple-1.1.1.jar' WriteFile.java

@SuppressWarnings("unchecked")
public class WriteFile extends Thread{

  private Socket acpt_sock;

  public WriteFile(Socket acpt_sock){
    this.acpt_sock = acpt_sock;
  }

  public String [] get_filename_json(String content){
    String [] results = new String[2];
    int firstBracket = content.indexOf('{');
    String fileName =  "./json_files/"+ content.substring(0,firstBracket) + ".json";
    String json_string = content.substring(firstBracket, content.length());
    results[0] = fileName;
    results[1] = json_string;
    return results;
  }

  public boolean check_exist(String filename){
    File f = null;
    Boolean exist = true;
    try {
         f = new File(filename);
         exist = f.isFile();
      } catch( SecurityException e) {
        System.out.println("file not found");
      }
    System.out.println(exist);
    if(exist){
      return true;
    }
    return false;
  }
  public boolean writeContent(String filename, String content){

    JSONParser parser = new JSONParser();
    try{
      FileWriter file = new FileWriter(filename);
      JSONObject json = (JSONObject) parser.parse(content);
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
      String [] filename_json = get_filename_json(content);
      Boolean file_exist = check_exist(filename_json[0]);
      String finished = "false";
      if(!file_exist){
        finished = Boolean.toString(writeContent(filename_json[0], filename_json[1].trim()));
      }
      byte [] encode = finished.getBytes("US-ASCII");
      acpt_sock.getOutputStream().write(encode,0,encode.length);
      acpt_sock.close();
    }
    catch(IOException e){}
  }
}
