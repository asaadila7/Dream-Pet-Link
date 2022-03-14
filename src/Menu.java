import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Menu extends Container {

    //Buttons
    private JToggleButton instructions = new JToggleButton (new ImageIcon (this.getClass ().getResource ("Resources/instructions.png")));
    private JToggleButton sound = new JToggleButton (new ImageIcon (this.getClass ().getResource ("Resources/soundOn.png")));
    private JButton play = new JButton ("New Game");
    private ImageIcon close = new ImageIcon (this.getClass ().getResource ("Resources/close.png"));
    private ImageIcon soundOff = new ImageIcon (this.getClass ().getResource ("Resources/soundOff.png"));

    public Menu (App.AppFrame owner) {
        instructions.setSelectedIcon (close);
        instructions.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent event) {
                    if (instructions.isSelected ()) add (new Instructions ());
                    else remove (1);
                }
            }
        );

        sound.setSelectedIcon (soundOff);
        sound.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent event) {
                    if (sound.isSelected ()) owner.setSound (true);
                    else owner.setSound (false);
                }
            }
        );

        play.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    owner.startGame ();
                }
            }
        );

        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel ();
        backgroundPanel.setLayout (new BorderLayout());

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
}