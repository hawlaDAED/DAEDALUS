/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.IOmanager;

import hawla.daedalus.SPSInterconnection.Filter;
import hawla.daedalus.SPSInterconnection.SPSServer;
import hawla.daedalus.UI.UserInterface;
import hawla.daedalus.communicationlayer.ModbusClient;
import hawla.daedalus.configuration_module.DataManager;
import hawla.daedalus.connectionlayer.ConnectionManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * Teilmodul, das den Zweck hat, den Gesamtablauf (Starten, übergeordnete Ablaufsteuerung, Beenden) der Software zu
 * steuern.
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class Daedalus_Driver
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

        final String usage = "daedalus [path_to_config]";
        final int posOfConfigPath = 0;

        try
        {

            //###############################################################################
            // Konfgurieren des Loggers, um die Software zur Laufzeit überwachen zu können
            Logger rootLogger = Logger.getLogger("");
            //Verhindere Log-Ausgaben auf der Konsole
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers[0] instanceof ConsoleHandler)
            {
                rootLogger.removeHandler(handlers[0]);
            }

            // lege das Log-Level fest
            rootLogger.setLevel(Level.WARNING);
            // Erzeuge einen Handler, der das Software-Log in Form einer Textdatei ausgibt
            // der Log landed hierbei im Homeverzeichnis des Ausführers mit fortlaufender Nummerierung der Logdatei
            FileHandler fileTxt = new FileHandler("%h/DAED_runtime%u.log");

            // erzeuge einen Formatierer, der reine Textform ausgibt
            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            rootLogger.addHandler(fileTxt);

            // create a TXT formatter
//            formatterTxt = new SimpleFormatter();
//            fileTxt.setFormatter(formatterTxt);
//            rootLogger.addHandler(fileTxt);
            //###############################################################################
            // zu viele Angaben durch Anwender
            if (args.length > 2)
            {
                System.err.println(usage);
                System.exit(-1);
            }

            // TODO Driver hingehend Software-Nutzung von Außen ausprogrammieren
            // Erstellung des zentralen Daten-Haltungs- wie Daten-Verwaltungs-Moduls \
            // auf Basis des Konfigurations-Pfades, abgelegt in den statischen Daten der Software
            DataManager dm = new DataManager();
            // fix lokalisierbare Konfigurationsdaten müssen vor allen anderen Daten geladen werden,
            // da diese den Pfad zur initialen Konfiguration beinhalten
            dm.loadDaedConfig();

            // kein Default-Pfad angegeben
            if (args.length == 2)
            {
                // Laden der auf der CLI angegebenen Konfigurationsdatei
                dm.loadUserConfig(new File(args[posOfConfigPath]));

            } else
            {
                // Laden der Defaultkonfiguration
                dm.loadUserConfig(new File(dm.getDaedalusData().getPathToUserConf()));
            }

            // 1. Erstellung des Verbindungsmoduls mitsamt der vorgesehenen dynamischen Daten dafür
            ConnectionManager connectionManager = new ConnectionManager(dm.getTransportData());

            // 2. Erstellung des ModbusClient mitsamt Datenübergabe
            SPSServer conToSps = null;
            ModbusClient modbusClient = new ModbusClient(dm.getClientData(), connectionManager);

            // 3. Erstellen und Öffnen des ServerSockets
            // -> unter Verwendung des IFPollingData
            conToSps = new SPSServer(new Filter(modbusClient, dm.getDaedalusData()), dm.getDaedalusData());
            conToSps.start();
            
            modbusClient.setSpsServer(conToSps);

            // 4. Hochfahren des UI
            Thread ui = new Thread(new UserInterface(dm));
            ui.start();

            // Blockieren, bis UI vom Nutzer ordnungsgemäß beendet wird
            ui.join();

            // Aufräumarbeiten (vor allen Dingen die Freigabe von Ressourcen)
            conToSps.interrupt();
            conToSps.shutdownSPSServer();
            connectionManager.closeAll();
            
            // die JVM letztendlich aufräumen und schließen
            System.exit(0);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerConfigurationException ex)
        {
            Logger.getLogger(Daedalus_Driver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex)
        {
            Logger.getLogger(Daedalus_Driver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
