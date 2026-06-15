/*
***********************************************
@Author : Mina Halim

@Last Modified: 13/6/2026

@Description: Displays the Main Menu for the game and explains the rules and the goal of the game.
************************************************
*/
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

class MenuMenu extends JFrame implements ActionListener {

    public MenuMenu() {
        super("MainMenu");

        // Main layered pane for background + UI layering
        JLayeredPane pane = new JLayeredPane();
        pane.setLayout(null);

        // Load and scale the background image
        ImageIcon bgIcon = new ImageIcon("Assets/Main menu.png");
        Image bgImg = bgIcon.getImage();
        Image bgScaled = bgImg.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        bgIcon = new ImageIcon(bgScaled);
        JLabel bgLabel = new JLabel(bgIcon);
        bgLabel.setBounds(0, 0, 800, 600);

        // Play button
        JButton PlayButton = new JButton("Play");
        PlayButton.setBounds(325, 514, 150, 45);
     // chatgpt
        PlayButton.setBackground(new Color(0, 0, 0, 0)); // transparent
        PlayButton.setForeground(Color.WHITE);
        PlayButton.setFont(new Font("Arial", Font.BOLD, 18));
        PlayButton.setFocusPainted(false);
        PlayButton.setContentAreaFilled(false);  // key line — stops Swing from painting the button background
     // ends here
        PlayButton.addActionListener(this);

        // Add background first (layer 0), then button on top (layer 1)
        pane.add(bgLabel, Integer.valueOf(0));
        pane.add(PlayButton, Integer.valueOf(1));

        setSize(800, 600);
        setContentPane(pane);
        setResizable(false);
        setLocationRelativeTo(null); // center the frame
        setVisible(true);

    } 

    public void actionPerformed(ActionEvent e) {

        // Launch the game and close the menu
    	GameFrame Frame3 = new GameFrame();
        dispose();
        

    } 

} 
