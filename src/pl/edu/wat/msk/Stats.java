package pl.edu.wat.msk;

import pl.edu.wat.msk.Gui.GuiFederate;
import pl.edu.wat.msk.Statystyka.StatystykaFederate;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Pawel on 2017-06-25.
 */
public class Stats {

    public static StatystykaFederate statystykaFederate;
    private JFrame frame;

    private JLabel liczbaKlientowLabel;
    private JLabel liczbaObsluzonychLabel;
    private JLabel przepustowoscLabel;

    private JLabel liczbaKlientowText;
    private JLabel liczbaObsluzonychText;
    private JLabel przepustowoscText;

    public Stats() {
        init();
    }

    public Stats (StatystykaFederate federate){
        this.statystykaFederate = federate;
        init();
    }


    public void setStats(int liczbaKlientow, int liczbaObsluzonych){
        liczbaKlientowText.setText(String.valueOf(liczbaKlientow));
        liczbaObsluzonychText.setText(String.valueOf(liczbaObsluzonych));
        przepustowoscText.setText(String.valueOf((float)((float)liczbaObsluzonych/liczbaKlientow)*100) + "%");
    }

    private void init() {
        frame = new JFrame();
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        frame.setSize(300, 300);
        frame.setTitle("Stats");

        liczbaKlientowLabel = new JLabel();
        liczbaKlientowLabel.setBounds(25,50,200,20);
        liczbaKlientowLabel.setText("Liczba klientów");

        liczbaObsluzonychLabel = new JLabel();
        liczbaObsluzonychLabel.setBounds(25,100,200,20);
        liczbaObsluzonychLabel.setText("Liczba obsłużonych");

        przepustowoscLabel = new JLabel();
        przepustowoscLabel.setBounds(25,150,200,20);
        przepustowoscLabel.setText("Przepustowość");


        liczbaKlientowText = new JLabel();
        liczbaKlientowText.setBounds(200,50,200,20);
        liczbaObsluzonychText = new JLabel();
        liczbaObsluzonychText.setBounds(200,100,200,20);
        przepustowoscText = new JLabel();
        przepustowoscText.setBounds(200,150,200,20);


        frame.add(liczbaKlientowLabel);
        frame.add(liczbaObsluzonychLabel);
        frame.add(przepustowoscLabel);

        frame.add(liczbaKlientowText);
        frame.add(liczbaObsluzonychText);
        frame.add(przepustowoscText);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void run(StatystykaFederate federat) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    federat.advanceTime(1.0);

                    Stats stats = new Stats(federat);
                    stats.frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
