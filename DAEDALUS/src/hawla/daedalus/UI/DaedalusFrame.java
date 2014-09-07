/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.UI;

import charva.awt.Color;
import charva.awt.Dimension;
import charva.awt.GridBagConstraints;
import charva.awt.GridBagLayout;
import charva.awt.Insets;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charvax.swing.BoxLayout;
import charvax.swing.JButton;
import charvax.swing.JFileChooser;
import charvax.swing.JFrame;
import charvax.swing.JLabel;
import charvax.swing.JMenu;
import charvax.swing.JMenuBar;
import charvax.swing.JMenuItem;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JScrollPane;
import charvax.swing.JTable;
import charvax.swing.ListSelectionModel;
import charvax.swing.border.LineBorder;
import charvax.swing.border.TitledBorder;
import charvax.swing.filechooser.FileFilter;
import hawla.daedalus.configuration_module.DataManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class DaedalusFrame extends JFrame implements ActionListener
{

    Integer MIN_DEV_ID = 0;
    Integer MAX_DEV_ID = 65536;

    // Handle für UI, um kontrolliert das UI zu beenden
    private final UserInterface ui;
    // Zugriff auf Datenmanager
    private final DataManager dataManager;
    // universell (Öffnen und Schließen) eingesetzter FileChooser
    private final JFileChooser fc;
    private final Path currentDir;
    // Filter für Konfigurationsdateien (.daed als File-Extension)
    private final FileFilterImpl fileFilterImpl;
// Referenz auf das genutzte Device-Table-Model, um Tabellen-Datenupdates zur Laufzeit
    // durchführen zu können
    private final DeviceTableModel devData;
// Tabelle, um Datenaktualisierung zur Laufzeit zuzulasse
    private final JTable devices;
    // EIngabe-Dialog für neue Geräte-Verbindungen
    private final DeviceInputDialog deviceInput;
    // Handle auf das Menü des Panels, um es bei der Gerätelöschung deaktivieren zu können
    JMenuBar mainMenuBar;
    // Knöpfe zur Verbindungsabbau-Behandlung
    private final JButton disconnectDevicesButton;
    private final JButton cancelButton;

    /**
     * Konstruktor des Hauptfensters der Software
     *
     * @param title Titel des Hauptfensters von Daedalus
     * @param dm Der DatenManager aus dem Konfigurationsmodul, der sämtliche Daten und den Zugriff darauf bündelt
     */
    public DaedalusFrame(String title, DataManager dm, UserInterface ui)
    {
        super(title);
        this.ui = ui;
        dataManager = dm;
        fc = new JFileChooser();
        fileFilterImpl = new FileFilterImpl();
        devData = new DeviceTableModel(dataManager);

        Path tmpCurrentDir = Paths.get(dataManager.getDaedalusData().getDAED_LOCATION().getPath());
        currentDir = tmpCurrentDir.getParent();

        // Farben des Gesamtfensters der Software festlegen
        setForeground(Color.green);
        setBackground(Color.black);

        // Layout des Gesamtfensters einstellen
        getContentPane().setLayout(new GridBagLayout());

        // Menu aufbauen und ins Hauptfenster einbinden
        mainMenuBar = formMainMenu();
        setJMenuBar(mainMenuBar);

        // Metadaten anlegen und in Hauptfenster einbinden
        GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().add(formMetaPan(constraints), constraints);

        // Gerätetabelle anlegen und ins Hauptfenster eingliedern
        constraints = new GridBagConstraints();
        // Gerätetabelle über separate Erzeugermethode anlegen
        devices = formDeviceTable(constraints);
        TitledBorder tableBorder = new TitledBorder(new LineBorder(Color.green));
        tableBorder.setTitle("Geräte");
        JScrollPane tableScrollPane = new JScrollPane(devices);
        tableScrollPane.setViewportBorder(tableBorder);
        getContentPane().add(tableScrollPane, constraints);

        // formieren und anpassen der Knöpfe zur Verbindungsabbau-Behandlung
        disconnectDevicesButton = new JButton("Disconnect");
        cancelButton = new JButton("Cancel");

        formDisconnectionButtons(new JButton[]
        {
            disconnectDevicesButton, cancelButton
        }, constraints);

        // Formieren des Hauptfensters, nachdem alle Teilkomponenten hinzugefügt wurden
//        setSize(90, 70);
//        setLocation(1, 1);
        validate();
        pack();
        setVisible(true);

        // Anlegen des initialien Eingabedialogs für neue Geräteverbindungen
        deviceInput = new DeviceInputDialog(this);
    }

    /**
     *
     * Zentrale Methode zur Action-Event-Behandlung.
     *
     * @param aE
     */
    @Override
    public void actionPerformed(ActionEvent aE)
    {
        String actionCommand = aE.getActionCommand();

        switch (actionCommand)
        {
            case "Exit":
                ui.endUI();
                break;
            case "Load Configuration":
                processLoadConfig();
                break;
            case "Save Configuration":
                processSaveConfig();
                break;
            case "Connect Device":
                processDeviceConnect();
                break;
            case "Disconnect Device":
                processDeviceDisconnect();
                break;
            default:
                throw new AssertionError();
        }

    }

    private JTable formDeviceTable(GridBagConstraints constraints)
    {

        JTable deviceTable = new JTable(devData);
        deviceTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deviceTable.setColumnSelectionAllowed(false);
        deviceTable.setRowSelectionAllowed(false);
        deviceTable.setPreferredScrollableViewportSize(new Dimension(40, 5));
//       devices.setEnabled(false);
        constraints.ipady = 5;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 1, 1, 1);
        return deviceTable;
    }

    private JPanel formMetaPan(GridBagConstraints constraints)
    {
        JPanel labelPanel = new JPanel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.insets = new Insets(1, 1, 1, 1);
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(new JLabel("SOFTWARE by Philipp Lang, Matthias Tafelmeier,"));
        labelPanel.add(new JLabel("Vanessa Siemon, Lena Schlor, Paul Hofmann"));
        return labelPanel;
    }

    private JMenuBar formMainMenu()
    {
        // das Menu zur Softwaresteuerung aufbauen und zusammenstecken
        JMenuBar mainMenu = new JMenuBar();
        // Abstand zum Programmbeginn schaffen
        mainMenu.setLocation(getLocation().x + 1, getLocation().y + 1);
        // Untermenü zur Konfigurationsverwaltung
        JMenu confMenu = new JMenu("Configuration");
        confMenu.setMnemonic('C');
        JMenuItem loadConf = new JMenuItem("Load Configuration", 'L');
        loadConf.addActionListener(this);
        JMenuItem saveConf = new JMenuItem("Save Configuration", 'S');
        saveConf.addActionListener(this);
        confMenu.add(loadConf);
        confMenu.addSeparator();
        confMenu.add(saveConf);
        // Untermenü zur Geräte-Verwaltung
        JMenu connMenu = new JMenu("Connections");
        connMenu.setMnemonic('o');
        JMenuItem connectDev = new JMenuItem("Connect Device", 'n');
        connectDev.addActionListener(this);
        JMenuItem disconDev = new JMenuItem("Disconnect Device", 'D');
        disconDev.addActionListener(this);
        connMenu.add(connectDev);
        connMenu.addSeparator();
        connMenu.add(disconDev);
        // Untermenü zur Software-Verwaltung
        JMenu daedMenu = new JMenu("DAEDALUS");
        daedMenu.setMnemonic('A');
        JMenuItem exitTrigger = new JMenuItem("Exit", 'x');
        exitTrigger.addActionListener(this);
        daedMenu.add(exitTrigger);
        // Zusammenführen der Teilkomponenten
        mainMenu.add(confMenu);
        mainMenu.add(connMenu);
        mainMenu.add(daedMenu);
        return mainMenu;
    }

    private void formDisconnectionButtons(final JButton[] disconnectionButtons, final GridBagConstraints constraints)
    {

        //        constraints.anchor = GridBagConstraints.SOUTH;
//        constraints.ipady = 1;
//        constraints.ipadx = 1;
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(0, 9, 0, 0);
        //        constraints.anchor = GridBagConstraints.SOUTHEAST;

        getContentPane().add(disconnectDevicesButton, constraints);

        constraints.gridx = 3;
        constraints.insets = new Insets(0, 3, 0, 1);
        getContentPane().add(cancelButton, constraints);

        cancelButton
                .addActionListener(new ActionListener()
                        {

                            @Override
                            public void actionPerformed(ActionEvent ae_)
                            {
                                // verbergen der Knöpfe nach Abbruch der Verbingungsabbaufunktionalität
                                UiUtils.shiftButtonState(disconnectionButtons, false);
                                // Hauptmenü wieder sichtbar und fokussierbar machen
                                UiUtils.shiftMenuBarSate(mainMenuBar, true);
                                devices.setRowSelectionAllowed(false);
                                mainMenuBar.requestFocus();
                            }
                });
        disconnectDevicesButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae_)
            {

                int choice = JOptionPane.showConfirmDialog(getContentPane(), "Wollen Sie die Verbindung zu den ausgewählten Geräten wirklich abbauen?", "Disconnect", JOptionPane.YES_NO_OPTION);

                // Auswertung der Nutzerkonfrontation
                switch (choice)
                {
                    case JOptionPane.YES_OPTION:

                        // die ausgewählten Gerätezeilen ermitteln
                        int[] chosenDeviceRows = devices.getSelectedRows();

                        for (int i = 0; i < chosenDeviceRows.length; i++)
                        {
                            // über die eindeutigen Geräte-ID die zu löschenden Geräte jeweils ausfindig machen
                            Integer deviceName = (Integer) devData.getValueAt(chosenDeviceRows[i], devData.getDEV_ID_COL());
                            // Gerätedaten um das jeweils ausgewählte Gerät kupieren
                            devData.diminishDevices(deviceName);
                        }
                        try
                        {
                            // Absichern der Änderung in der aktuellen Konfigurationsdatei
                            dataManager.saveUserConfig(new File(dataManager.getDaedalusData().getPathToUserConf()));
                        } catch (TransformerException ex)
                        {
                            Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        devices.setRowSelectionInterval(-1, -1);
                        pack();
                        repaint();
                        break;

                    case JOptionPane.NO_OPTION:
                        // Rücksetzen des Selektion auf gar nichts
                        devices.setRowSelectionInterval(-1, -1);
                        pack();
                        repaint();
                        break;
                    default:
                        // unerwarteter Eingabefall bei Dialog -> prinzipiell unmöglich
                        throw new AssertionError();
                }

                // verbergen der Knöpfe nach Abbruch der Verbingungsabbaufunktionalität
                UiUtils.shiftButtonState(disconnectionButtons, false);
                // Hauptmenü wieder sichtbar und fokussierbar machen
                UiUtils.shiftMenuBarSate(mainMenuBar, true);
                devices.setRowSelectionAllowed(false);
                mainMenuBar.requestFocus();
            }
        });

        // Knöpfe sind nur bei Funktionalitätsauswahl aktiv und sichtbar, deshalb werden diese initial
        // deaktiviert
        UiUtils.shiftButtonState(disconnectionButtons, false);
    }

    private void processSaveConfig()
    {
        fc.setCurrentDirectory(currentDir.toFile());
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(fileFilterImpl);
        int option = fc.showSaveDialog(this);

        switch (option)
        {
            case JFileChooser.CANCEL_OPTION:
                // nothing to write home about
                break;
            case JFileChooser.APPROVE_OPTION:
                try
                {
                    // Speichern der aktuellen Konfiguration über das Konfigurations-Modul
                    dataManager.saveUserConfig(fc.getSelectedFile());

                } catch (TransformerException ex)
                {

                    // eher symbolische Exception, aber dennoch -> falls etwas übersehen wurde
                    Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "could not transform the internal data to an external config", ex);
                } catch (Exception ex)
                {
                    // sollte absolut nicht eintreten können
                    Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "saving file via UI failed", ex);
                }

                break;
            case JFileChooser.ERROR_OPTION:
                throw new Error("JFileChooser internal programmatic error has occurred");
            default:
                throw new AssertionError("unforseen switch case has gropped up");
        }
    }

    private void processLoadConfig()
    {
        // initialisiere den FC mit dem aktuellen Verzeichnis des JAR-Archives
        fc.setCurrentDirectory(currentDir.toFile());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(fileFilterImpl);
        int option = fc.showOpenDialog(this);

        switch (option)
        {
            case JFileChooser.CANCEL_OPTION:
                // nothing to write home about
                break;
            case JFileChooser.APPROVE_OPTION:
                try
                {

                    final File userChosenFile = fc.getSelectedFile();

                    // Laden der Konfiguration über das Konfigurations-Modul,f alls diese existiert
                    if (dataManager.loadUserConfig(userChosenFile))
                    {

                        // speichere den neuen Default-Pfad für die initiale Konfiguration
                        dataManager.getDaedalusData().setPathToUserConf(userChosenFile.getCanonicalPath());
                        try
                        {
                            dataManager.saveDaedConfig();
                        } catch (TransformerException ex)
                        {
                            Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        this.repaint();
                        this.pack();
                    } else
                    {
                        // Datei existiert nicht
                        // was allerdings durch File-Chooser-Filterung ausgeschlossen werden kann
                        // doch trotzdem  zur Wahrung der Sicherheitsanforderungen an Code und Wahrung
                        // der Kosistenz
                        JOptionPane.showMessageDialog(this, "Das Filesystem sieht die gewählte Konfigurationsdatei als nicht existent an. Deshalb kann diese auch nicht geladen werden.", "Filesystem", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SAXException ex)
                {

                    JOptionPane.showMessageDialog(this, "Die gewählte Konfiguration konnte nicht geparsed werden. Gehen Sie sicher, dass die Konfigurationsdatei dem vorgebenen Konfigurationsstandard entspricht.", "Config_Parsing", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "incorrect config file format", ex);
                } catch (IOException ex)
                {

                    JOptionPane.showMessageDialog(this, "Die gewählte Konfiguration konnte nicht korrekt geladen werden.", "Cofnig_Read", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "problem during reading file gropped up", ex);
                }
                break;
            case JFileChooser.ERROR_OPTION:
                throw new Error("JFileChooser internal programmatic error has occurred");
            default:
                throw new AssertionError("unforseen switch case has gropped up");
        }

    }

    private void processDeviceConnect()
    {

        try
        {

            // User mit Eingabeaufforderung konfrontieren
            deviceInput.show();

            // Gerätenamen und IP-Adresse aus dem Dialogobjekt holen (werden dabei zurückgesetzt -> Aufräumen implizit)
            String inputDevName = deviceInput.fetchInputDevName();
            String inputDevIP = deviceInput.fetchInputDevIP();
            Integer devID = null;
            try
            {
                devID = Integer.parseInt(deviceInput.fetchInputdevID());
            } catch (NumberFormatException ne)
            {
                Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "ungültige ID eingegeben", ne);
            }
            // Aktionen nur bei Verbindungswunsch durchführen, ansonsten einfach alles verwerfen
            if (deviceInput.getOption() == DeviceInputDialog.Option.Connect)
            {

                // Überprüfung der eingegebenen Geräte-Daten und entsprechende Reaktion
                // darauf, falls eine Prüfung scheitern sollte
                String userReproof = "";

                // ID zu klein oder ID zu groß
                if (devID == null)
                {
                    JOptionPane.showMessageDialog(this, "ID muss eine eine Ganz-Zahl zwischen 0 und 65535 sein", "Eingabefehler", JOptionPane.WARNING_MESSAGE);
                }
                if (devID.compareTo(MIN_DEV_ID) < 0 || devID.compareTo(MAX_DEV_ID) > 0)
                {
                    JOptionPane.showMessageDialog(this, "ID muss zwischen 0 und 65535 sein", "Eingabefehler", JOptionPane.WARNING_MESSAGE);
                }
                if (!inputDevName.matches(dataManager.getDaedalusData().getDEVICE_NAME_PATTERN()))
                {
                    userReproof += "Gerätename ist ungültig. ";
                }

                if (!inputDevIP.matches(dataManager.getDaedalusData().getIPADDRESS_PATTERN()))
                {
                    userReproof += "IPv4-Adresse folgt nicht der Standardspezifikation RFC 791.";
                }
                if (!userReproof.isEmpty())
                {

                    JOptionPane.showMessageDialog(this, userReproof, "Eingabefehler", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Gerät anbinden, falls noch nicht eingebunden
                if (!devData.expandDevices(devID, inputDevName, inputDevIP))
                {
                    userReproof = "Gerät ist bereits angebunden.";
                    JOptionPane.showMessageDialog(this, userReproof, "Geräteduplikat", JOptionPane.WARNING_MESSAGE);
                }
                try
                {
                    // Absichern der neuen Konfiguration nach Neuverbindung
                    dataManager.saveUserConfig(new File(dataManager.getDaedalusData().getPathToUserConf()));
                } catch (TransformerException ex)
                {
                    Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex)
        {
            Logger.getLogger(DaedalusFrame.class.getName()).log(Level.SEVERE, "unerwarteter, generischer Fehler beim Geräteverbindungsaufbau ", ex);
        }
    }

    private void processDeviceDisconnect()
    {

        //Funktionsbuttons aktivieren
        UiUtils.shiftButtonState(new JButton[]
        {
            disconnectDevicesButton, cancelButton
        }, true);

        // fokussiere die Tabelle und deaktiviere im Anschluss das Menü, um den Fokus
        // auf die Tabelle bis zum Ende der Auswahl zu erzwingen
        devices.requestFocus();
        UiUtils.shiftMenuBarSate(mainMenuBar, false);

        // erlaube das Selektieren von Geräten
        devices.setRowSelectionAllowed(true);
        devices.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Funktionalität steckt zum größten Teil in den Knöpfen zur Verbindungsaufgabe
    }

    private static class FileFilterImpl extends FileFilter
    {

        public FileFilterImpl()
        {
        }

        @Override
        public boolean accept(File file_)
        {
            String fileName = file_.getAbsolutePath();
            return fileName.endsWith("daed");
        }
    }

}
