import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class GameScreen extends JPanel implements ActionListener, KeyListener, MouseListener {
	
	Image playerSprite;

    // Game loop timer — fires every ~16ms for roughly 60 FPS
// chatgpt
    private Timer gameLoop;
    private final int DELAY = 16;
// ends here

    // Screen dimensions
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;

    //Map background 
    private Image mapBackground;

    // Player properties
    private int playerX = 375;       // starting X 
    private int playerY = 275;       // starting Y 
    private final int PLAYER_SIZE = 100;
    private final int PLAYER_SPEED = 4;

    //tracks which keys are currently held down
    // indexes: 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    private boolean[] keysHeld = new boolean[4];

    public GameScreen() {

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Load and scale the map background
        ImageIcon bgIcon = new ImageIcon("Assets/bg1.png");
        mapBackground = bgIcon.getImage().getScaledInstance(SCREEN_WIDTH, SCREEN_HEIGHT, Image.SCALE_SMOOTH);
        
        // Load Player sprite
        playerSprite = new ImageIcon("Assets/player.png").getImage();
        

        // Initialize the game loop timer (does not start yet)
        gameLoop = new Timer(DELAY, this);

    } 

    public void startGameLoop() {
        requestFocusInWindow();
        gameLoop.start();
    } // end of startGameLoop

    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    } 

 // chatgpt
    public void update() {

        // Move player based on which keys are held
        if (keysHeld[0]) playerY = playerY - PLAYER_SPEED; // UP
        if (keysHeld[1]) playerY = playerY + PLAYER_SPEED; // DOWN
        if (keysHeld[2]) playerX = playerX - PLAYER_SPEED; // LEFT
        if (keysHeld[3]) playerX = playerX + PLAYER_SPEED; // RIGHT

        // Traps Player to the edges of the screen.
        if (playerX < 0) 
        	playerX = 0;
        if (playerY < 0) 
        	playerY = 0;
        if (playerX > SCREEN_WIDTH - PLAYER_SIZE)  playerX = SCREEN_WIDTH - PLAYER_SIZE;
        if (playerY > SCREEN_HEIGHT - PLAYER_SIZE) playerY = SCREEN_HEIGHT - PLAYER_SIZE;

    } 
// ends here


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        // display character sprite
        g.drawImage(playerSprite, playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, this);
        
        
        // Draw map background
        g.drawImage(mapBackground, 0, 0, this);

        // Draw player as a filled blue rectangle
        //g.setColor(Color.BLUE);
       //g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        // Draw a white outline around the player so it stands out on any background
        //g.setColor(Color.BLACK);
        //g.drawRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

    } 

    // KeyListener (Movement)
// chatgpt
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) 
        	keysHeld[0] = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)
        	keysHeld[1] = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) 
        	keysHeld[2] = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT)
        	keysHeld[3] = true;

    } 

    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)   
        	keysHeld[0] = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) 
        	keysHeld[1] = false;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) 
        	keysHeld[2] = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) 
        	keysHeld[3] = false;

    } 
    public void keyTyped(KeyEvent e) {
        // Not used
    } // end of keyTyped

    // --- MouseListener stubs (slicing mechanic added in Step 3) ---

    public void mousePressed(MouseEvent e) {
        // Slash/slicing logic added in Step 3
    } // end of mousePressed

    public void mouseReleased(MouseEvent e) {
        // Not used
    } // end of mouseReleased

    public void mouseClicked(MouseEvent e) {
        // Not used
    } // end of mouseClicked

    public void mouseEntered(MouseEvent e) {
        // Not used
    } // end of mouseEntered

    public void mouseExited(MouseEvent e) {
        // Not used
    } // end of mouseExited
    
   // ends here

} 
