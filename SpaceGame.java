/**
 * Project: Solo Lab 7 Assignment
 * Purpose: Solo Space Game
 * Course: IST 242
 * Author: Abdullah Koro
 * Date Developed: 6/17/24
 * Last Date Changed: 6/21/24
 * Revision: 1
 */

// Press S for shield

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SpaceGame extends JFrame implements KeyListener
{
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 50; // Adjusted to match sprite sheet
    private static final int OBSTACLE_HEIGHT = 50; // Adjusted to match sprite sheet
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static final int STAR_COUNT = 50;
    private static final int POWER_UP_SIZE = 20;
    private static final int MAX_HEALTH = 100;

    private int score = 0;
    private int playerHealth = MAX_HEALTH;
    private boolean shieldActive = false;
    private boolean isGameOver = false;
    private boolean isProjectileVisible = false;
    private boolean isFiring = false;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private int countdownTimer = 60; // 60 seconds countdown

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;
    private Timer gameTimer;
    private Timer countdownTimerObject;
    private List<Point> obstacles;
    private List<Point> stars;
    private List<Point> healthPowerUps;
    private Random random;
    private Image spaceshipImage;
    private Image obstacleSpriteSheet;
    private Clip fireClip;
    private Clip collisionClip;

    /**
     * Constructor to set up the game
     */
    public SpaceGame()
    {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        random = new Random();
        obstacles = new ArrayList<>();
        stars = new ArrayList<>();
        healthPowerUps = new ArrayList<>();

        for (int i = 0; i < STAR_COUNT; i++)
        {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }

        spaceshipImage = new ImageIcon("resources/spaceship.png").getImage();
        obstacleSpriteSheet = new ImageIcon("resources/obstacles.png").getImage();

        gamePanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setBounds(10, 10, 100, 20);

        healthLabel = new JLabel("Health: " + playerHealth);
        healthLabel.setForeground(Color.GREEN);
        healthLabel.setBounds(10, 30, 100, 20);

        timerLabel = new JLabel("Time: " + countdownTimer);
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setBounds(10, 50, 100, 20);

        gamePanel.setLayout(null);
        gamePanel.add(scoreLabel);
        gamePanel.add(healthLabel);
        gamePanel.add(timerLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;

        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;

        loadAudioClips();

        gameTimer = new Timer(20, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!isGameOver)
                {
                    update();
                    gamePanel.repaint();
                }
            }
        });

        countdownTimerObject = new Timer(1000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!isGameOver)
                {
                    countdownTimer--;
                    timerLabel.setText("Time: " + countdownTimer);
                    if (countdownTimer <= 0)
                    {
                        isGameOver = true;
                        gamePanel.repaint();
                    }
                }
            }
        });

        gameTimer.start();
        countdownTimerObject.start();
    }

    /**
     * Draws the game components
     *
     * @param g The graphics object to draw on
     */
    private void draw(Graphics g)



    {
        // Set background to black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw background stars
        for (Point star : stars)
        {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.fillRect(star.x, star.y, 2, 2);
        }

        // Draw player spaceship
        g.drawImage(spaceshipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);

        // Draw projectile
        if (isProjectileVisible)

        {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Draw obstacles using sprite sheet
        for (Point obstacle : obstacles)
        {
            int spriteX = (obstacle.x / OBSTACLE_WIDTH) % 4 * OBSTACLE_WIDTH;
            g.drawImage(obstacleSpriteSheet, obstacle.x, obstacle.y, obstacle.x + OBSTACLE_WIDTH, obstacle.y + OBSTACLE_HEIGHT, spriteX, 0, spriteX + OBSTACLE_WIDTH, OBSTACLE_HEIGHT, this);
        }

        // Draw health power-ups
        g.setColor(Color.PINK);
        for (Point powerUp : healthPowerUps)
        {
            g.fillRect(powerUp.x, powerUp.y, POWER_UP_SIZE, POWER_UP_SIZE);
        }

        // Draw shield
        if (shieldActive) {
            g.setColor(new Color(0, 255, 255, 128)); // Cyan color with transparency
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    /**
     * Updates the game state
     */
    private void update()
    {
        if (!isGameOver)
        {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++)
            {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT)
                {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles
            if (random.nextDouble() < 0.02)
            {
                int obstacleX = random.nextInt(WIDTH - OBSTACLE_WIDTH);
                obstacles.add(new Point(obstacleX, 0));
            }

            // Move health power-ups
            for (int i = 0; i < healthPowerUps.size(); i++)
            {
                healthPowerUps.get(i).y += OBSTACLE_SPEED;
                if (healthPowerUps.get(i).y > HEIGHT) {
                    healthPowerUps.remove(i);
                    i--;
                }
            }

            // Generate new health power-ups
            if (random.nextDouble() < 0.01)

            {
                int powerUpX = random.nextInt(WIDTH - POWER_UP_SIZE);
                healthPowerUps.add(new Point(powerUpX, 0));
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles)
            {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect))
                {
                    if (shieldActive)
                    {
                        continue;
                    }
                    playerHealth -= 10;
                    healthLabel.setText("Health: " + playerHealth);
                    playSound(collisionClip);
                    if (playerHealth <= 0) {
                        isGameOver = true;
                    }
                }
            }

            // Check collision with projectile
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    scoreLabel.setText("Score: " + score);
                    playSound(fireClip);
                    isProjectileVisible = false;
                    break;
                }
            }

            // Check collision with health power-ups
            for (int i = 0; i < healthPowerUps.size(); i++)

            {
                Rectangle powerUpRect = new Rectangle(healthPowerUps.get(i).x, healthPowerUps.get(i).y, POWER_UP_SIZE, POWER_UP_SIZE);
                if (playerRect.intersects(powerUpRect)) {
                    playerHealth = Math.min(playerHealth + 20, MAX_HEALTH);
                    healthLabel.setText("Health: " + playerHealth);
                    healthPowerUps.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Loads the audio clips for the game
     */
    private void loadAudioClips()
    {
        try {
            fireClip = AudioSystem.getClip();
            collisionClip = AudioSystem.getClip();
            ClassLoader cl = getClass().getClassLoader();
            fireClip.open(AudioSystem.getAudioInputStream(cl.getResource("resources/fire.wav")));
            collisionClip.open(AudioSystem.getAudioInputStream(cl.getResource("resources/collision.wav")));
            System.out.println("Fire and collision sounds loaded successfully.");
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Plays a given sound clip
     *
     * @param clip The sound clip to play
     */
    private void playSound(Clip clip)
    {
        if (clip != null)
        {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0)
        {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH)
        {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            playSound(fireClip);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_S)
        { // Shield activation I put it in top too
            shieldActive = !shieldActive;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}