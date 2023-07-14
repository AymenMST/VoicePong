package pong;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import audioCall.Discussion;
import audioCall.DiscussionServer;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * @author User
 */
public class FunPong extends JFrame implements KeyListener, Runnable {

    private static final long serialVersionUID = 1L;

    private static Image image;
    private Graphics g;
    private static final String TITLE = "Fun pong";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 640;
    private String servername = "servername", clientname = "clientname";

    public FunPong() {
    }

    @Override
    public void run() {

        this.setVisible(true);
        this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);
        this.addKeyListener(this);
    }

    private Image createImage() {

        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage.createGraphics();
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.drawImage(image, 0, 0, this);
        return bufferedImage;

    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(createImage(), 0, 20, this);
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        // TODO Auto-generated method stub

        int keyCode = ev.getKeyCode();
        String portAdd = null;
        String ipAdd = null;

        //Create a Server
        if (keyCode == KeyEvent.VK_S) {
          Thread discussionServer = new Thread(new DiscussionServer());
          discussionServer.start();
            //Input Dialog for get a port address
            portAdd = JOptionPane.showInputDialog(null, "ex. 1024", "Enter server port:", 1);

            //Alert Message
            if (portAdd != null) {
                if (!isPort(portAdd)) {
                    JOptionPane.showMessageDialog(null, "Enter port number as a right format!", "Error!", 1);
                } else {

                    //Input Dialog for get a nick name for server player
                    servername = JOptionPane.showInputDialog(null, "Nick name:", "Enter server name:", 1);
                    servername += "";

                    //Alert Message
                    if (servername.length() > 10 || servername.length() < 3 || servername.startsWith("null")) {
                        JOptionPane.showMessageDialog(null, "Enter name as a right format!", "Error!", 1);

                    } else {

                        FunPongServer myServer = new FunPongServer(servername, portAdd);
                        Thread myServerT = new Thread(myServer);
                        myServerT.start();
                        this.setVisible(false);
                    }
                }
            }
        }

        //Create a Client
        if (keyCode == KeyEvent.VK_C) {
            Thread discussion = new Thread(new Discussion());
            discussion.start();
            //Input Dialog IP @
            ipAdd = JOptionPane.showInputDialog(null, "ex. 127.0.0.1", "Enter server ip:", 1);

            if (ipAdd != null) {

                //Alert Message
                if (!isIPAddress(ipAdd)) {
                    JOptionPane.showMessageDialog(null, "Enter ip number as a right format!", "Enter server ip:", 1);
                } else {
                    //Input Dialog Port Number
                    portAdd = JOptionPane.showInputDialog(null, "ex. 1024", "Enter server port number:", 1);

                    //Alert Message
                    if (portAdd != null) {
                        if (!isPort(portAdd)) {
                            JOptionPane.showMessageDialog(null, "Enter port number as a right format!", "Error!:", 1);
                        } //Input Dialog for get a nick name for client player 
                        else {
                            clientname = JOptionPane.showInputDialog(null, "Nick name:", "Enter server name:", 1);
                            clientname += "";
                            if (clientname.length() > 10 || clientname.length() < 3 || clientname.startsWith("null")) {
                                JOptionPane.showMessageDialog(null, "Enter name as a right format!", "Error!", 1);
                            } //Start a Client
                            else {
                                FunPongClient myClient = new FunPongClient(clientname, portAdd, ipAdd);
                                Thread myClientT = new Thread(myClient);
                                myClientT.start();
                                this.setVisible(false);
                            }
                        }
                    }
                }
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

    //Check PORT number type
    private boolean isPort(String str) {
        Pattern pPattern = Pattern.compile("\\d{1,4}");
        return pPattern.matcher(str).matches();
    }
    //Check IP address type

    private boolean isIPAddress(String str) {
        Pattern ipPattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
        return ipPattern.matcher(str).matches();
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FunPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        SwingUtilities.invokeLater(() -> {
            Toolkit tk = Toolkit.getDefaultToolkit();
            image = tk.getImage("Background.jpg");
            FunPong newT = new FunPong();
            newT.run();
        });

    }

}
