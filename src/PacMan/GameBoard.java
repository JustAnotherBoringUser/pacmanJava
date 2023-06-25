package PacMan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import CSVReader.CSVReader;

/**
 * The GameBoard class represents the board panel where the game is played
 * It extends JPanel and implements ActionListener.
 * @see PacMan
 */
public class GameBoard extends JPanel implements ActionListener {

    private Dimension d;
    static CSVReader csv;
    static String playerNickname;
    private ArrayList<ArrayList<String>> leaderBoard;
    
    private static int dyingSecondsCount=5;
    public static ScheduledExecutorService executor ;
    private final Font smallFont = new Font("Arial",Font.BOLD, 18);
    private boolean inGame = false;
    private boolean powerup=false;
    private boolean dying=false;
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS_X = 17; 
    private final int N_BLOCKS_Y = 17 ;
    private final int SCREEN_SIZE_X = N_BLOCKS_X * BLOCK_SIZE;
    private final int SCREEN_SIZE_Y = N_BLOCKS_Y * BLOCK_SIZE;
    private final int PacMan_SPEED = 6;

    private int lives, score;

    private int[] dx, dy;

    private Image heart, scaredghost, redghost, orangeghost, blueghost, pinkghost;
    private Image up, down, left, right;

    private Image title;

    private long[] ghostEatenTime;

    private int PacMan_x, PacMan_y, PacMand_x, PacMand_y;
    private int req_dx, req_dy;

    private final int[] levelData = {

    	    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0,19,26,26,18,18,26,18,18,18,26,18,18,26,26,22, 0,
            0,21, 0, 0,17,20, 0,17,16,20, 0,17,20, 0, 0,21, 0,
            0,21, 0,35,16,20, 0,25,24,28, 0,17,16,38, 0,21, 0,
            0,17,26,16,16,20, 0, 0, 0, 0, 0,17,16,16,26,20, 0,
            0,21, 0,17,24,16,18,18,18,18,18,16,24,20, 0,21, 0,
            0,21, 0,21, 0,17,16,24,16,24,16,20, 0,21, 0,21, 0,
            0,17,26,28, 0,17,20, 0,21, 0,17,20, 0,25,26,20, 0,
            0,21, 0, 0, 0,17,20, 0,29, 0,17,20, 0, 0, 0,21, 0,
            0,17,26,22, 0,17,20, 0, 0, 0,17,20, 0,19,26,20, 0,
            0,21, 0,21, 0,17,16,18,18,18,16,20, 0,21, 0,21, 0,
            0,21, 0,17,18,16,24,24,24,24,24,16,18,20, 0,21, 0,
            0,17,26,16,16,20, 0, 0, 0, 0, 0,17,16,16,26,20, 0,
            0,21, 0,41,16,20, 0,19,18,22, 0,17,16,44, 0,21, 0,
            0,21, 0, 0,17,20, 0,17,16,20, 0,17,20, 0, 0,21, 0, 
            0,25,26,26,24,24,26,24,24,24,26,24,24,26,26,28, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0

    };
    private int[] screenData;

    private Timer timer;
    

    private Ghost[] ghosts;
    private Thread[] ghostThreads;

    /**
     * Constructor of GameBoard class
     * Sets up needed options.
     * Creates class object and starts the ghost threads.
     * @see Ghost
     * @see CSVReader
     */
    public GameBoard() {

        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        // Initialize the ghosts
        ghosts = new Ghost[3];
        ghostThreads = new Thread[3];
        ghosts[0] = new Ghost("red");
        ghosts[1] = new Ghost("blue");
        ghosts[2] = new Ghost("orange");
        
        // Start the ghost threads
        for (int i = 0; i < ghostThreads.length; i++) {
            ghostThreads[i] = new Thread(ghosts[i]);
            ghostThreads[i].start();
        }
        initGame();
        csv = new CSVReader();
        leaderBoard = csv.getParsedFile();

    }

