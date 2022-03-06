import java.io.File;
import javax.swing.*;
import java.awt.*;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Instructions extends JDialog {

    /*
     *Instructions (method)
     *@param name:owner type: Frame
     * Pre : parent component present : frame
     *Post : Sets modal = true (disables actions on parent frame), sets title
     *@return (none)
     */
    public Instructions (Frame owner) {
        super (owner, "How to Play", true);
        setUp ();
    }

    /*
     *Instructions (method)
     *@param name:owner type: Dialog
     *Sets modal = true (disables actions on parent dialog), sets title
     * Pre : parent component present : dialog
     *Post : Sets modal = true (disables actions on parent frame), sets title
     *@return (none)
     */
    public Instructions (Dialog owner) {
        super (owner, "How to Play", true);
        setUp ();
    }

    /*
     *setUp (method)
     *This method creates the dialog and writes the instructions file to the textField
     *@param (none)
     *@return (none)
     * Pre: n/a
     * Post : creates dialog with instructions
     */
    public void setUp () {
        setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE); //Does not end program on close

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

        setLayout (new FlowLayout ());
        add (new JScrollPane (text)); //allow users to scroll
        pack ();
        setLocationRelativeTo (null);
        setVisible (true); //display on screen
    }
}