import java.io.File;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Instructions extends JScrollPane {
    public Instructions () {
        JTextArea text = new JTextArea (20, 30);
        text.setBorder (BorderFactory.createEmptyBorder (30, 30, 30, 30)); //padding
        text.setLineWrap (true); // wraps text around so it does not take too much horizontal space
        text.setEditable (false);

        try (Scanner scanner = new Scanner (new File ("./Instructions.txt"))) {
            //Scanner scanner = new Scanner (new FileInputStream (new File ("./Instructions.txt").getPath ()));
            while (scanner.hasNext ()) {
                text.append (scanner.nextLine () + "\n"); //write instructions.txt to textArea on dialog
            }
        } catch (FileNotFoundException e) { //if file is not found
            text.append ("instructions file not found");
        }

        add (text);
    }
}