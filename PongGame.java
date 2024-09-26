import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;



class Ball {
    int x, y;
    int diameter;
    double xSpeed, ySpeed;
    private int countdown = -1;  // Variable pour stocker le temps restant avant le début
    private Timer countdownTimer;  // Timer pour gérer le décompte


    public Ball(int x, int y, int diameter, double xSpeed, double ySpeed) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }
    public void move() {
        x += xSpeed;
        y += ySpeed;
    }

    public void reverseX() {
        xSpeed = -xSpeed;
    }

    public void reverseY() {
        ySpeed = -ySpeed;
    }
}

public class PongGame extends JPanel implements KeyListener, ActionListener {
    private int paddleWidth = 10;
    private int paddle1Height = 120;
    private int paddle2Height = 120;
    private int paddle1Y = 150, paddle2Y = 150;
    private int ballX = 250, ballY = 150, ballDiameter = 50;
    private double ballXSpeed = 5;
    private double ballYSpeed = 5; // Changer en double
    private int countdown = -1;  // Variable pour stocker le temps restant avant le début
    private Timer countdownTimer; 

    private int ScoreP1 = 0; // Score pour le joueur 1
    private int ScoreP2 = 0; // Score pour le joueur 2

    private int BonusType; 


    

    // Bonus/Malus
    private int bonusX = -100, bonusY = -100, bonusWidth = 100, bonusHeight = 100;
    private boolean bonusActive = false;
    private boolean isBonus = true;  // Bonus (vert) ou malus (rouge)

    private Timer timer;
    private Timer paddle1MoveTimer;
    private Timer paddle2MoveTimer;
    private Timer bonusTimer;
    private Timer malusTimer;  // Timer pour la durée du malus

    private boolean paddle1MovingUp = false, paddle1MovingDown = false;
    private boolean paddle2MovingUp = false, paddle2MovingDown = false;

    private Random random;


    private List<Ball> balls;
    // Variable pour suivre quel paddle a touché la balle en dernier
    private boolean lastHitByPaddle1 = false; 
    private boolean controlsInvertedPaddle1 = false; // Inversion des contrôles du paddle 1
    private boolean controlsInvertedPaddle2 = false; // Inversion des contrôles du paddle 2

 // Variables pour les sons
 private SoundPlayer paddleHitSound;
 private SoundPlayer bonusSound;
 private SoundPlayer malusSound;
 private SoundPlayer ambianceSound;
 
 private Image bonusSpeedUpImage;
 private Image controlemalus;
 private Image malusMultiBallImage;
 private Image malusPaddleShrinkImage;
 


