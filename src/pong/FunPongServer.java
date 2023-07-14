package pong;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import audioCall.*;
import javax.swing.JFrame;

/**
 * @author User
 */
public class FunPongServer extends JFrame implements KeyListener, Runnable, WindowListener {

    private static final long serialVersionUID = 1L;

    // - Frame - //
    private static final String TITLE = "server";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 460;

    //Game Variables 
    boolean isRunning = false;
    boolean check = true;
    boolean initgame = false;

    //Players & Objects
    Ball movingBALL;
    private PlayerServer playerS;
    private PlayerClient playerC;

    private int ballSpeed = 5;
    private int barWidth = 30;
    private int barHeight = 80;
    private int max_Score = 9;
    private int barSpeed = 10;
    private boolean Restart = false;
    private boolean restartON = false;

    //Server
    private static Socket clientSoc = null;
    private static ServerSocket serverSoc = null;
    private int portAdd;

    //Graphical
    private Graphics g;
    private Font sFont = new Font("Comic sans ms", Font.ITALIC, 72);
    private Font mFont = new Font("Comic sans ms", Font.ITALIC, 48);
    private Font nFont = new Font("Comic sans ms", Font.ITALIC, 28);
    private Font rFont = new Font("Comic sans ms", Font.ITALIC, 16);
    private String[] message;	//Split Message to two piece in an array
    private Thread movB;

    public FunPongServer(String servername, String portAdd) {

        //Create player
        playerS = new PlayerServer();
        playerC = new PlayerClient("");
        playerS.setName(servername);

        //Setting Frame
        this.portAdd = Integer.parseInt(portAdd);
        this.isRunning = true;
        this.setTitle(TITLE + "::port number[" + portAdd + "]");
        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);

        //Create Moving Ball 
        movingBALL = new Ball(playerS.getBallx(), playerS.getBally(), ballSpeed, ballSpeed, 45, WIDTH, HEIGHT);

