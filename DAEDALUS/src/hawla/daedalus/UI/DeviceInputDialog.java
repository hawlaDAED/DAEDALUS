/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.UI;

import charva.awt.Color;
import charva.awt.FlowLayout;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charvax.swing.BoxLayout;
import charvax.swing.JButton;
import charvax.swing.JDialog;
import charvax.swing.JFrame;
import charvax.swing.JLabel;
import charvax.swing.JPanel;
import charvax.swing.JTextField;
import charvax.swing.border.LineBorder;

/**
 *
 * GUI-Dialog-Klasse, um neu einzufügende Geräte in für die Datenhaltung strukturierter Form zu erhalten.
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class DeviceInputDialog extends JDialog implements ActionListener
{

    enum Option
    {

        Connect, Cancel;
    };

    private final JTextField devIDInputField;
    private final JTextField devNameInputField;
    private final JTextField devIPInputField;
    private Option recentChoice = null;

    /**
     * Konstruktor des Dialogs, der dafür vorgesehen ist, neue Geräteverbindungen zu verwirklichen.
     *
     * @param parent Das übergeordnete Fenster des Verbindungsdialogs.
     */
    public DeviceInputDialog(JFrame parent)
    {
        super(parent, "Neue Verbindung");

        // Textfeld für Geräte-ID-Eingabe (max. 2 byte, d. h. Werte zw. 0 und 65.535)
        devIDInputField = new JTextField("", 5);

        // Textfeld für die Geräte-Nameneingabe (maximal 15 alphanumerische Zeichen)
        devNameInputField = new JTextField("", 15);

        // Textfeld für die Geräte-IP (IP-Format nach RFC 791)
        devIPInputField = new JTextField("", 16);

        // Angaben zur Eingabe mit Eingabefelder zusammenstellen und zusammenfügen
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel devIDInputPan = new JPanel(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        devIDInputPan.setBorder(new LineBorder(Color.green));
        devIDInputPan.add(new JLabel(""));
        devIDInputPan.add(new JLabel("Geben Sie hier die Geräte-ID ein."));
        devIDInputPan.add(new JLabel("(min. 0 und max. 65535)"));
        devIDInputPan.add(new JLabel(""));
        devIDInputPan.add(devIDInputField);
        this.getContentPane().add(devIDInputPan);

        JPanel devNameInputPan = new JPanel(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        devNameInputPan.setBorder(new LineBorder(Color.green));
        devNameInputPan.add(new JLabel(""));
        devNameInputPan.add(new JLabel("Geben Sie hier den Gerätenamen ein."));
        devNameInputPan.add(new JLabel("(min. 1 und max. 15 sichtbare Zeichen)"));
        devNameInputPan.add(new JLabel(""));
        devNameInputPan.add(devNameInputField);
        this.getContentPane().add(devNameInputPan);

        JPanel devIPInputPan = new JPanel(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        devIPInputPan.setBorder(new LineBorder(Color.green));
        devIPInputPan.add(new JLabel(""));
        devIPInputPan.add(new JLabel("Geben Sie hier die IPv4-Adresse ein."));
        devIPInputPan.add(new JLabel("(IP-Format nach RFC 791)"));
        devIPInputPan.add(new JLabel(""));
        devIPInputPan.add(devIPInputField);
        devIPInputPan.add(new JLabel(""));
        this.getContentPane().add(devIPInputPan);

        // Entscheidungsbuttons für die Steuerung des Dialogs
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae_)
            {
                recentChoice = DeviceInputDialog.Option.Connect;
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae_)
            {
                recentChoice = DeviceInputDialog.Option.Cancel;
                devNameInputField.setText("");
                devIPInputField.setText("");
                setVisible(false);
            }
        });
        JPanel butttonpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 5));
        butttonpanel.add(connectButton);
        butttonpanel.add(cancelButton);

        this.getContentPane().add(butttonpanel);

        pack();
        validate();

        // Zentrieren des Dialogs in Bezug auf die übergeordnete Komponente
        this.setLocationRelativeTo(parent);

    }

    String fetchInputdevID()
    {
        String inputID = devIDInputField.getText();
        devIDInputField.setText("");
        return inputID;
    }

    /**
     *
     * @return Vom SW-Nutzer eingegebener Gerätename des Gerätes, das neu verbunden werden soll.
     */
    String fetchInputDevName()
    {
        String inputDev = devNameInputField.getText();
        // Zurücksetzen, des vormals Gelesenen, um Missverständnisse zu verhindern
        devNameInputField.setText("");
        return inputDev;
    }

    /**
     *
     * @return Vom Nutzer bestimmte IP des neu hinzuzuverbindenen Gerätes
     */
    String fetchInputDevIP()
    {
        String inputIP = devIPInputField.getText();
// Zurücksetzen, des vormals Gelesenen, um Missverständnisse zu verhindern
        devIPInputField.setText("");
        return inputIP;
    }

    Option getOption()
    {
        Option tmp = recentChoice;
        recentChoice = null;
        return tmp;
    }

    /**
     *
     * @param ae_
     */
    @Override
    public void actionPerformed(ActionEvent ae_)
    {
        String command = ae_.getActionCommand();

        switch (command)
        {
            case "Connect":
                // reading input

                break;
            case "Cancel":
                // simply do nothing

                break;
            default:
                throw new AssertionError();
        }

    }

}