 public PongGame() {
    this.setPreferredSize(new Dimension(1200, 800));
    this.setBackground(new Color(0x0F9DE8));

    this.setFocusable(true);
    this.addKeyListener(this);
    timer = new Timer(3, this);
    timer.start();

    paddle1MoveTimer = new Timer(10, evt -> movePaddle1());
    paddle2MoveTimer = new Timer(10, evt -> movePaddle2());


    random = new Random();

    balls = new ArrayList<>();
    balls.add(new Ball(250, 150, ballDiameter, ballXSpeed, ballYSpeed)); // Ajouter la première balle


    // Timer pour générer des bonus/malus toutes les 7 secondes
    bonusTimer = new Timer(7000, evt -> spawnBonus());
    bonusTimer.start();

    // Initialisation des sons
    bonusSound = new SoundPlayer("bonus.wav");
    malusSound = new SoundPlayer("malus.wav");
    ambianceSound = new SoundPlayer("ambiance.wav");
   
    bonusSpeedUpImage = new ImageIcon("speed.png").getImage();
    controlemalus = new ImageIcon("controle.png").getImage();
    malusMultiBallImage = new ImageIcon("multi.png").getImage();
    malusPaddleShrinkImage = new ImageIcon("shrink.png").getImage();
    

    ambianceSound.loop();
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    // Afficher les paddles
    g.setColor(new Color(0, 51,102)); // Bleu ciel (RGB: 135, 206, 250)

    g.fillRoundRect(0, paddle1Y, paddleWidth, paddle1Height, 20, 20);
    g.fillRoundRect(getWidth() - paddleWidth, paddle2Y, paddleWidth, paddle2Height, 20, 20);

   
        // Afficher toutes les balles
        for (Ball ball : balls) {
            g.fillOval(ball.x, ball.y, ball.diameter, ball.diameter);
        }
    // Afficher les scores
    g.setFont(new Font("Arial", Font.BOLD, 20));
    g.drawString("Player 1: " + ScoreP1, 50, 30);
    g.drawString("Player 2: " + ScoreP2, getWidth() - 150, 30);

    // Dessiner le bonus/malus s'il est actif
    if (bonusActive) {
        switch (BonusType) {
            case 1:
                g.drawImage(bonusSpeedUpImage, bonusX, bonusY, bonusWidth, bonusHeight, this);
                break;
            case 2:
                g.drawImage(controlemalus, bonusX, bonusY, bonusWidth, bonusHeight, this);
                break;
            case 3:
                g.drawImage(malusPaddleShrinkImage, bonusX, bonusY, bonusWidth, bonusHeight, this);
                break;
            case 4:
                g.drawImage(malusMultiBallImage, bonusX, bonusY, bonusWidth, bonusHeight, this);
                break;
        }
    }
    if (countdown > 0) {
        g.setFont(new Font("Arial", Font.BOLD, 100));
        g.setColor(Color.RED);
        g.drawString(String.valueOf(countdown), getWidth() / 2 - 50, getHeight() / 2);
    }
}


    @Override
public void actionPerformed(ActionEvent e) {
    if (countdown > 0) {
        return; // Ne pas mettre à jour la balle pendant le décompte
    }

    for (Ball ball : balls) {
        ball.move();
        
        // Gestion des rebonds sur le haut et le bas
        if (ball.y <= 0 || ball.y >= getHeight() - ball.diameter) {
            ball.reverseY();
        }

        // Paddle 1 touche la balle
        if (ball.x <= paddleWidth && ball.y + ball.diameter >= paddle1Y && ball.y <= paddle1Y + paddle1Height) {
            ball.xSpeed = Math.abs(ball.xSpeed); // Assurez-vous que la balle va à droite
            int contactY = ball.y + ball.diameter / 2 - paddle1Y; // Point de contact relatif
            double normalizedContact = (double) contactY / paddle1Height; // Normaliser le contact
            ball.ySpeed = (normalizedContact - 0.5) * 2 * ball.xSpeed; // Ajuster la vitesse en Y
            lastHitByPaddle1 = true;
        }

        // Paddle 2 touche la balle
        if (ball.x >= getWidth() - paddleWidth - ball.diameter && ball.y + ball.diameter >= paddle2Y && ball.y <= paddle2Y + paddle2Height) {
            ball.xSpeed = -Math.abs(ball.xSpeed); // Assurez-vous que la balle va à gauche
            int contactY = ball.y + ball.diameter / 2 - paddle2Y; // Point de contact relatif
            double normalizedContact = (double) contactY / paddle2Height; // Normaliser le contact
            ball.ySpeed = (0.5 - normalizedContact) * 2 * ball.xSpeed; // Ajuster la vitesse en Y
            lastHitByPaddle1 = false; // On a touché le paddle 2
        }
    
        // Gérer les sorties de balle
        if (ball.x < 0 || ball.x > getWidth()) {
            scoreP(); // Appeler la méthode pour gérer le score
            // Ne réinitialisez pas la position de la balle ici
        }
    }
    
    // Gérer la collision de la balle avec le bonus/malus
    if (bonusActive && balls.stream().anyMatch(ball -> ball.x + ball.diameter >= bonusX && ball.x <= bonusX + bonusWidth &&
            ball.y + ball.diameter >= bonusY && ball.y <= bonusY + bonusHeight)) {
        applyBonusOrMalus();
        bonusActive = false;  // Désactiver le bonus/malus après collision
    }

    repaint();
}



    

