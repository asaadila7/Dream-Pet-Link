import java.io.File;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Menu extends Container implements ActionListener {

    //Buttons
    private JButton instructions = new JButton (new ImageIcon ("./Instructions.png"));
    private JButton sound = new JButton (new ImageIcon("./sound.png"));
    private JButton play = new JButton ("New Game");

    public Menu () {
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel ();
        backgroundPanel.setLayout (new BorderLayout());

        //action listener
        play.addActionListener (this);
        instructions.addActionListener (this);
        sound.addActionListener (this);

        //Button Layout
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30)); //padding
        play.setAlignmentX(Component.CENTER_ALIGNMENT); //center align
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        sound.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPane.add(play);
        buttonPane.add(Box.createRigidArea(new Dimension(0, 10))); //padding
        buttonPane.add(instructions);
        buttonPane.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPane.add(sound);
        buttonPane.setOpaque(false); //set transparent

        backgroundPanel.add(buttonPane, BorderLayout.CENTER);
        add (backgroundPanel);
    }

    public void actionPerformed (ActionEvent event) {
        if (event.getSource () == play) {
            Main.frame.startGame ();
        } else if (event.getSource () == instructions) {
            new Instructions (Main.frame);
        } else if (event.getSource () == sound) {
        }
    }
}