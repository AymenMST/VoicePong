/*6
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioCall;

/**
 *
 * @author User
 */
import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class Discussion implements Runnable{

    boolean stopaudioCapture = false;
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream InputStream;
    SourceDataLine sourceLine;
    String host = "192.168.43.247";

    public Discussion() {

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    @Override
    public void run() {
                try {
            DatagramSocket serverSocket = new DatagramSocket(5000);
            byte[] receiveData = new byte[10000];
            adFormat = getAudioFormat();
            DataLine.Info dataLineInf = new DataLine.Info(TargetDataLine.class, adFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInf);
            targetDataLine.open(adFormat);
            targetDataLine.start();

            Thread micThread = new Thread(new StartThread());
            micThread.start();
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
                try {
                    byte audioData[] = receivePacket.getData();
                    InputStream byteInputStream = new ByteArrayInputStream(audioData);
                    adFormat = getAudioFormat();
                    InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
                    sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceLine.open(adFormat);
                    sourceLine.start();
                    Thread playThread = new Thread(new PlayThread());
                    playThread.start();
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            StackTraceElement stackEle[] = e.getStackTrace();
            for (StackTraceElement val : stackEle) {
                System.out.println(val);
            }

        }
        
    }

    class StartThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {

            byteOutputStream = new ByteArrayOutputStream();
            stopaudioCapture = false;
            try {
                DatagramSocket clientSocket = new DatagramSocket(5500);
                InetAddress IPAddress = InetAddress.getByName(host);
                while (!stopaudioCapture) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 5600);
                        clientSocket.send(sendPacket);
                        byteOutputStream.write(tempBuffer, 0, cnt);
                    }
                }
                byteOutputStream.close();
            } catch (Exception e) {
                System.out.println("DiscussionThread::run()" + e);
                System.exit(0);
            }
        }
    }

    class PlayThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {
            try {
                int cnt;
                while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceLine.write(tempBuffer, 0, cnt);
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            } finally {
                //  sourceLine.drain();
                //  sourceLine.close();
            }
        }
    }

    public static void main(String args[]) {

        new Discussion().run();
    }
}
