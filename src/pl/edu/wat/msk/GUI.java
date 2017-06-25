package pl.edu.wat.msk;

import hla.rti.RTIexception;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Pawel on 2017-06-25.
 */
public class GUI {

        private JFrame frame;

        private JLabel czasObslugiLabel;
        private JLabel liczbaKlientowLabel;
        private JLabel okresCzasuNaplywuLabel;
        private JLabel liczbaOkienekLabel;

        private JTextField czasObslugiText;
        private JTextField liczbaKlientowText;
        private JTextField okresCzasuNaplywuText;
        private JTextField liczbaOkienekText;

        private JButton startSymulacjiButton;
        private JButton stopSymulacjiButton;

        private float czasObslugi;
        private int liczbaNaplywajacychKlientow;
        private float okresCzasuNaplywu;
        private int liczbaOkienek;
        private boolean koniecSymulacji;


        public GUI (){
            init();
        }

        private void init(){
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

            okresCzasuNaplywuText = new JTextField();
            okresCzasuNaplywuText.setBounds(250,150,100,20);

            liczbaOkienekText = new JTextField();
            liczbaOkienekText.setBounds(250,200,100,20);


            //Buttons
            startSymulacjiButton = new JButton();
            startSymulacjiButton.setText("Start");
            startSymulacjiButton.setBounds(150, 250, 100, 30 );

            stopSymulacjiButton = new JButton();
            stopSymulacjiButton.setText("Stop");
            stopSymulacjiButton.setBounds(150,300,100,30);

            startSymulacjiButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    czasObslugi = (float) Float.valueOf(czasObslugiText.getText().toString());
                    //System.out.println(czasObslugi);
                    liczbaNaplywajacychKlientow = Integer.valueOf(liczbaKlientowText.getText().toString());
                    //System.out.println(liczbaNaplywajacychKlientow);
                    okresCzasuNaplywu = Float.valueOf(okresCzasuNaplywuText.getText().toString());
                    //System.out.println(okresCzasuNaplywu);
                    liczbaOkienek = Integer.valueOf(liczbaOkienekText.getText().toString());
                    //System.out.println(liczbaOkienek);

                }
            });

            stopSymulacjiButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    koniecSymulacji = true;
                }
            });


            frame.add(czasObslugiLabel);
            frame.add(liczbaKlientowLabel);
            frame.add(okresCzasuNaplywuLabel);
            frame.add(liczbaOkienekLabel);

            frame.add(czasObslugiText);
            frame.add(liczbaKlientowText);
            frame.add(okresCzasuNaplywuText);
            frame.add(liczbaOkienekText);

            frame.add(startSymulacjiButton);
            frame.add(stopSymulacjiButton);

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            frame.setVisible(true);

        }

        public static void main(String args[]){
            GUI gui = new GUI();
        }
}