    // Méthode pour déplacer la paddle 1 en fonction de la direction
    private void movePaddle1() {
        if (controlsInvertedPaddle1) {  // Vérifier si les contrôles sont inversés
            if (paddle1MovingDown && paddle1Y > 0) {
                paddle1Y -= 5;  // Inverser le mouvement
            }
            if (paddle1MovingUp && paddle1Y < getHeight() - paddle1Height) {
                paddle1Y += 5;  // Inverser le mouvement
            }
        } else {
            if (paddle1MovingUp && paddle1Y > 0) {
                paddle1Y -= 5;
            }
            if (paddle1MovingDown && paddle1Y < getHeight() - paddle1Height) {
                paddle1Y += 5;
            }
        }
    }

    // Méthode pour déplacer la paddle 2 en fonction de la direction
    private void movePaddle2() {
        if (controlsInvertedPaddle2) {  // Vérifier si les contrôles sont inversés
            if (paddle2MovingDown && paddle2Y > 0) {
                paddle2Y -= 5;  // Inverser le mouvement
            }
            if (paddle2MovingUp && paddle2Y < getHeight() - paddle2Height) {
                paddle2Y += 5;  // Inverser le mouvement
            }
        } else {
            if (paddle2MovingUp && paddle2Y > 0) {
                paddle2Y -= 5;
            }
            if (paddle2MovingDown && paddle2Y < getHeight() - paddle2Height) {
                paddle2Y += 5;
            }
        }
    }

    private static final int MAX_SCORE = 10;

    private void scoreP() {
        // Vérifier de quel côté la balle est sortie
        if (ballX < 0) {  // Si la balle sort du côté gauche
            ScoreP2 += 1;  // Le joueur 2 marque un point
        } else if (ballX > getWidth()) {  // Si la balle sort du côté droit
            ScoreP1 += 1;  // Le joueur 1 marque un point
        }
    
        // Vérifiez si un joueur a atteint le score maximum
        if (ScoreP1 >= MAX_SCORE || ScoreP2 >= MAX_SCORE) {
            endGame();  // Appeler la méthode pour finir le jeu
            return;
        }
    
        resetGameState(); // Réinitialiser l'état du jeu avant de démarrer le compte à rebours
        startCountdown(); // Démarrer le décompte de 3 secondes
    }
    
    
    
    

    private void startCountdown() {
        countdown = 3;  // Initialiser le décompte à 3
        countdownTimer = new Timer(1000, evt -> {
            countdown--;  // Décrémenter le décompte
            if (countdown <= 0) {
                countdownTimer.stop();  // Arrêter le timer à 0
                resetBallPosition();  // Réinitialiser la balle après le décompte
                countdown = -1; // Réinitialiser le décompte pour qu'il ne soit plus affiché
            }
            repaint();  // Redessiner l'écran pour afficher le décompte
        });
        countdownTimer.start();  // Démarrer le timer du décompte
    }
    
    
    private void resetGameState() {
        // Réinitialiser la taille des paddles
        paddle1Height = 120;
        paddle2Height = 120;
        
        // Réinitialiser les contrôles inversés
        controlsInvertedPaddle1 = false;
        controlsInvertedPaddle2 = false;
    
        // Réinitialiser le nombre de balles à une seule
        balls.clear(); // Supprimer toutes les balles
        balls.add(new Ball(getWidth() / 2 - ballDiameter / 2, getHeight() / 2 - ballDiameter / 2, ballDiameter, ballXSpeed, ballYSpeed)); // Ajouter une nouvelle balle
    
        // Autres réinitialisations nécessaires...
    }

    private void resetBallPosition() {
        // Réinitialiser la position de chaque balle au centre
        for (Ball ball : balls) {
            ball.x = getWidth() / 2 - ball.diameter / 2;
            ball.y = getHeight() / 2 - ball.diameter / 2;
    
            // Générer une direction aléatoire pour chaque balle
            double angle = random.nextDouble() * Math.PI / 2 + Math.PI / 2; // Angle entre 45 et 135 degrés
            ball.xSpeed = Math.cos(angle) * 5;  // Vitesse initiale
            ball.ySpeed = Math.sin(angle) * 5;  // Vitesse initiale
        }
    }
    

    
    

