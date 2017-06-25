package pl.edu.wat.msk;

import javax.swing.*;

/**
 * Created by Pawel on 2017-06-25.
 */
public class Stats {

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

    public static void main(String args[]) {
        Stats stat = new Stats();
        stat.setStats(100,20);
    }
}
