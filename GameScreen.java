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
    private int playerX = 375;
    private int playerY = 275;
    private final int PLAYER_SIZE = 40;
    private final int PLAYER_SPEED = 4;

    // --- Health properties ---
    private int playerHP = 100;
    private final int MAX_HP = 100;
    private final int DAMAGE_PER_TICK = 10;
    private final int DAMAGE_DELAY = 60;    // frames between damage ticks (~1 second)
    private int damageCooldown = 0;

    // --- Game state ---
    private boolean gameOver = false;
    private boolean paused = false;
    private boolean victory = false;

    // tracks which keys are currently held down
    // Indices: 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    private boolean[] keysHeld = new boolean[4];

    // --- Enemy & Wave properties ---
    private final int ENEMY_SIZE = 20;
    private final int MAX_WAVES = 20;

    private int waveNumber = 1;
    private int enemyCount;
    private int[] enemyX;
    private int[] enemyY;
    private boolean[] enemyAlive;

    private Random rand = new Random();

    // --- Boss properties ---
    private final int BOSS_SIZE = 80;
    private final int BOSS_MAX_HP = 3;              // takes 3 slashes to kill
    private final int TELEPORT_DELAY = 180;         // teleports every 3 seconds at 60fps
    private final int BOSS_SPEED = 3;               // boss moves slower than normal enemies

    private boolean bossAlive = false;
    private int bossX = 0;
    private int bossY = 0;
    private int bossHP = BOSS_MAX_HP;
    private int bossTeleportTimer = 0;
    private int bossDamageCooldown = 0;

    // --- Slash properties ---
    private Image[] slashFrames = new Image[14];    // slash_00.png to slash_13.png
    private int slashFrame = -1;                    // current frame being drawn (-1 = not slashing)
    private int slashTimer = 0;                     // counts frames for animation speed
    private final int FRAME_DURATION = 2;           // game ticks per slash frame
    private final int SLASH_RANGE = 150;            // pixels from player center an enemy can be hit
    private final int SLASH_COOLDOWN = 60;          // 1 second at 60fps
    private int slashCooldown = 0;

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

        // Load all 14 slash animation frames (slash_00.png to slash_13.png)
        for (int i = 0; i < 14; i++) {
            String fileName = String.format("Assets/slash_%02d.png", i);
            ImageIcon slashIcon = new ImageIcon(fileName);
            slashFrames[i] = slashIcon.getImage().getScaledInstance(PLAYER_SIZE * 4, PLAYER_SIZE * 4, Image.SCALE_SMOOTH);
        }

        // Spawn the first wave
        spawnWave();

        // Initialize the game loop timer (does not start yet)
        gameLoop = new Timer(DELAY, this);

    }

    // chatgpt
    public void spawnWave() {

        // Waves 1-10: start at 3 enemies, +1 per wave
        // Waves 11-20: start at 5 enemies, +1 per wave
        if (waveNumber <= 10) {
            enemyCount = 2 + waveNumber;
        } else {
            enemyCount = 4 + (waveNumber - 10);
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

        // Spawn the boss on wave 10 and wave 20
        if (waveNumber == 10 || waveNumber == 20) {
            spawnBoss();
        } else {
            bossAlive = false;
        }

    }

    // Spawns the boss at a random screen edge
    public void spawnBoss() {
        bossHP = BOSS_MAX_HP;
        bossAlive = true;
        bossTeleportTimer = 0;
        bossDamageCooldown = 0;
        teleportBoss();
    }

    // Teleports the boss to a random screen edge position
    public void teleportBoss() {
        int edge = rand.nextInt(4);

        if (edge == 0) { // top edge
            bossX = rand.nextInt(SCREEN_WIDTH - BOSS_SIZE);
            bossY = 0;
        } else if (edge == 1) { // bottom edge
            bossX = rand.nextInt(SCREEN_WIDTH - BOSS_SIZE);
            bossY = SCREEN_HEIGHT - BOSS_SIZE;
        } else if (edge == 2) { // left edge
            bossX = 0;
            bossY = rand.nextInt(SCREEN_HEIGHT - BOSS_SIZE);
        } else { // right edge
            bossX = SCREEN_WIDTH - BOSS_SIZE;
            bossY = rand.nextInt(SCREEN_HEIGHT - BOSS_SIZE);
        }

        bossTeleportTimer = 0;
    }
    // ends here

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

        // Don't update anything if the game is over, paused, or victory
        if (gameOver || paused || victory) return;

        // Move player based on which keys are held
        if (keysHeld[0]) playerY -= PLAYER_SPEED; // UP
        if (keysHeld[1]) playerY += PLAYER_SPEED; // DOWN
        if (keysHeld[2]) playerX -= PLAYER_SPEED; // LEFT
        if (keysHeld[3]) playerX += PLAYER_SPEED; // RIGHT

        // Clamp player to screen bounds
        if (playerX < 0) playerX = 0;
        if (playerY < 0) playerY = 0;
        if (playerX > SCREEN_WIDTH - PLAYER_SIZE)  playerX = SCREEN_WIDTH - PLAYER_SIZE;
        if (playerY > SCREEN_HEIGHT - PLAYER_SIZE) playerY = SCREEN_HEIGHT - PLAYER_SIZE;

        // Tick down cooldowns each frame
        if (damageCooldown > 0) damageCooldown--;
        if (slashCooldown > 0) slashCooldown--;
        if (bossDamageCooldown > 0) bossDamageCooldown--;

        // Advance the slash animation
        if (slashFrame >= 0) {
            slashTimer++;
            if (slashTimer >= FRAME_DURATION) {
                slashTimer = 0;
                slashFrame++;
                if (slashFrame >= 14) {
                    slashFrame = -1;
                }
            }
        }

        // Move each alive enemy straight toward the player
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

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

            // Find direction from enemy to player center
            double dx = (playerX + PLAYER_SIZE / 2) - (enemyX[i] + ENEMY_SIZE / 2);
            double dy = (playerY + PLAYER_SIZE / 2) - (enemyY[i] + ENEMY_SIZE / 2);
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist != 0) {
                enemyX[i] += (int)(enemySpeed * dx / dist);
                enemyY[i] += (int)(enemySpeed * dy / dist);
            }

            // Check collision with player using Rectangle.intersects()
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle enemyRect = new Rectangle(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);

            if (playerRect.intersects(enemyRect)) {
                if (damageCooldown == 0) {
                    playerHP -= DAMAGE_PER_TICK;
                    damageCooldown = DAMAGE_DELAY;
                    if (playerHP <= 0) {
                        playerHP = 0;
                        gameOver = true;
                    }
                }
            }
        }

        // --- Boss update ---
        if (bossAlive) {

            // Teleport the boss every TELEPORT_DELAY frames
            bossTeleportTimer++;
            if (bossTeleportTimer >= TELEPORT_DELAY) {
                teleportBoss();
            }

            // Boss moves slowly toward the player
            double bDx = (playerX + PLAYER_SIZE / 2) - (bossX + BOSS_SIZE / 2);
            double bDy = (playerY + PLAYER_SIZE / 2) - (bossY + BOSS_SIZE / 2);
            double bDist = Math.sqrt(bDx * bDx + bDy * bDy);

            if (bDist != 0) {
                bossX += (int)(BOSS_SPEED * bDx / bDist);
                bossY += (int)(BOSS_SPEED * bDy / bDist);
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle bossRect = new Rectangle(bossX, bossY, BOSS_SIZE, BOSS_SIZE);

            if (playerRect.intersects(bossRect)) {
                if (bossDamageCooldown == 0) {
                    playerHP -= DAMAGE_PER_TICK;
                    bossDamageCooldown = DAMAGE_DELAY;
                    if (playerHP <= 0) {
                        playerHP = 0;
                        gameOver = true;
                    }
                }
            }
        }

        // Check if all enemies AND the boss are defeated
        boolean allEnemiesDead = true;
        for (int i = 0; i < enemyCount; i++) {
            if (enemyAlive[i]) {
                allEnemiesDead = false;
                break;
            }
        }

        // Only advance the wave once the boss is also defeated (if one was present)
        boolean bossDefeated = !bossAlive || (waveNumber != 10 && waveNumber != 20);

        if (allEnemiesDead && bossDefeated) {
            if (waveNumber == 20) {
                victory = true; // player beat both bosses — trigger victory
            } else if (waveNumber < MAX_WAVES) {
                waveNumber++;
                spawnWave();
            }
        }

    }

    // Triggers a 360 degree slash — kills enemies and damages boss within SLASH_RANGE
    public void performSlash() {

        int playerCenterX = playerX + PLAYER_SIZE / 2;
        int playerCenterY = playerY + PLAYER_SIZE / 2;

        // Check normal enemies
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            int enemyCenterX = enemyX[i] + ENEMY_SIZE / 2;
            int enemyCenterY = enemyY[i] + ENEMY_SIZE / 2;

            double dx = playerCenterX - enemyCenterX;
            double dy = playerCenterY - enemyCenterY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist <= SLASH_RANGE) {
                enemyAlive[i] = false;
            }
        }

        // Check boss
        if (bossAlive) {
            int bossCenterX = bossX + BOSS_SIZE / 2;
            int bossCenterY = bossY + BOSS_SIZE / 2;

            double dx = playerCenterX - bossCenterX;
            double dy = playerCenterY - bossCenterY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist <= SLASH_RANGE) {
                bossHP--;
                if (bossHP <= 0) {
                    bossAlive = false;
                }
            }
        }

        // Start the slash animation
        slashFrame = 0;
        slashTimer = 0;

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
        g.setColor(Color.WHITE);
        g.drawRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        // Draw the slash animation centered on the player if active
        if (slashFrame >= 0 && slashFrame < 14) {
            int slashSize = PLAYER_SIZE * 4;
            int slashX = playerX - slashSize / 2 + PLAYER_SIZE / 2;
            int slashY = playerY - slashSize / 2 + PLAYER_SIZE / 2;
            g.drawImage(slashFrames[slashFrame], slashX, slashY, this);
        }

        // Draw each alive enemy — red for waves 1-10, purple for waves 11-20
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            if (waveNumber <= 10) {
                g.setColor(Color.RED);
            } else {
                g.setColor(new Color(128, 0, 128)); // purple
            }
            g.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
        }

        // Draw the boss as a large semi-circle
        // Boss 1 (wave 10) = orange, Boss 2 (wave 20) = dark red
        if (bossAlive) {
            if (waveNumber <= 10) {
                g.setColor(new Color(255, 140, 0)); // orange
            } else {
                g.setColor(new Color(139, 0, 0)); // dark red
            }

            // Draw filled semi-circle (arc of 180 degrees)
            g.fillArc(bossX, bossY, BOSS_SIZE, BOSS_SIZE, 0, 180);
            g.setColor(Color.WHITE);
            g.drawArc(bossX, bossY, BOSS_SIZE, BOSS_SIZE, 0, 180);

            // Draw boss HP indicators as small squares above the boss
            for (int h = 0; h < bossHP; h++) {
                g.setColor(Color.YELLOW);
                g.fillRect(bossX + 10 + h * 25, bossY - 15, 20, 10);
                g.setColor(Color.BLACK);
                g.drawRect(bossX + 10 + h * 25, bossY - 15, 20, 10);
            }
        }

        // --- HUD ---
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Wave: " + waveNumber, 10, 20);

        // Draw HP bar background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(10, 30, 200, 18);

        // Draw HP bar fill (green → yellow → red based on HP)
        if (playerHP > 60) {
            g.setColor(Color.GREEN);
        } else if (playerHP > 30) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect(10, 30, (int)(200 * ((double) playerHP / MAX_HP)), 18);

        g.setColor(Color.WHITE);
        g.drawRect(10, 30, 200, 18);
        g.drawString(playerHP + " / " + MAX_HP, 220, 45);

        // Boss wave warning label
        if (bossAlive) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("BOSS WAVE", 340, 20);
        }

        // --- Pause overlay ---
        if (paused) {

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.drawString("PAUSED", 270, 250);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ESC to Resume", 300, 310);

        }

        // --- Game Over overlay ---
        if (gameOver) {

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.drawString("GAME OVER", 195, 220);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("You reached Wave " + waveNumber, 270, 290);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", 310, 340);

        }

        // --- Victory --- launches VictoryScreen and closes the game frame
        if (victory) {
            gameLoop.stop();
            dispose();
            new VictoryScreen();
        }

    }

    // Disposes the parent GameFrame when victory is triggered
    public void dispose() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    // KeyListener (Movement)
    // chatgpt
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    keysHeld[0] = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  keysHeld[1] = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  keysHeld[2] = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keysHeld[3] = true;

        // ESC to pause and unpause
        if (k == KeyEvent.VK_ESCAPE && !gameOver) {
            paused = !paused;
        }

        // R to restart when game is over
        if (k == KeyEvent.VK_R && gameOver) {
            restartGame();
        }

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

    // Resets all game state back to the beginning
    public void restartGame() {
        playerX = 375;
        playerY = 275;
        playerHP = MAX_HP;
        damageCooldown = 0;
        slashCooldown = 0;
        slashFrame = -1;
        waveNumber = 1;
        gameOver = false;
        paused = false;
        victory = false;
        bossAlive = false;
        spawnWave();
    }

    // MouseListener — left click triggers a 360 degree slash
    public void mousePressed(MouseEvent e) {

        if (gameOver || paused) return;

        if (slashCooldown == 0) {
            performSlash();
            slashCooldown = SLASH_COOLDOWN;
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