    /**
     * Loads the images used in the game.
     */
    private void loadImages() {
        down = new ImageIcon("images/down.gif").getImage();
        up = new ImageIcon("images/up.gif").getImage();
        left = new ImageIcon("images/left.gif").getImage();
        right = new ImageIcon("images/right.gif").getImage();
        heart = new ImageIcon("images/heart.png").getImage();
        title = new ImageIcon("images/title.jpg").getImage();
        scaredghost=new ImageIcon("images/scaredghost.png").getImage();
        redghost=new ImageIcon("images/redghost.png").getImage();
        orangeghost=new ImageIcon("images/orangeghost.png").getImage();
        blueghost=new ImageIcon("images/blueghost.png").getImage();
        pinkghost=new ImageIcon("images/pinkghost.png").getImage();
    }
    
    /**
     * Initializes various variables used in the game.
     */
    private void initVariables() {

        screenData = new int[N_BLOCKS_X * N_BLOCKS_Y];
        d = new Dimension(500, 500);
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(70, this);
        timer.start();
        ghostEatenTime = new long[3];
    }

    /**
     * Change view on screen depending on state of the game
     *  @param g the Graphics object used for drawing
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if(powerup==true)
        {
            drawPowerUpTime(g2d);
        }

        if (inGame) {
            try {
                playGame(g2d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (lives==0){
            showGameOverScreen(g2d);
        } else {
            showIntroScreen(g2d);
            g.drawImage(title, 80, 15, 250, 50, this) ;
        }
        
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    /**
     * Equivalent of game loop
     *  @param g2d the Graphics object used for drawing
     *  @throws IOException if an I/O error occurs
     */
    private void playGame(Graphics2D g2d) throws IOException {

        if (dying) {

            death();

        } else {

            movePacMan();
            drawPacMan(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    /**
     * Shows the start screen.
     *
     * @param g2d the Graphics object used for drawing
     * @see paintComponent()
     */
    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, 110, 185);
    }

    /**
     * Shows GAME OVER screen and top 5 user scores.
     *
     * @param g2d the Graphics object used for drawing
     * @see paintComponent()
     */
    private void showGameOverScreen(Graphics g2d){

    	g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);
        
        String title = "GAME OVER";
        String restart = "Press SPACE to restart";   
        String top5 = "Top 5:";  
        g2d.setColor(Color.yellow);
        g2d.drawString(title,140,90);
        g2d.drawString(restart, 95, 150);
        g2d.drawString(top5, 130, 210);
    
        int count = 1;
        int startY = 250;
        for (int i = 0; i < csv.getParsedFile().size(); i++) {
            ArrayList<String> entry = csv.getParsedFile().get(i);
            String nick = entry.get(0);
            String score = entry.get(1);
            String text = count + ". " + nick + ": " + score;
            g2d.drawString(text, 130, startY + i * 30);

            count++;

            if (count > 5) {
                break;
            }
        }

    }
    
    /**
     * Initializes the game.
     */ 
    private void initGame() {

        lives = 3;
        score = 0;
        initLevel();

    }

    /**
     * Initializes the current level of the game.
     */
    private void initLevel() {

        int i;

        for (i = 0; i < N_BLOCKS_X * N_BLOCKS_Y; i++) {
            screenData[i] = levelData[i];
        }


        continueLevel();
    }

    /**
     * Reset PacMan and ghosts to initial positions in case of PacMan losing one of his lives
     */
    private void continueLevel() {

        int dx = 1;
        int random;
        
        for (int i = 0; i < ghosts.length; i++) {
            Ghost ghost = ghosts[i];
            ghost.respawn(); // reset position of all ghosts
        }

        PacMan_x = 10 * BLOCK_SIZE;  //start position
        PacMan_y = 11 * BLOCK_SIZE;
        PacMand_x = 0;	//reset direction move
        PacMand_y = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }
    
    /**
     * Ends the game if PacMan lost all his 3 lives
     * @throws IOException if an I/O error occurs
     */
    private void death() throws IOException {

        lives--;

        if (lives == 0) {
            inGame = false;
            checkScore();
        }
        
        continueLevel();
    }
    
    /**
     * Saves the score to .csv file with nick name provided by user
     * @throws IOException if an I/O error occurs
     */
    public void checkScore() throws IOException {
        String name=JOptionPane.showInputDialog("Enter your nickname:");
        csv.saveScore(score, name);
}

