/*
***********************************************
@Author : Mina Halim

@Last Modified: 12/6/2026

@Description: Displays the Controls for the game / how to play.
************************************************
*/
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
public class ControlScreen {

	public static void main(String[] args) {
		ControlFrame frame = new ControlFrame();
		frame.addWindowListener
		(new WindowAdapter()
				{
			public void windowClosing(WindowEvent e)

			{
				System.exit(0);
			}

				}

	);

	} //  end of main

}// end of class



class ControlFrame extends JFrame {
	public ControlFrame() {
		super("Controls Screen");
		// Main pane
		JLayeredPane pane = new JLayeredPane();
		pane.setLayout(null); 

		

		// Import Controls screen
// chatgpt
		ImageIcon icon = new ImageIcon("Assets/Controls.png");
		Image img = icon.getImage();
		Image scaled = img.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaled);
		JLabel imageLabel = new JLabel(icon);
		imageLabel.setBounds(0, 0, 800, 600);
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		// ends here

		// Continue button
		JButton StartButton = new JButton("Continue");
		StartButton.setBounds(325, 500, 150, 40);
		StartButton.setBackground(Color.BLACK);
		StartButton.setForeground(Color.WHITE);
		StartButton.setFocusPainted(false);


		//  Go to next frame
		StartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuMenu Frame2 = new MenuMenu();
				dispose();
			}
		});



		pane.add(imageLabel, Integer.valueOf(0)); // background
		pane.add(StartButton, Integer.valueOf(1)); // on top



		setSize(800, 600);
		setContentPane(pane);
		setVisible(true);
		setLocationRelativeTo(null); // center the frame
		

	}
	

}
