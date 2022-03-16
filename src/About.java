import java.io.File;
import java.io.FileInputStream;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class About extends JPanel {
    public About () {
        JTextArea text = new JTextArea (25, 60);
        text.setBorder (BorderFactory.createEmptyBorder (30, 30, 30, 30)); //padding
        text.setLineWrap (true);
        text.setWrapStyleWord (true);
        text.setEditable (false); //user can not change instructions

        try {
            Scanner scanner = new Scanner (new FileInputStream (new File ("./src/Resources/About.txt").getPath ()));
            while (scanner.hasNext ()) {
                text.append (scanner.nextLine () + "\n");
            }
        } catch (FileNotFoundException e) { //if file is not found
            System.out.println ("instructions file not found");
        }

        add (new JScrollPane (text)); //allow users to scroll
    }
}