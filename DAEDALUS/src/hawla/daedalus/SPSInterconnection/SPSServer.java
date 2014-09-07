package hawla.daedalus.SPSInterconnection;

import hawla.daedalus.configuration_module.IOManagerData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s-mtafel / s-plangg
 */
public class SPSServer extends Thread {

    private static final int PORT = 5021;
    private static final int PROTOCOL_PACKET_SIZE = 8;
    // Fluchtwert für Eingabedatenstrom zur Erkennung eines Verbindungsabbruches
    private static final int CONNECTION_ESCAPE = -1;
    // je Verbindung zugewiesener, individueller Socket
    private Socket sessionSocket;
    // angelegter, dem Server zu
    private final ServerSocket serverSocket;
    // Filterweiche, um von der SPS-Steuerungssoftware nach dem vereinbarten Protokoll die Packete an die zuständigen Module (Polling- oder Modbus-Client) weiterzureichen
    private final Filter filteringSwitch;
    // Datenempfang aktiv
    private boolean sessionOpen;
// Datenstrom zum Empfangen der Daten nach Protokoll
    private InputStream in;
    // Datenstrom zum Rausschreiben der synchronen Antworten an die SPS-Seite
    private OutputStream out;
    // Datenschaufel zum Weiterreichen der eingegangenen Daten
    private final byte[] receivedData = new byte[PROTOCOL_PACKET_SIZE];
    // Zugriff auf Software-Konfiguration zum Update der Inet-Adresse der Gegenseite bei jeder Session
    private IOManagerData daedConf;

    public SPSServer(final Filter filter, final IOManagerData data) throws IOException {
        serverSocket = new ServerSocket(PORT);
        this.filteringSwitch = filter;
        daedConf = data;
    }

    @Override
    public void run() {
        while (!(this.isInterrupted())) {
            try {

                // Verdindungsaufbau in aktueller Session
                sessionSocket = serverSocket.accept();

                // Initialisierung der Ströme für die aktuelle Session
                in = sessionSocket.getInputStream();
                out = sessionSocket.getOutputStream();

                // Herausziehen der IPv4-Inetadresse der Gegenseite (SPS) und Rekonfiguration zur
                daedConf.setSPSIP(sessionSocket.getInetAddress());

                // Rekonfiguration 
                // starte den Sessionablauf
                sessionOpen = true;
                // empfange so lange Daten, wie die Verbindung auf beiden Seiten offen ist und die Datenübertragung nicht zu Ende ist
                while (sessionOpen) {

                    // Während das Lesen aus dem Eingabestrom nicht einen Wert kleiner als Null zurückliefert (Fluchtwert laut JAVA_API für Verbindungsabbruch)
                    for (int result = in.read(receivedData); result > CONNECTION_ESCAPE; result = in.read(receivedData)) {
                        filteringSwitch.forward(receivedData);
                    }

                    // Session wurde unterbrochen - einseitiger Verbindungsabbau
                    // -> somit müssen sessionbezogene Ressourcen freigegeben werden
                    shutdownSession();
                }

            } catch (IOException ex) {
                Logger.getLogger(SPSServer.class.getName()).log(Level.SEVERE, "problem at initialising a Server-Socket", ex);
            } catch (Exception ex) {
                Logger.getLogger(SPSServer.class.getName()).log(Level.SEVERE, "General problem at SPSServer – unexpected", ex);
            }
        }

    }

    public void shutdownSession() throws IOException {
        sessionOpen = false;
        in.close();
        out.close();
        in = null;
        out = null;

        // Session-Socket könnte gar nicht durch den Server-Socket erstellt worden sein,
        // da es keine Verbindungsversuche gab
        if (sessionSocket != null) {
            sessionSocket.close();
            sessionSocket = null;
        }
    }

    public void shutdownSPSServer() throws IOException {

        sessionOpen = false;
        this.interrupt();
        serverSocket.close();
        // Session-Socket könnte gar nicht durch den Server-Socket erstellt worden sein,
        // da es keine Verbindungsversuche gab
        if (sessionSocket != null) {
            sessionSocket.close();
        }

    }

    public boolean answerSPS(final byte[] modbusAnswer) {
        
        
        
        if (out == null) {
            return false;
        } else {
            try {
                Logger.getLogger(SPSServer.class.getName()).log(Level.SEVERE, "ANSWERING SPS: " + Arrays.toString(modbusAnswer), "");
                
                out.write(modbusAnswer);
                out.flush();
            } catch (IOException ex) {
                Logger.getLogger(SPSServer.class.getName()).log(Level.SEVERE, "answer to SPS failed", ex);
            }

            return true;
        }

    }

}
