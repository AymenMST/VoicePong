package pong;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import audioCall.*;
import javax.swing.JFrame;

/**
 * @author User
 */
public class FunPongClient extends JFrame implements KeyListener, Runnable, WindowListener {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "client";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 460;
    boolean isRunning = false;

    //Players
    private PlayerServer playerS;
    private PlayerClient playerC;
    private int barWidth = 30;
    private int barHeight = 80;
    private int barSpeed = 15;

    //Server
    private static Socket clientSoc;
    private int portAdd;
    private String ipAdd;
    private boolean reset = false;
    private int countS = 0;

    //Graphical
    private Graphics g;
    private Font sFont = new Font("Comic sans ms", Font.ITALIC, 72);
    private Font mFont = new Font("Comic sans ms", Font.ITALIC, 48);
    private Font nFont = new Font("Comic sans ms", Font.ITALIC, 28);
    private Font rFont = new Font("Comic sans ms", Font.ITALIC, 16);
    private String[] message;	// - Split Message to two piece in an array - //

    public FunPongClient(String clientname, String portAdd, String ipAdd) {

        //Players
        playerS = new PlayerServer();
        playerC = new PlayerClient(clientname);
        playerS.setName(clientname);

        //Socket
        this.ipAdd = ipAdd;
        this.portAdd = Integer.parseInt(portAdd);
        this.isRunning = true;

        //Frame
        this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        addKeyListener(this);
    }

    @Override
    public void run() {
        // Server Socket
        try {
            System.out.println("Finding server...\nConnecting to " + ipAdd + ":" + portAdd);
            clientSoc = new Socket(ipAdd, portAdd);
            System.out.println("Connected to server...");

            if (clientSoc.isConnected()) {
                System.out.println("TEST");
                //define get send objects

                while (true) {
                    //Creating Streams
                    ObjectOutputStream sendObj = new ObjectOutputStream(clientSoc.getOutputStream());
                    sendObj.writeObject(playerC);
                    sendObj = null;

                    ObjectInputStream getObj = new ObjectInputStream(clientSoc.getInputStream());
                    playerS = (PlayerServer) getObj.readObject();
                    getObj = null;

                    // reset restart status
                    if (reset) {

                        if (countS > 3) {
                            playerC.restart = false;
                            reset = false;
                            countS = 0;
                        }
                    }
                    countS++;
                    repaint();
                }

            } else {
                System.out.println("Disconnected...");
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    //Paint 
    private Image createImage() {

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
        g.setColor(new Color(255, 255, 255));
        g.setFont(sFont);
        g.drawString("" + playerS.getScoreS(), WIDTH / 2 - 60, 120);
        g.drawString("" + playerS.getScoreP(), WIDTH / 2 + 15, 120);

        //Player Names
        g.setFont(nFont);
        g.setColor(Color.white);
        g.drawString(playerS.getName(), WIDTH / 10, HEIGHT - 20);
        g.drawString(playerC.getName(), 600, HEIGHT - 20);

        //Player Bar
        g.setColor(new Color(255, 0, 0));
        g.fillRect(playerS.getX(), playerS.getY(), barWidth, barHeight);
        g.setColor(new Color(0, 0, 255));
        g.fillRect(playerC.getX(), playerC.getY(), barWidth, barHeight);

        //Ball
        g.setColor(new Color(255, 255, 255));
        g.fillOval(playerS.getBallx(), playerS.getBally(), 45, 45);
      //  g.setColor(new Color(228, 38, 36));
      //  g.fillOval(playerS.getBallx() + 5, playerS.getBally() + 5, 45 - 10, 45 - 10);

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

    public void paint(Graphics g) {
        g.drawImage(createImage(), 0, 0, this);
        playerC.ok = true;
    }

    //Update Player
    public void playerUP() {
        if (playerC.getY() - barSpeed > barHeight / 2 - 10) {

            playerC.setY(playerC.getY() - barSpeed);
        }
    }

    public void playerDOWN() {
        if (playerC.getY() + barSpeed < HEIGHT - barHeight - 30) {

            playerC.setY(playerC.getY() + barSpeed);
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
        if (playerS.isRestart()) {
            playerC.restart = true;
            reset = true;
        }
        if (keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_N && playerS.isRestart()) {
            try {
                this.setVisible(false);
                clientSoc.close();
                System.exit(EXIT_ON_CLOSE);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
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

    }

    @SuppressWarnings("deprecation")
    @Override
    public void windowClosing(WindowEvent arg0) {
        // TODO Auto-generated method stub

        Thread.currentThread().stop();
        this.setVisible(false);
        try {
            clientSoc.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
