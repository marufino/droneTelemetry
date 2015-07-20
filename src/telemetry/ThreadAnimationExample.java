package telemetry;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class ThreadAnimationExample extends JFrame {

    public ThreadAnimationExample() throws IOException {

        initUI();
    }
    
    private void initUI() throws IOException {
        
        add(new Board());

        setResizable(false);
        pack();
        
        setTitle("Drone Telemetry");    
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {                
                JFrame ex = null;
                try {
                    ex = new ThreadAnimationExample();
                } catch (IOException ex1) {
                    Logger.getLogger(ThreadAnimationExample.class.getName()).log(Level.SEVERE, null, ex1);
                }
                ex.setVisible(true);                
            }
        });
    }
}