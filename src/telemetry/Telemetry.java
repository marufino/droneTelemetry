/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;

/**
 *
 * @author Miguel
 */
public class Telemetry {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        int portNumber = 520;
        String inputLine;
        String[] lidarDat;
        
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = serverSocket.accept();
        //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
        
        System.out.println("test");
        
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            lidarDat = inputLine.split(",");
        }
        
        
    
    }
    
}
