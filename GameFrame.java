/*
***********************************************
@Author : Mina Halim

@Last Modified: 13/6/2026

@Description: The frame that contains the game screen and makes it visible to the player.
************************************************
*/
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        super("Survivors");
        // Create and add the main game screen
        GameScreen gameScreen = new GameScreen();
        add(gameScreen);

        // Exit on window close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        }); // end of windowListener

        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        // Start the game loop after the frame is visible
        gameScreen.startGameLoop();

    } 

} 
