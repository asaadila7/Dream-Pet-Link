import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Pause extends JDialog implements ActionListener {
    //buttons
    /*
    private JButton sound = new JButton (new ImageIcon (new File ("./settings.png").getPath ()));
    private JButton instructions = new JButton (new ImageIcon (new File ("./Instructions.png").getPath ()));
    private JButton resume = new JButton ("Resume");
    private JButton quit = new JButton ("Quit");
    */
    private JButton sound = new JButton (new ImageIcon ("./settings.png"));
    private JButton instructions = new JButton (new ImageIcon ("./Instructions.png"));
    private JButton resume = new JButton ("Resume");
    private JButton quit = new JButton ("Quit");

    public Pause (Frame owner) {
        //sets modal, owner and title
        super (owner, "Paused", true);
        setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE); //cannot exit via x button
        BoxLayout layout = new BoxLayout (getContentPane (), BoxLayout.PAGE_AXIS);
        getContentPane ().setLayout (layout);

        //action listeners
        sound.addActionListener (this);
        instructions.addActionListener (this);
        resume.addActionListener (this);
        quit.addActionListener (this);

        add (Box.createRigidArea (new Dimension (150, 20))); //padding
        add (resume);
        add (Box.createRigidArea (new Dimension (150, 20)));
        add (quit);
        add (Box.createRigidArea (new Dimension (150, 20)));
        add (instructions);
        add (Box.createRigidArea (new Dimension (150, 20)));
        add (sound);
        add (Box.createRigidArea (new Dimension (150, 20)));

        sound.setAlignmentX (Component.CENTER_ALIGNMENT);
        instructions.setAlignmentX (Component.CENTER_ALIGNMENT);
        resume.setAlignmentX (Component.CENTER_ALIGNMENT);
        quit.setAlignmentX (Component.CENTER_ALIGNMENT);

        layout.layoutContainer (getContentPane ());
        pack ();
        setLocationRelativeTo (null);
        setVisible (true);
    }

    @Override
    public void actionPerformed (ActionEvent event) {
        if (event.getSource () == resume) {
            resume ();
            dispose ();
        } else if (event.getSource () == quit) {
            App.quitGame ();
            dispose ();
        } else if (event.getSource () == instructions) {
            new Instructions (this);
        } else if (event.getSource () == sound) {
        }
    }
}