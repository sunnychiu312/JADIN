package Hub;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class Main{
  public static void main(String[] args) throws IOException{
    String alias = args[0];
    String file_name = args[1];
    Configurator config = new Configurator(file_name, alias);
    HashMap<String, String[]> routeMapping = config.get_routeMapping();
    Hub only_mirror = new Hub(routeMapping);
  }
}