    // Génération aléatoire d'un bonus/malus
    private void spawnBonus() {
        BonusType = random.nextInt(4) + 1;  // Choisir un type de bonus/malus
        bonusX = random.nextInt(getWidth() - bonusWidth);
        bonusY = random.nextInt(getHeight() - bonusHeight);
        bonusActive = true;
    }

    private void applyBonusOrMalus() {
        switch (BonusType) {
            case 1: // Bonus - Accélérer la balle
                for (Ball ball : balls) {
                    ball.xSpeed *= 1.5; // Augmente la vitesse de la balle
                }
                break;
            case 2: // Malus - Inverser les contrôles
                controlsInvertedPaddle1 = !controlsInvertedPaddle1;
                controlsInvertedPaddle2 = !controlsInvertedPaddle2;
                break;
            case 3: // Malus - Rétrécir le paddle
                if (lastHitByPaddle1) {
                    paddle1Height = Math.max(10, paddle1Height / 2); // Rétrécir le paddle 1
                } else {
                    paddle2Height = Math.max(10, paddle2Height / 2); // Rétrécir le paddle 2
                }
                break;
            case 4: // Malus - Ajouter une balle supplémentaire
                balls.add(new Ball(250, 150, ballDiameter, ballXSpeed, ballYSpeed)); // Ajouter une nouvelle balle
                break;
        }
    }

    // // Méthode pour démarrer le timer de malus
    // private void startMalusTimer(int player) {
    //     malusTimer = new Timer(10000, evt -> {
    //         if (player == 1) {
    //             controlsInvertedPaddle1 = false;  // Rétablir les contrôles du joueur 1
    //         } else {
    //             controlsInvertedPaddle2 = false;  // Rétablir les contrôles du joueur 2
    //         }
    //         malusTimer.stop();  // Arrêter le timer de malus
    //     });
    //     malusTimer.setRepeats(false); // Exécuter une seule fois
    //     malusTimer.start();  // Démarrer le timer
    // }

    private void endGame() {
        String winner = (ScoreP1 >= MAX_SCORE) ? "Player 1 wins!" : "Player 2 wins!";
        JOptionPane.showMessageDialog(this, winner, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        timer.stop();  // Arrêter le jeu
        bonusTimer.stop();  // Arrêter le timer des bonus/malus
        if (countdownTimer != null) {
            countdownTimer.stop();  // Arrêter le timer de décompte
        }
    }
    

    @Override
    public void keyPressed(KeyEvent e) {
        // Paddle 1 (gauche)
        if (e.getKeyCode() == KeyEvent.VK_W) {
            paddle1MovingUp = true;
            paddle1MovingDown = false;
            if (!paddle1MoveTimer.isRunning()) {
                paddle1MoveTimer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            paddle1MovingDown = true;
            paddle1MovingUp = false;
            if (!paddle1MoveTimer.isRunning()) {
                paddle1MoveTimer.start();
            }
        }

        // Paddle 2 (droite)
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            paddle2MovingUp = true;
            paddle2MovingDown = false;
            if (!paddle2MoveTimer.isRunning()) {
                paddle2MoveTimer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            paddle2MovingDown = true;
            paddle2MovingUp = false;
            if (!paddle2MoveTimer.isRunning()) {
                paddle2MoveTimer.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Paddle 1 (gauche)
        if (e.getKeyCode() == KeyEvent.VK_W) {
            paddle1MovingUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            paddle1MovingDown = false;
        }
        if (!paddle1MovingUp && !paddle1MovingDown) {
            paddle1MoveTimer.stop();
        }

        // Paddle 2 (droite)
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            paddle2MovingUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            paddle2MovingDown = false;
        }
        if (!paddle2MovingUp && !paddle2MovingDown) {
            paddle2MoveTimer.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong Game with Bonus/Malus");
        PongGame game = new PongGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}