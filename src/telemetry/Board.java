/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package telemetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class Board extends JPanel
        implements Runnable {

    private final int B_WIDTH = 1024;
    private final int B_HEIGHT = 768;
    private final int DELAY = 25;

    private final int LOWESTPWM = -100;
    private final int HIGHESTPWM = 100;
    
    private Thread animator;

    private String inputLine;
    private String[] lidarDat;
    private int[] distances = new int[360];
    private float[] rcSignals = new float[4];
    private int[] corrections = new int[4];
    private float[] rcCorrected = new float[4];
    private String[] signalNames = {"Aileron", "Elevator","Throttle","Rudder"};
    
    private BufferedReader in;
    
    public Board() throws IOException {

        initBoard();
    }

    private void initBoard() throws IOException {

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        setDoubleBuffered(true);
        
        int portNumber = 520;
        
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = serverSocket.accept();
        //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
        
        System.out.println("Connected");
        
    }

    @Override
    public void addNotify() {
        super.addNotify();

        animator = new Thread(this);
        animator.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawStuff(g);
    }
    
    private void drawStuff(Graphics g) {
        
        // draw lidar map (g, data, xloc, yloc)
        drawMap(g,distances,B_WIDTH/2,B_HEIGHT/2,270);

        // draw rc gauges (g, data, xloc, yloc, 1/scale)
        drawGauge(g,rcSignals,200,500,2,Color.ORANGE);
        float [] leftGauge = {-rcSignals[3],-rcSignals[2]};
        drawGauge(g,leftGauge,100,500,2,Color.ORANGE);
        drawGauge(g,rcCorrected,200,500,2,Color.GREEN);
        
        
        
        
        for (int i = 3; i >= 0 ; i--) {
            g.drawString(signalNames[i], (25 + 50*i), 25);
            g.drawString(String.valueOf(rcSignals[i]), (25 + 50*i), 50);
        }
        
        
        for (int j = 3; j >= 0 ; j--) {
            g.setColor(Color.RED);
            g.drawString(String.valueOf(corrections[j]), (25 + 50*j), 75);
        }
        
        Toolkit.getDefaultToolkit().sync();
    }

    private void cycle() throws IOException {

        //get latest data
        inputLine = in.readLine();
        //System.out.println(inputLine);
        lidarDat = inputLine.split(",");
        
        // store lidar data
        for (int count = 0; count < 360 ; count++) {
            distances[count] = Integer.parseInt(lidarDat[count]);
        }   

        // store rc signals
        for (int count = 0; count < 4 ; count++) {
            rcSignals[count] = (int)Float.parseFloat(lidarDat[360+count]);
            if (rcSignals[count] > HIGHESTPWM)
                rcSignals[count] = HIGHESTPWM;
            
            if (rcSignals[count] < LOWESTPWM)
                rcSignals[count] = LOWESTPWM;
            
        }
        
        //store corrections
        for (int count = 0; count < 4 ; count++) {
            corrections[count] = Integer.parseInt(lidarDat[364+count]);
        }
        
        //store corrected rc signals
        for (int count = 0; count < 2 ; count++) {
            if(count==0){
                rcCorrected[count] = rcSignals[count] + corrections[1] - corrections[3];
            }
            
            if(count==1)
                rcCorrected[count] = rcSignals[count] + corrections[0] - corrections[2];
            
            if (rcCorrected[count] > HIGHESTPWM)
                rcCorrected[count] = HIGHESTPWM;
            if (rcCorrected[count] < LOWESTPWM)
                rcCorrected[count] = LOWESTPWM;
        }

    }

    @Override
    public void run() {

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {

            try {
                cycle();
            } catch (IOException ex) {
                Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
            }
            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;

            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("Interrupted: " + e.getMessage());
            }

            beforeTime = System.currentTimeMillis();
        }
    }
    
    private void drawGauge(Graphics g, float[] signals, int xLoc, int yLoc, int scale, Color color){
        int rs = 100/scale;
        int rl = 200/scale;
       
        // background
        g.setColor(Color.WHITE);
        g.drawOval(xLoc-(rs)/2,yLoc-(rs)/2, rs,rs);
        g.drawOval(xLoc-(rl)/2,yLoc-(rl)/2, rl,rl);
        g.drawRect(xLoc-100/scale,yLoc-100/scale,200/scale,200/scale);
        
        // stick
        g.setColor(color);
        g.drawLine(xLoc, yLoc, xLoc+((int)signals[0])/scale, yLoc+((int)signals[1])/scale);
    }
    
    
    private void drawMap(Graphics g, int[] distances, int xOffset, int yOffset, int angleOffset){
        /* DRAW MAP */
        int x,y;
        g.setColor(Color.GREEN);
        for (int count = 0; count < 360 ; count++) {
            x = (int) -(distances[count]*Math.cos(Math.toRadians(count+angleOffset)))/10 + xOffset;
            y = (int) (distances[count]*Math.sin(Math.toRadians(count+angleOffset)))/10 + yOffset;
            g.drawLine(x,y,x,y);
        }

        //draw self
        g.setColor(Color.RED);
        g.drawOval(xOffset,yOffset,10,10);
    }
}