    /**
     * Check if all pellets have been eaten and starts new level
     * @see initLevel()
     */
    private void checkMaze() {

        int i = 0;
        boolean finishedLevel1 = true;

        for ( i = 0; i < N_BLOCKS_X * N_BLOCKS_Y; i++) {
            if ((screenData[i] & 48) != 0) { // Check if any dots (16 + 32) are present
                finishedLevel1 = false;
                break;
            }
        }
        
        if(score==182||score==374||score==566)
        {
            finishedLevel1=true;
        }

        if (finishedLevel1) {

            score += 10;

            initLevel();
        }
    }

    /**
     * Moves ghosts on the board and determines collision behavior
     * @param g2d the Graphics2D object used for drawing
     */
    private void moveGhosts(Graphics2D g2d) {
        for (int i = 0; i < ghosts.length; i++) {
            Ghost ghost = ghosts[i];
            ghost.draw(g2d);

            if (ghost.collidesWith(PacMan_x, PacMan_y) && inGame) {
                if (powerup) {
                    ghost.setAlive(false);
                    ghostEatenTime[i] = System.currentTimeMillis();
                } else {
                    dying = true;
                }
            }

            // Check if the ghost is dead and if enough time has passed to respawn it
            if (!ghost.isAlive() && System.currentTimeMillis() - ghostEatenTime[i] >= 5000) {
            	if(!powerup) {
                ghost.respawn(); // Add a respawn method to the Ghost class to reset its position
                score += 200; //extra points for eateing the ghost
            	}
            }
        }
    }

    /**
     * Moves PacMan on the game board.
     */
    private void movePacMan() {

        int pos;
        int ch;

        if (PacMan_x % BLOCK_SIZE == 0 && PacMan_y % BLOCK_SIZE == 0) {
            pos = PacMan_x / BLOCK_SIZE + N_BLOCKS_X * (int) (PacMan_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (int) (ch & 15);
                score++;
            }

            if ((ch & 32) != 0) {
                if(!powerup) {

                    powerup = true;
                    dyingSecondsCount = 5;
                    executor = Executors.newScheduledThreadPool(0);
                    executor.scheduleAtFixedRate(setDying, 0, 1, TimeUnit.SECONDS);
                }
                else if(powerup)
                {
                    dyingSecondsCount = 5;
                    screenData[pos] = (int) (ch & 15);
                    score += 5;
                }
            }


            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    PacMand_x = req_dx;
                    PacMand_y = req_dy;
                }
            }

