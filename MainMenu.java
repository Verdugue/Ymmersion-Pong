import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenu {
    private static JFrame frame;

    public static void showMenu() {
        // Fermer la fenêtre existante si elle est déjà ouverte
        if (frame != null) {
            frame.dispose();
        }

        // Créer la fenêtre du menu principal
        frame = new JFrame("Pong Game Menu");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panneau de menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 1));

        JLabel title = new JLabel("Pong Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        menuPanel.add(title);

        // Bouton pour démarrer le jeu
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.addActionListener(e -> {
            frame.getContentPane().removeAll(); // Supprimer les éléments du menu
            PongGame game = new PongGame();     // Créer une instance de PongGame
            frame.add(game);                    // Ajouter le jeu à la fenêtre
            frame.revalidate();                 // Actualiser le contenu de la fenêtre
            frame.repaint();
            game.requestFocus();                // Donner le focus à l'objet PongGame pour capturer les entrées clavier
        });
        menuPanel.add(startButton);

        // Bouton pour quitter le jeu
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 24));
        exitButton.addActionListener(e -> System.exit(0));
        menuPanel.add(exitButton);

        frame.add(menuPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null); // Centre la fenêtre sur l'écran
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        showMenu(); // Afficher le menu au démarrage
    }
}
