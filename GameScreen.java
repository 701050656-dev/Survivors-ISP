import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class GameScreen extends JPanel implements ActionListener, KeyListener, MouseListener {

    // Game loop timer — fires every ~16ms for roughly 60 FPS
    // chatgpt
    private Timer gameLoop;
    private final int DELAY = 16;
    // ends here

    // Screen dimensions
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;

    // Map backgrounds — bg1 for waves 1-10, bg2 for waves 11-20
    private Image mapBackground1;
    private Image mapBackground2;

    // Player properties
    private int playerX = 375;      // starting X
    private int playerY = 275;      // starting Y
    private final int PLAYER_SIZE = 40;
    private final int PLAYER_SPEED = 4;
    private int playerhp; // how much hp the player currently has
    private int maxhp; // max amount of hp the player has.
    
    // tracks which keys are currently held down
    // Indices: 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    private boolean[] keysHeld = new boolean[4];

    // --- Enemy & Wave properties ---
    private final int ENEMY_SIZE = 20;
    private final int MAX_WAVES = 20;

    private int waveNumber = 1;         // current wave
    private int enemyCount;             // how many enemies spawn this wave
    private int[] enemyX;               // X positions of all enemies
    private int[] enemyY;               // Y positions of all enemies
    private boolean[] enemyAlive;       // tracks which enemies are still alive
    

    private Random rand = new Random();

    public GameScreen() {

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Load and scale both map backgrounds
        ImageIcon bgIcon1 = new ImageIcon("Assets/bg1.png");
        mapBackground1 = bgIcon1.getImage().getScaledInstance(SCREEN_WIDTH, SCREEN_HEIGHT, Image.SCALE_SMOOTH);

        ImageIcon bgIcon2 = new ImageIcon("Assets/bg2.png");
        mapBackground2 = bgIcon2.getImage().getScaledInstance(SCREEN_WIDTH, SCREEN_HEIGHT, Image.SCALE_SMOOTH);
        


        // Spawn the first wave
        spawnWave();

        // Initialize the game loop timer (does not start yet)
        gameLoop = new Timer(DELAY, this);

    }

// needs a method header. 
    public void spawnWave() {

        // Waves 1-10: start at 3 enemies, +1 per wave
        // Waves 11-20: start at 5 enemies, +1 per wave
        if (waveNumber <= 10) {
            enemyCount = 2 + waveNumber; // wave 1=3, wave 10=12
        } else {
            enemyCount = 4 + (waveNumber - 10); // wave 11=5, wave 20=14
        }

        enemyX = new int[enemyCount];
        enemyY = new int[enemyCount];
        enemyAlive = new boolean[enemyCount];

        // Spawn each enemy at a random position along a random screen edge
        for (int i = 0; i < enemyCount; i++) {
            int edge = rand.nextInt(4); // 0=top, 1=bottom, 2=left, 3=right

            if (edge == 0) { // top edge
                enemyX[i] = rand.nextInt(SCREEN_WIDTH);
                enemyY[i] = 0;
            } else if (edge == 1) { // bottom edge
                enemyX[i] = rand.nextInt(SCREEN_WIDTH);
                enemyY[i] = SCREEN_HEIGHT - ENEMY_SIZE;
            } else if (edge == 2) { // left edge
                enemyX[i] = 0;
                enemyY[i] = rand.nextInt(SCREEN_HEIGHT);
            } else { // right edge
                enemyX[i] = SCREEN_WIDTH - ENEMY_SIZE;
                enemyY[i] = rand.nextInt(SCREEN_HEIGHT);
            }

            enemyAlive[i] = true;
        }

    }
    // ends here
    
// needs method header
    public void startGameLoop() {
        requestFocusInWindow();
        gameLoop.start();
    }

    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    // chatgpt
    public void update() {

        // Move player based on which keys are held
        if (keysHeld[0]) playerY -= PLAYER_SPEED; // UP
        if (keysHeld[1]) playerY += PLAYER_SPEED; // DOWN
        if (keysHeld[2]) playerX -= PLAYER_SPEED; // LEFT
        if (keysHeld[3]) playerX += PLAYER_SPEED; // RIGHT

        // Stop the player to screen bounds so they can't walk off screen
        if (playerX < 0) playerX = 0;
        if (playerY < 0) playerY = 0;
        if (playerX > SCREEN_WIDTH - PLAYER_SIZE)  playerX = SCREEN_WIDTH - PLAYER_SIZE;
        if (playerY > SCREEN_HEIGHT - PLAYER_SIZE) playerY = SCREEN_HEIGHT - PLAYER_SIZE;

        // Move each alive enemy straight toward the player
        playerhp = 100;
        maxhp = 100;
        
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) 
            	continue;

            // Waves 1-10: speed starts at 2, caps at 5
            // Waves 11-20: speed resets to 2, caps at 6
            double enemySpeed;
            if (waveNumber <= 10) {
                enemySpeed = 2 + (waveNumber - 1) * 0.5;
                if (enemySpeed > 5) enemySpeed = 5;
            } else {
                enemySpeed = 2 + (waveNumber - 11) * 0.5;
                if (enemySpeed > 6) enemySpeed = 6;
            }
// chatgpt
            // Find direction from enemy to player center
            double dx = (playerX + PLAYER_SIZE / 2) - (enemyX[i] + ENEMY_SIZE / 2);
            double dy = (playerY + PLAYER_SIZE / 2) - (enemyY[i] + ENEMY_SIZE / 2);
            double dist = Math.sqrt(dx * dx + dy * dy);

            // enemy move
            if (dist != 0) {
                enemyX[i] += (int)(enemySpeed * dx / dist);
                enemyY[i] += (int)(enemySpeed * dy / dist);
// ends here
            }
            	if ( enemyX[i] == playerX && enemyY[i] == playerY && playerhp > 0) {
            		playerhp = maxhp - 10;
            	}
            	
            	if(playerhp <= 0) {
            		System.out.println("you died");
            	}
           
        }

        // Check if all enemies are defeated — if so, advance the wave
        boolean allDead = true;
        for (int i = 0; i < enemyCount; i++) {
            if (enemyAlive[i]) {
                allDead = false;
                break;
            }
        }

        if (allDead && waveNumber < MAX_WAVES) {
            waveNumber++;
            spawnWave();
        }

    }
    // ends here

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the correct background depending on the wave
        if (waveNumber <= 10) {
            g.drawImage(mapBackground1, 0, 0, this);
        } else {
            g.drawImage(mapBackground2, 0, 0, this);
        }

        // Draw player as a filled blue rectangle
        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        // Draw a white outline around the player
        g.setColor(Color.WHITE);
        g.drawRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        
        // Draw HP bar
        g.setColor(Color.RED);
        g.drawRect(50, 500, 120, 30);
        g.fillRect(50, 500, 120, 30);

        // Draw each alive enemy — red for waves 1-10, purple for waves 11-20
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            if (waveNumber <= 10) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(new Color(128, 0, 128)); // purple
            }
            g.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
        }

        // Draw wave number in the top-left corner
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Wave: " + waveNumber, 10, 20);

    }

    // KeyListener (Movement)
    // chatgpt
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    keysHeld[0] = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  keysHeld[1] = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  keysHeld[2] = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keysHeld[3] = true;

    }

    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    keysHeld[0] = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  keysHeld[1] = false;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  keysHeld[2] = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keysHeld[3] = false;
        // ends here

    }

    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // MouseListener — click an enemy to remove it
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // Check if the click landed on any alive enemy
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            // Calculate distance from player center to enemy center
            double dx = (playerX + PLAYER_SIZE / 2) - (enemyX[i] + ENEMY_SIZE / 2);
            double dy = (playerY + PLAYER_SIZE / 2) - (enemyY[i] + ENEMY_SIZE / 2);
            double distToPlayer = Math.sqrt(dx * dx + dy * dy);

            // Only allow the click if the enemy is within 200 pixels of the player
            if (distToPlayer > 200) continue;

            if (mx >= enemyX[i] && mx <= enemyX[i] + ENEMY_SIZE &&
                my >= enemyY[i] && my <= enemyY[i] + ENEMY_SIZE) {
                enemyAlive[i] = false;
            }
        }

    }


    public void mouseReleased(MouseEvent e) {
        // Not used
    }

    public void mouseClicked(MouseEvent e) {
        // Not used
    }

    public void mouseEntered(MouseEvent e) {
        // Not used
    }

    public void mouseExited(MouseEvent e) {
        // Not used
    }

}
