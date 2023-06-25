package PacMan;

import javax.swing.*;
import java.awt.*;

public class PacMan extends JFrame{

    public PacMan() {
        add(new GameBoard());
    }

    /**
     * Main method of the game.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        PacMan pac = new PacMan();
        pac.setVisible(true);
        pac.setTitle("PacMan");
        pac.setResizable(false);
        pac.setSize(425,475);
        pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pac.setLocationRelativeTo(null);


    }

}