        addKeyListener(this);
        //addWindowListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        // TODO Auto-generated method stub
        // Server Socket
        try {
            serverSoc = new ServerSocket(portAdd);
            System.out.println("Server has started to running on the " + portAdd + " port.\nWaiting for a player...");
            System.out.println("Waiting for connection...");
            playerS.setImessage("Waiting f   r a player...");
            clientSoc = serverSoc.accept();

            System.out.println("Connected a player...");

            if (clientSoc.isConnected()) { //If connected a player start to loop 

                boolean notchecked = true; //Client isChecked?
                movB = new Thread(movingBALL);
                while (true) {

                    //Checking game situation
                    if (playerS.getScoreP() >= max_Score || playerS.getScoreS() >= max_Score && Restart == false) {

                        if (playerS.getScoreS() > playerS.getScoreP()) {
                            playerS.setOmessage("Won               Loss-Play Again: Press any key || Exit: Esc|N");
                            playerS.setImessage("Won               Loss-Play again? ");
                            Restart = true;
                        } else {
                            playerS.setImessage("Loss              Won-Play Again: Press any key || Exit: Esc|N");
                            playerS.setOmessage("Loss              Won-Play Again: Press any key || Exit: Esc|N");
                            Restart = true;
                        }
                        movB.suspend();	//Stop the ball object
                    }

                    //is client ready...
                    if (playerC.ok && notchecked) {
                        playerS.setImessage("");
                        movB.start();
                        notchecked = false;
                    }

                    updateBall();

                    //Creating Streams
                    ObjectInputStream getObj = new ObjectInputStream(clientSoc.getInputStream());
                    playerC = (PlayerClient) getObj.readObject();
                    getObj = null;

                    //Send Object to Client
                    ObjectOutputStream sendObj = new ObjectOutputStream(clientSoc.getOutputStream());
                    sendObj.writeObject(playerS);
                    sendObj = null;

                    //Check Restart Game
                    if (restartON) {

                        if (playerC.restart) {
                            playerS.setScoreP(0);
                            playerS.setScoreS(0);
                            playerS.setOmessage("");
                            playerS.setImessage("");
                            Restart = false;
                            playerS.setRestart(false);
                            playerS.setBallx(380);
                            playerS.setBally(230);
                            movingBALL.setX(380);
                            movingBALL.setY(230);
                            movB.resume();
                            restartON = false;
                        }
                    }
                    repaint();
                }
            } else {
                System.out.println("Disconnected...");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Image createImage() {

        //BufferedImage Keep the Screen Frames
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage.createGraphics();

        //Table
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        //Lines
        g.setColor(Color.white);
        g.fillRect(WIDTH / 2 - 5, 0, 5, HEIGHT);
        g.fillRect(WIDTH / 2 + 5, 0, 5, HEIGHT);

        //Score
        g.setFont(sFont);
        g.setColor(new Color(228, 38, 36));
        g.drawString("" + playerS.getScoreS(), WIDTH / 2 - 60, 120);
        g.drawString("" + playerS.getScoreP(), WIDTH / 2 + 15, 120);

        //Player Names
        g.setFont(nFont);
        g.setColor(Color.white);
        g.drawString(playerS.getName(), WIDTH / 10, HEIGHT - 20);
        g.drawString(playerC.getName(), 600, HEIGHT - 20);

        //Players
        g.setColor(new Color(255, 0, 0));
        g.fillRect(playerS.getX(), playerS.getY(), barWidth, barHeight);
        g.setColor(new Color(0, 0, 255));
        g.fillRect(playerC.getX(), playerC.getY(), barWidth, barHeight);

        //Ball
        g.setColor(new Color(255, 255, 255));
        g.fillOval(playerS.getBallx(), playerS.getBally(), 45, 45);
       // g.setColor(new Color(228, 38, 36));
       // g.fillOval(playerS.getBallx() + 5, playerS.getBally() + 5, 45 - 10, 45 - 10);

        //Message
        message = playerS.getImessage().split("-");
        g.setFont(mFont);
        g.setColor(Color.white);
        if (message.length != 0) {
            g.drawString(message[0], WIDTH / 4 - 31, HEIGHT / 2 + 38);
            if (message.length > 1) {
                if (message[1].length() > 6) {
                    g.setFont(rFont);
                    g.setColor(new Color(228, 38, 36));
                    g.drawString(message[1], WIDTH / 4 - 31, HEIGHT / 2 + 100);
                }
            }
        }
        return bufferedImage;
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(createImage(), 0, 0, this);
    }

    public void updateBall() {

        //collisions
        checkCol();

        // - update the ball - //
        playerS.setBallx(movingBALL.getX());
        playerS.setBally(movingBALL.getY());

    }

    //Update Player
    public void playerUP() {
        if (playerS.getY() - barSpeed > barHeight / 2 - 10) {

            playerS.setY(playerS.getY() - barSpeed);
        }
    }

    public void playerDOWN() {
        if (playerS.getY() + barSpeed < HEIGHT - barHeight - 30) {

            playerS.setY(playerS.getY() + barSpeed);
        }
    }

    //Check Collision 
    public void checkCol() {

        //Checking ball side, when a player got a score check -> false * if ball behind of the players check -> true
        if (playerS.getBallx() < playerC.getX() && playerS.getBallx() > playerS.getX()) {
            check = true;
        }

        //Server Player Score 
        if (playerS.getBallx() > playerC.getX() && check) {

            playerS.setScoreS(playerS.getScoreS() + 1);

            check = false;
        } //Client Player Score
        else if (playerS.getBallx() <= playerS.getX() && check) {

            playerS.setScoreP(playerS.getScoreP() + 1);

            check = false;

        }

        //Checking Server Player Bar
        if (movingBALL.getX() <= playerS.getX() + barWidth && movingBALL.getY() + movingBALL.getRadius() >= playerS.getY() && movingBALL.getY() <= playerS.getY() + barHeight) {
            movingBALL.setX(playerS.getX() + barWidth);
            playerS.setBallx(playerS.getX() + barWidth);
            movingBALL.setXv(movingBALL.getXv() * -1);
        }

        //Checking Client Player Bar
        if (movingBALL.getX() + movingBALL.getRadius() >= playerC.getX() && movingBALL.getY() + movingBALL.getRadius() >= playerC.getY() && movingBALL.getY() <= playerC.getY() + barHeight) {
            movingBALL.setX(playerC.getX() - movingBALL.getRadius());
            playerS.setBallx(playerC.getX() - movingBALL.getRadius());
            movingBALL.setXv(movingBALL.getXv() * -1);
        }

    }

    @Override
    public void keyPressed(KeyEvent arg0) {

        // TODO Auto-generated method stub
        int keycode = arg0.getKeyCode();
        if (keycode == KeyEvent.VK_UP) {
            playerUP();
            repaint();
        }
        if (keycode == KeyEvent.VK_DOWN) {
            playerDOWN();
            repaint();
        }
        if (Restart == true) {
            restartON = true;
            playerS.setRestart(true);
        }

        if (keycode == KeyEvent.VK_N || keycode == KeyEvent.VK_ESCAPE && Restart == true) {
            try {
                this.setVisible(false);
                serverSoc.close();
                System.exit(EXIT_ON_CLOSE);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void windowClosing(WindowEvent arg0) {
        // TODO Auto-generated method stub

        Thread.currentThread().stop();
        this.setVisible(false);
        try {
            serverSoc.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(1);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        // TODO Auto-generated method stub
        System.exit(1);
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowIconified(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowOpened(WindowEvent arg0) {
        // TODO Auto-generated method stub

    }

}
