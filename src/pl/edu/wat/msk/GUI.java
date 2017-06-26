package pl.edu.wat.msk;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import hla.rti.RTIexception;
import pl.edu.wat.msk.Gui.GuiFederate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;


public class GUI {
        //gowno
        public static boolean start = false;

        public static GuiFederate guiFederate;
        private JFrame frame;

        private JLabel czasObslugiLabel;
        private JLabel liczbaKlientowLabel;
        private JLabel okresCzasuNaplywuLabel;
        private JLabel liczbaOkienekLabel;
        private JLabel czasTrwaniaSymulacjiL;

        public static JTextField czasObslugiText;
        public static JTextField liczbaKlientowText;
        public static JTextField okresCzasuNaplywuText;
        public static JTextField liczbaOkienekText;
        public static JTextField czasTrwaniaSymulacji;

        private JButton startSymulacjiButton;
        private JButton stopSymulacjiButton;

        private float czasObslugi;
        private int liczbaNaplywajacychKlientow;
        private float okresCzasuNaplywu;
        private int liczbaOkienek;
        private boolean koniecSymulacji;


        public GUI (GuiFederate federate) throws Exception{
            this.guiFederate = federate;
            init();
        }

        private void init() throws Exception{
            koniecSymulacji = false;
            frame = new JFrame();
            frame.setTitle("GUI");
            frame.setResizable(false);

            frame.setSize(400,400);
            frame.getContentPane().setLayout(null);


            //Labels
            czasObslugiLabel = new JLabel();
            czasObslugiLabel.setBounds(25,50,200,20);
            czasObslugiLabel.setText("Czas obsługi");

            //Labels
            czasTrwaniaSymulacjiL = new JLabel();
            czasTrwaniaSymulacjiL.setBounds(25,250,200,20);
            czasTrwaniaSymulacjiL.setText("Czas trwania symulacji");

            liczbaKlientowLabel = new JLabel();
            liczbaKlientowLabel.setBounds(25,100,200,20);
            liczbaKlientowLabel.setText("Liczba napływających klientów");

            okresCzasuNaplywuLabel = new JLabel();
            okresCzasuNaplywuLabel.setBounds(25,150,200,20);
            okresCzasuNaplywuLabel.setText("Okres czasu napływu");

            liczbaOkienekLabel = new JLabel();
            liczbaOkienekLabel.setBounds(25,200,200,20);
            liczbaOkienekLabel.setText("Liczba okienek obsługi");

            //TextFields
            czasObslugiText = new JTextField();
            czasObslugiText.setBounds(250,50,100,20);

            liczbaKlientowText = new JTextField();
            liczbaKlientowText.setBounds(250,100,100,20);

            czasTrwaniaSymulacji = new JTextField();
            czasTrwaniaSymulacji.setBounds(250,250,100,20);

            okresCzasuNaplywuText = new JTextField();
            okresCzasuNaplywuText.setBounds(250,150,100,20);

            liczbaOkienekText = new JTextField();
            liczbaOkienekText.setBounds(250,200,100,20);


            stopSymulacjiButton = new JButton();
            stopSymulacjiButton.setText("Stop");
            stopSymulacjiButton.setBounds(150,300,100,30);

            stopSymulacjiButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        GuiFederate.zakonczSymulacje = true;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });


            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                frame.dispose();
                guiFederate.running=false;
                guiFederate.endSim();
                }
            });

            frame.add(czasObslugiLabel);
            frame.add(liczbaKlientowLabel);
            frame.add(okresCzasuNaplywuLabel);
            frame.add(liczbaOkienekLabel);
            frame.add(czasTrwaniaSymulacjiL);

            frame.add(czasObslugiText);
            frame.add(liczbaKlientowText);
            frame.add(okresCzasuNaplywuText);
            frame.add(liczbaOkienekText);
            frame.add(czasTrwaniaSymulacji);

            frame.add(stopSymulacjiButton);

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            frame.setVisible(true);

        }

    public static void run(GuiFederate federat) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI gui = new GUI(federat);
                    gui.setInitialData(2,3,5,2);
                    gui.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setInitialData(int czasObslugi, int liczbaNaplywajacychKlientow, int okresCzasuNaplywu, int liczbaOkienek){
            czasObslugiText.setText(String.valueOf(czasObslugi));
            liczbaKlientowText.setText(String.valueOf(liczbaNaplywajacychKlientow));
            okresCzasuNaplywuText.setText(String.valueOf(okresCzasuNaplywu));
            liczbaOkienekText.setText(String.valueOf(liczbaOkienek));

    }

}