            // Check for standstill
            if ((PacMand_x == -1 && PacMand_y == 0 && (ch & 1) != 0)
                    || (PacMand_x == 1 && PacMand_y == 0 && (ch & 4) != 0)
                    || (PacMand_x == 0 && PacMand_y == -1 && (ch & 2) != 0)
                    || (PacMand_x == 0 && PacMand_y == 1 && (ch & 8) != 0)) {
                PacMand_x = 0;
                PacMand_y = 0;
            }
        }
        PacMan_x = PacMan_x + PacMan_SPEED * PacMand_x;
        PacMan_y = PacMan_y + PacMan_SPEED * PacMand_y;
    }

    /**
     * Changes PacMan picture when he change direction
     */
    private void drawPacMan(Graphics2D g2d) {

        if (req_dx == -1) {
            g2d.drawImage(left, PacMan_x + 1, PacMan_y + 1, this);
        } else if (req_dx == 1) {
            g2d.drawImage(right, PacMan_x + 1, PacMan_y + 1, this);
        } else if (req_dy == -1) {
            g2d.drawImage(up, PacMan_x + 1, PacMan_y + 1, this);
        } else {
            g2d.drawImage(down, PacMan_x + 1, PacMan_y + 1, this);
        }
    }

    /**
     * Draws maze with pellets on it
     */
    private void drawMaze(Graphics2D g2d) {

        int i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE_Y; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE_X; x += BLOCK_SIZE) {

                g2d.setColor(new Color(25,25,166));
                g2d.setStroke(new BasicStroke(3));

                if ((levelData[i] == 0)) {
                    g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }
                
                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
                }

                if (((screenData[i]&32)!=0))
                {
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 5, y + 5, 12, 12);
                }

                i++;
            }
        }
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(0, 255, 0));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE_X / 2 + 76, SCREEN_SIZE_Y + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE_Y+1, this);
        }
    }

    private void drawPowerUpTime(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(255, 170, 0));
        String s = "Power Up: " + dyingSecondsCount;
        g.drawString(s, SCREEN_SIZE_X / 2-50, SCREEN_SIZE_Y + 16);

    }

    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) { 
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }
    
    Runnable setDying= new Runnable() {
        public void run() {
            powerup = true;
            dyingSecondsCount--;

            if(dyingSecondsCount==0){
                powerup=false;
                executor.shutdownNow();
            }
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
   
        repaint();
    }

    /**
     * The Ghost class inside GameBoard class represents a ghost entity in the game.
     * Each ghost is controlled by a separate thread and implements the Runnable interface.
     * The ghost can move in different directions and has behavior based on its color: red ghost chases PacMan,
     * orange ghost moves randomly, and blue ghost imitates the behavior of red ghost half of the time
     * and move randomly the other half of the time.
     * @see GameBoard
     * @see PacMan
     */
    private class Ghost implements Runnable {
        private static final int BLOCK_SIZE = 24;
        private static final int[] dx = { -1, 0, 1, 0 };
        private static final int[] dy = { 0, -1, 0, 1 };

        private int x;
        private int y;
        private int direction;
        private boolean alive;
        private String color;

        /**
         * Constructs a new Ghost object with the specified color.
         *
         * @param color the color of the ghost
         */
        public Ghost(String color) {
            this.color = color;
            this.alive = true;
            
            // Set initial position and direction of the ghost
            this.x = 3 * BLOCK_SIZE; 
            this.y = 3 * BLOCK_SIZE; 
            this.direction = 0; // direction (0 = left, 1 = up, 2 = right, 3 = down)
        }

        /**
         * This method is executed when the ghost thread starts.
         * It continuously moves the ghost and waits between movements.
         * This method must be implemented because of the Runnable interface.
         */
        @Override
        public void run() {
            while (true) {
            	move();
                try {
                    // Delay between movements
                   Thread.sleep(350);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Moves the ghost based on its color and state.
         */
        public void move() {
            if (color.equals("red")) {
            	Red();
           } else if(color.equals("orange")){
        	   random_move();
           } else {
        	   boolean LikeRed = Math.random() < 0.5; 
        	   if(LikeRed) {
        		   Red();
        	   } else {
        		   random_move(); 
        	   }
        	  // System.out.println(LikeRed);
           }
        } 
        
        /**
         * Moves the red ghost.
         * If the power-up is active, it moves randomly; otherwise, it chases PacMan.
         */
        public void Red() {
        	if(!powerup) {
        	chase();
        	} else {
        		random_move();
        	}
        }
        
        /**
         * Makes the ghost chase PacMan.
         * It selects the best direction to move towards PacMan.
         */
        public void chase() {
            // Chase PacMan
            int pacManX = PacMan_x / BLOCK_SIZE;
            int pacManY = PacMan_y / BLOCK_SIZE;

            int currentBlockX = x / BLOCK_SIZE;
            int currentBlockY = y / BLOCK_SIZE;
            int pos = currentBlockX + N_BLOCKS_X * currentBlockY;

            ArrayList<Integer> possibleDirections = new ArrayList<>();

            if ((screenData[pos] & 1) == 0 && direction != 2) {
                possibleDirections.add(0); // Left
            }

            if ((screenData[pos] & 2) == 0 && direction != 3) {
                possibleDirections.add(1); // Up
            }

            if ((screenData[pos] & 4) == 0 && direction != 0) {
                possibleDirections.add(2); // Right
            }

            if ((screenData[pos] & 8) == 0 && direction != 1) {
                possibleDirections.add(3); // Down
            }

            int targetDirection = -1;

            if (possibleDirections.contains(0) && pacManX < currentBlockX) {
                targetDirection = 0; // Move left
            } else if (possibleDirections.contains(1) && pacManY < currentBlockY) {
                targetDirection = 1; // Move up
            } else if (possibleDirections.contains(2) && pacManX > currentBlockX) {
                targetDirection = 2; // Move right
            } else if (possibleDirections.contains(3) && pacManY > currentBlockY) {
                targetDirection = 3; // Move down
            }

            if (targetDirection != -1 && !possibleDirections.isEmpty()) {
                direction = targetDirection;
            } else {
                // Select a new direction that avoids walls
                ArrayList<Integer> validDirections = new ArrayList<>();

                for (Integer dir : possibleDirections) {
                    int nextBlockX = currentBlockX + dx[dir];
                    int nextBlockY = currentBlockY + dy[dir];
                    int nextPos = nextBlockX + N_BLOCKS_X * nextBlockY;

                    if ((screenData[nextPos] & 16) == 0) { // Check if next block is not a wall
                        validDirections.add(dir);
                    }
                }

                if (!validDirections.isEmpty()) {
                    int randomIndex = (int) (Math.random() * validDirections.size());
                    direction = validDirections.get(randomIndex);
                } else {
                    if ((screenData[pos] & 15) == 15) {
                        direction = 4; // Stop
                    } else {
                        direction = (direction + 2) % 4; // Reverse direction
                    }
                }
            }

            x += dx[direction] * BLOCK_SIZE;
            y += dy[direction] * BLOCK_SIZE;
        }

        /**
         * Makes the ghost move randomly.
         * It selects a valid direction randomly from the available directions.
         */
        public void random_move() {
        	
		    int currentBlockX = x / BLOCK_SIZE;
		    int currentBlockY = y / BLOCK_SIZE;
		    int pos = currentBlockX + N_BLOCKS_X * currentBlockY;

		    ArrayList<Integer> possibleDirections = new ArrayList<>();

		    if ((screenData[pos] & 1) == 0 && direction != 2) {
		        possibleDirections.add(0); // Left
		    }

		    if ((screenData[pos] & 2) == 0 && direction != 3) {
		        possibleDirections.add(1); // Up
		    }

		    if ((screenData[pos] & 4) == 0 && direction != 0) {
		        possibleDirections.add(2); // Right
		    }

		    if ((screenData[pos] & 8) == 0 && direction != 1) {
		        possibleDirections.add(3); // Down
		    }

		    if (possibleDirections.isEmpty()) {
		        if ((screenData[pos] & 15) == 15) {
		            direction = 4; // Stop
		        } else {
		            direction = (direction + 2) % 4; // Reverse direction
		        }
		    } else {
		        int randomIndex = (int) (Math.random() * possibleDirections.size());
		        direction = possibleDirections.get(randomIndex);
		    }

		    x += dx[direction] * BLOCK_SIZE;
		    y += dy[direction] * BLOCK_SIZE;
		  


        }

        /**
         * Draws the ghost on the screen, choosing right image based on ghost's color.
         *
         * @param g2d the Graphics2D object used for drawing
         */
        public void draw(Graphics2D g2d) {
        if (alive) {
        if(!powerup){
        	if(color == "red") {
        		g2d.drawImage(redghost, x, y, null);
        	} else if(color == "orange") {
        		g2d.drawImage(orangeghost, x, y, null);
        	}else {
        		g2d.drawImage(blueghost, x, y, null);
        	}
        } else {
          g2d.drawImage(scaredghost, x, y, null);
        }
        }
        }

        /**
         * Checks if the ghost collides with PacMan.
         *
         * @param px the x-coordinate of PacMan
         * @param py the y-coordinate of PacMan
         * @return true if the ghost collides with PacMan, false otherwise
         */
        public boolean collidesWith(int px, int py) {
            // Calculate the distance between object and PacMan
            double distance = Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2));

            double collisionThreshold = 15.0;
            return distance <= collisionThreshold;
        }

        
        /**
         * Sets the alive state of the ghost.
         *
         * @param alive the alive state to set
         */
        public void setAlive(boolean alive) {
            this.alive = alive;
        }
        
        /**
         * Checks if the ghost is alive.
         *
         * @return true if the ghost is alive, false otherwise
         */
        public boolean isAlive() {
            return alive;
        }
        
        /**
         * Respawns the ghost by setting its position, direction, and alive state to their initial values.
         * This function is also used to reset ghosts' positions when PacMan loses one of his lives
         * @see GameBoard#continueLevel()
         */
        public void respawn() {
            // Set the ghost's position to its initial position
            x = 3 * BLOCK_SIZE; 
            y = 3 * BLOCK_SIZE;
            direction = 0;
            alive = true;
        }

    }

}