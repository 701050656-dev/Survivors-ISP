import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;

import javax.sound.sampled.*;
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

    // Enemy sprites — enemy1.png for waves 1-10, enemy2.png for waves 11-20
    private Image enemy1Image;
    private Image enemy2Image;

    // Boss sprites — boss1.png for wave 10, boss2.png for wave 20
    private Image boss1Image;
    private Image boss2Image;

    // Player sprite
    private Image playerImage;

    // --- Sound effects ---
    private Clip[] slashSounds = new Clip[3]; // Slash.wav, Slash2.wav, Slash3.wav
    private Clip damageSound;
    private Clip deathSound;

    // Player properties
    private int playerX = 375;
    private int playerY = 275;
    private final int PLAYER_SIZE = 40;
    private final int PLAYER_SPEED = 4;

    // --- Health properties ---
    private int playerHP = 100;
    private final int MAX_HP = 100;
    private final int DAMAGE_PER_TICK = 10;
    private final int DAMAGE_DELAY = 60;
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
    private final int BOSS_MAX_HP = 3;
    private final int TELEPORT_DELAY = 180;
    private final int BOSS_SPEED = 2;

    private boolean bossAlive = false;
    private int bossX = 0;
    private int bossY = 0;
    private int bossHP = BOSS_MAX_HP;
    private int bossTeleportTimer = 0;
    private int bossDamageCooldown = 0;

    // --- Slash properties ---
    private Image[] slashFrames = new Image[14];
    private int slashFrame = -1;
    private int slashTimer = 0;
    private final int FRAME_DURATION = 2;
    private final int SLASH_RANGE = 150;
    private final int SLASH_COOLDOWN = 60;
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

        // Load and scale the player sprite
        ImageIcon playerIcon = new ImageIcon("Assets/player.png");
        playerImage = playerIcon.getImage().getScaledInstance(PLAYER_SIZE, PLAYER_SIZE, Image.SCALE_SMOOTH);

        // Load and scale enemy sprites
        ImageIcon enemy1Icon = new ImageIcon("Assets/enemy1.png");
        enemy1Image = enemy1Icon.getImage().getScaledInstance(ENEMY_SIZE, ENEMY_SIZE, Image.SCALE_SMOOTH);

        ImageIcon enemy2Icon = new ImageIcon("Assets/enemy2.png");
        enemy2Image = enemy2Icon.getImage().getScaledInstance(ENEMY_SIZE, ENEMY_SIZE, Image.SCALE_SMOOTH);

        // Load and scale boss sprites
        ImageIcon boss1Icon = new ImageIcon("Assets/boss1.png");
        boss1Image = boss1Icon.getImage().getScaledInstance(BOSS_SIZE, BOSS_SIZE, Image.SCALE_SMOOTH);

        ImageIcon boss2Icon = new ImageIcon("Assets/boss2.png");
        boss2Image = boss2Icon.getImage().getScaledInstance(BOSS_SIZE, BOSS_SIZE, Image.SCALE_SMOOTH);

        // Load all 14 slash animation frames (slash_00.png to slash_13.png)
        for (int i = 0; i < 14; i++) {
            String fileName = String.format("Assets/slash_%02d.png", i);
            ImageIcon slashIcon = new ImageIcon(fileName);
            slashFrames[i] = slashIcon.getImage().getScaledInstance(PLAYER_SIZE * 4, PLAYER_SIZE * 4, Image.SCALE_SMOOTH);
        }

        // Load sound effects (victory.wav is handled by VictoryScreen)
        slashSounds[0] = loadSound("Assets/Slash.wav");
        slashSounds[1] = loadSound("Assets/Slash2.wav");
        slashSounds[2] = loadSound("Assets/Slash3.wav");
        damageSound    = loadSound("Assets/dmg.wav");
        deathSound     = loadSound("Assets/lost.wav");

        // Spawn the first wave
        spawnWave();

        // Initialize the game loop timer (does not start yet)
        gameLoop = new Timer(DELAY, this);

    }

    // Loads a .wav file and returns a Clip ready to play
    private Clip loadSound(String filePath) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            return clip;
        } catch (Exception e) {
            System.out.println("Could not load sound: " + filePath);
            return null;
        }
    }

    // Stops, rewinds, and plays a Clip from the beginning
    private void playSound(Clip clip) {
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    // chatgpt
    public void spawnWave() {

        if (waveNumber <= 10) {
            enemyCount = 2 + waveNumber;
        } else {
            enemyCount = 4 + (waveNumber - 10);
        }

        enemyX = new int[enemyCount];
        enemyY = new int[enemyCount];
        enemyAlive = new boolean[enemyCount];

        for (int i = 0; i < enemyCount; i++) {
            int edge = rand.nextInt(4);

            if (edge == 0) {
                enemyX[i] = rand.nextInt(SCREEN_WIDTH);
                enemyY[i] = 0;
            } else if (edge == 1) {
                enemyX[i] = rand.nextInt(SCREEN_WIDTH);
                enemyY[i] = SCREEN_HEIGHT - ENEMY_SIZE;
            } else if (edge == 2) {
                enemyX[i] = 0;
                enemyY[i] = rand.nextInt(SCREEN_HEIGHT);
            } else {
                enemyX[i] = SCREEN_WIDTH - ENEMY_SIZE;
                enemyY[i] = rand.nextInt(SCREEN_HEIGHT);
            }

            enemyAlive[i] = true;
        }

        if (waveNumber == 10 || waveNumber == 20) {
            spawnBoss();
        } else {
            bossAlive = false;
        }

    }

    public void spawnBoss() {
        bossHP = BOSS_MAX_HP;
        bossAlive = true;
        bossTeleportTimer = 0;
        bossDamageCooldown = 0;
        teleportBoss();
    }

    public void teleportBoss() {
        int edge = rand.nextInt(4);

        if (edge == 0) {
            bossX = rand.nextInt(SCREEN_WIDTH - BOSS_SIZE);
            bossY = 0;
        } else if (edge == 1) {
            bossX = rand.nextInt(SCREEN_WIDTH - BOSS_SIZE);
            bossY = SCREEN_HEIGHT - BOSS_SIZE;
        } else if (edge == 2) {
            bossX = 0;
            bossY = rand.nextInt(SCREEN_HEIGHT - BOSS_SIZE);
        } else {
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

        if (gameOver || paused || victory) return;

        if (keysHeld[0]) playerY -= PLAYER_SPEED;
        if (keysHeld[1]) playerY += PLAYER_SPEED;
        if (keysHeld[2]) playerX -= PLAYER_SPEED;
        if (keysHeld[3]) playerX += PLAYER_SPEED;

        if (playerX < 0) playerX = 0;
        if (playerY < 0) playerY = 0;
        if (playerX > SCREEN_WIDTH - PLAYER_SIZE)  playerX = SCREEN_WIDTH - PLAYER_SIZE;
        if (playerY > SCREEN_HEIGHT - PLAYER_SIZE) playerY = SCREEN_HEIGHT - PLAYER_SIZE;

        if (damageCooldown > 0) damageCooldown--;
        if (slashCooldown > 0) slashCooldown--;
        if (bossDamageCooldown > 0) bossDamageCooldown--;

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

        // Move enemies and check collision with player
        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            double enemySpeed;
            if (waveNumber <= 10) {
                enemySpeed = 2 + (waveNumber - 1) * 0.5;
                if (enemySpeed > 5) enemySpeed = 5;
            } else {
                enemySpeed = 2 + (waveNumber - 11) * 0.5;
                if (enemySpeed > 6) enemySpeed = 6;
            }

            double dx = (playerX + PLAYER_SIZE / 2) - (enemyX[i] + ENEMY_SIZE / 2);
            double dy = (playerY + PLAYER_SIZE / 2) - (enemyY[i] + ENEMY_SIZE / 2);
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist != 0) {
                enemyX[i] += (int)(enemySpeed * dx / dist);
                enemyY[i] += (int)(enemySpeed * dy / dist);
            }

            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle enemyRect  = new Rectangle(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);

            if (playerRect.intersects(enemyRect)) {
                if (damageCooldown == 0) {
                    playerHP -= DAMAGE_PER_TICK;
                    damageCooldown = DAMAGE_DELAY;
                    if (playerHP <= 0) {
                        playerHP = 0;
                        gameOver = true;
                        playSound(deathSound);  // play lost.wav on death
                    } else {
                        playSound(damageSound); // play dmg.wav on hit
                    }
                }
            }
        }

        // Boss update
        if (bossAlive) {

            bossTeleportTimer++;
            if (bossTeleportTimer >= TELEPORT_DELAY) {
                teleportBoss();
            }

            double bDx = (playerX + PLAYER_SIZE / 2) - (bossX + BOSS_SIZE / 2);
            double bDy = (playerY + PLAYER_SIZE / 2) - (bossY + BOSS_SIZE / 2);
            double bDist = Math.sqrt(bDx * bDx + bDy * bDy);

            if (bDist != 0) {
                bossX += (int)(BOSS_SPEED * bDx / bDist);
                bossY += (int)(BOSS_SPEED * bDy / bDist);
            }

            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle bossRect   = new Rectangle(bossX, bossY, BOSS_SIZE, BOSS_SIZE);

            if (playerRect.intersects(bossRect)) {
                if (bossDamageCooldown == 0) {
                    playerHP -= DAMAGE_PER_TICK;
                    bossDamageCooldown = DAMAGE_DELAY;
                    if (playerHP <= 0) {
                        playerHP = 0;
                        gameOver = true;
                        playSound(deathSound);  // play lost.wav on death
                    } else {
                        playSound(damageSound); // play dmg.wav on hit
                    }
                }
            }
        }

        boolean allEnemiesDead = true;
        for (int i = 0; i < enemyCount; i++) {
            if (enemyAlive[i]) {
                allEnemiesDead = false;
                break;
            }
        }

        boolean bossDefeated = !bossAlive || (waveNumber != 10 && waveNumber != 20);

        if (allEnemiesDead && bossDefeated) {
            if (waveNumber == 20) {
                victory = true; // victory.wav plays in VictoryScreen
            } else if (waveNumber < MAX_WAVES) {
                waveNumber++;
                spawnWave();
            }
        }

    }

    // Triggers a 360 degree slash — kills enemies and damages boss within SLASH_RANGE
    public void performSlash() {

        // Pick one of the 3 slash sounds at random
        playSound(slashSounds[rand.nextInt(3)]);

        int playerCenterX = playerX + PLAYER_SIZE / 2;
        int playerCenterY = playerY + PLAYER_SIZE / 2;

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

        slashFrame = 0;
        slashTimer = 0;

    }
    // ends here

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (waveNumber <= 10) {
            g.drawImage(mapBackground1, 0, 0, this);
        } else {
            g.drawImage(mapBackground2, 0, 0, this);
        }

        g.drawImage(playerImage, playerX, playerY, this);

        if (slashFrame >= 0 && slashFrame < 14) {
            int slashSize = PLAYER_SIZE * 4;
            int slashX = playerX - slashSize / 2 + PLAYER_SIZE / 2;
            int slashY = playerY - slashSize / 2 + PLAYER_SIZE / 2;
            g.drawImage(slashFrames[slashFrame], slashX, slashY, this);
        }

        for (int i = 0; i < enemyCount; i++) {
            if (!enemyAlive[i]) continue;

            if (waveNumber <= 10) {
                g.drawImage(enemy1Image, enemyX[i], enemyY[i], this);
            } else {
                g.drawImage(enemy2Image, enemyX[i], enemyY[i], this);
            }
        }

        if (bossAlive) {
            if (waveNumber <= 10) {
                g.drawImage(boss1Image, bossX, bossY, this);
            } else {
                g.drawImage(boss2Image, bossX, bossY, this);
            }

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

        g.setColor(Color.DARK_GRAY);
        g.fillRect(10, 30, 200, 18);

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

        // --- Victory --- launches VictoryScreen (plays victory.wav) and closes game frame
        if (victory) {
            gameLoop.stop();
            dispose();
            new VictoryScreen();
        }

    }

    public void dispose() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    // chatgpt
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP)    keysHeld[0] = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN)  keysHeld[1] = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT)  keysHeld[2] = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keysHeld[3] = true;

        if (k == KeyEvent.VK_ESCAPE && !gameOver) {
            paused = !paused;
        }

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

    public void keyTyped(KeyEvent e) { }

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

    public void mousePressed(MouseEvent e) {

        if (gameOver || paused) return;

        if (slashCooldown == 0) {
            performSlash();
            slashCooldown = SLASH_COOLDOWN;
        }

    }

    public void mouseReleased(MouseEvent e) { }
    public void mouseClicked(MouseEvent e)  { }
    public void mouseEntered(MouseEvent e)  { }
    public void mouseExited(MouseEvent e)   { }

}
