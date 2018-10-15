
import java.io.IOException;

public class Main{
  public static void main(String[] args) throws IOException, InterruptedException {
    String alias = args[1];
    String file_name = args[0];
    Hub myHub = new Hub(file_name, alias);
  }
}
