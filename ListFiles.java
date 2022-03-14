import java.io.File;
import java.io.FileWriter;

class ListFiles {
  public static void main(String[] args) {
    File folder = new File ("C:\\Users\\aadil\\Documents\\Git Projects\\Dream Pet Link\\Dream Pet Link\\src\\Resources\\Tiles");
    File [] listOfFiles = folder.listFiles ();

    File list = new File ("C:\\Users\\aadil\\Documents\\Git Projects\\Dream Pet Link\\Dream Pet Link\\filenames.txt");

    try (FileWriter writer = new FileWriter (list)) {
      for (File file : listOfFiles) {
	      writer.write ("\"");
        String fileName = file.getName ();
        fileName = fileName.substring (0, fileName.length ());
        writer.write (fileName);
      	writer.write ("\", ");
      }
    	writer.write ("\b\b");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}