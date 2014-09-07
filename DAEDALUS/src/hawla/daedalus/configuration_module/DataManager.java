/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * Teilmodul zur Datenhaltung und Verwaltung der kompletten Software
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class DataManager
{

    // Parser für XML-Daten der User-Konfiguration
    private final DocumentBuilder builderUserData;
    // Parser für XML-Daten der SW-Konfiguration
    private final DocumentBuilder builderSWData;

    // Referenzen auf die gesamten Daten des Projekts
    private final IOManagerData daedalusData;
    private TransportData transportData;
    private ClientData clientData;
// Transformer zum Abspeichern der Softwarekonfiguration
    private final Transformer transformer;

    /**
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public DataManager() throws ParserConfigurationException, SAXException, TransformerConfigurationException
    {

        // initiale Konfigurations-Objekte anlegen
        daedalusData = new IOManagerData();
        transportData = new TransportData();
        // die Device-Namen des Clients stützen sich auf die Schlüssel-Werte in der
        // Geräte-Daten-Struktur der Transportschicht (so lässt sich Speicher-Overhead vermeiden)
        clientData = new ClientData(transportData.getDeviceNames());

        final String JAXP_SCHEMA_LANGUAGE
                = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        final String W3C_XML_SCHEMA
                = "http://www.w3.org/2001/XMLSchema";

        // Konfiguration des SAX-Parsers, um aus xsd-Dateien direkt zu lesen
        final String JAXP_SCHEMA_SOURCE
                = "http://java.sun.com/xml/jaxp/properties/schemaSource";
        // Zeiger im FS zu dem fixen Konfigurations-Datei-Schema
        final String USER_CONFIG_SCHEMA
                = daedalusData.getPATH_TO_USER_CONF_SCHEMA().toString();
        // ebenso Zeiger im FS auf das Konfigurations-Schema für die SW-Daten
        final String DAED_CONFIG_SCHEMA = daedalusData.getPATH_TO_SW_CONF_SCHEMA().toString();

        final boolean validating = true;
        final boolean commentsIgnore = true;
        final boolean nsAware = true;
        final boolean ingoreWhiteSpace = true;
        final boolean prettyPrinting = true;
        // Konfigurieren der Document-Builder-Fabrik mit den vorbereiteten Parametern
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(nsAware);
        factory.setIgnoringElementContentWhitespace(ingoreWhiteSpace);
        factory.setIgnoringComments(commentsIgnore);
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        factory.setAttribute(JAXP_SCHEMA_SOURCE, USER_CONFIG_SCHEMA);
        builderUserData = factory.newDocumentBuilder();
        factory.setAttribute(JAXP_SCHEMA_SOURCE, DAED_CONFIG_SCHEMA);
        builderSWData = factory.newDocumentBuilder();

        // Initialisiere Transformer, um die aktuelle Software-Konfiguration abzuspeichern
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        //  transformerFactory.setFeature(OutputKeys.INDENT, prettyPrinting);
        transformer = transformerFactory.newTransformer();

    }

    public boolean loadDaedConfig() throws SAXException, IOException
    {

        // den gesamten Konfgurations-Baum im Speicher anlegen
        Document configurationTree = parseConfig(daedalusData.getPATH_TO_SW_CONF().toFile(), false);

        // Laden des Initialisierungspfadknotens zur SW-Konfiguration
        Node initPath = configurationTree.getFirstChild();

        // Pfad als Stringform
        String initPathWS = initPath.getFirstChild().getTextContent();

        
        // setzen des Initialisiserungspfades
        daedalusData.setPathToUserConf(initPathWS);

        // Laden des Polling-Intervall-Knotens
        Node pollingIntervallNode = initPath.getFirstChild().getNextSibling();

        // Herauspopeln der Ganzzahldaten aus dem Textstring des Konfigurations-Baumes
        int pollingIntervall = Integer.parseInt(pollingIntervallNode.getTextContent());

        // Setzen der Daten im Software-Konfigurations-Datencontainer
        daedalusData.setPollingIntervall(pollingIntervall);

        return true;
    }

    public boolean saveDaedConfig() throws TransformerException
    {

        final String schemaLocation = "./" + daedalusData.getPATH_TO_SW_CONF_SCHEMA().getFileName().toString();
        // Baumwurzel für Konfigurations-Datei anlegen
        Document configTree = builderUserData.newDocument();
        // Daten-Wurzel nach Schema erzeugen und einhängen in Baum
        Element softwareConfig = configTree.createElement("SoftwareConfiguration");
        configTree.appendChild(softwareConfig);
        softwareConfig.setAttributeNS(daedalusData.getDefaultNsURI(), daedalusData.getDefaultQualifiedN(), schemaLocation);

        // Element für den initialen Konfigurations-Pfad anlegen
        Element initConfigPath = configTree.createElement("InitConfigPath");
        // Einhängen in den Software-Konfigurations-Baum
        softwareConfig.appendChild(initConfigPath);
        // Pfad zur initialien Konfigurationsdatei in DOM schreiben
        initConfigPath.setTextContent(daedalusData.getPathToUserConf());

        // Polling-Intervall in die Konfiguration schreiben
        Element pollingInterval = configTree.createElement("PollingInterval");
        pollingInterval.setTextContent(daedalusData.getPollingInterval() + "");
        softwareConfig.appendChild(pollingInterval);

        // Rausschicken der Konfiguration über den Transformer
        DOMSource source = new DOMSource(configTree);
        StreamResult result = new StreamResult(daedalusData.getPATH_TO_SW_CONF().toFile());
        transformer.transform(source, result);

        return true;
    }

    /**
     * Methode zum Parsen einer Anwender-Konfigurationsdatei im XML-Format. Dabei muss die zu ladende
     * Konfigurationsdatei valide sein gegenüber einem fixen, vordefinierten XML-Schema, das der Software bekannt ist.
     *
     * @param configFile
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public boolean loadUserConfig(final File configFile) throws SAXException, IOException
    {
        if (ConfigUtils.checkFileExists(configFile))
        {

            // den gesamten Konfgurations-Baum im Speicher anlegen
            Document configurationTree = parseConfig(configFile, true);

            // die alten Daten komplett loswerden (dem GC übergeben)
            // und mit neuen Objekten füllen, um die Grundlage für neuzuladende Daten zu schaffen
            // Pfad zur Userkonfiguration muss aber beigehalten werden
//            final String pathToUserConf = this.daedalusData.getPathToUserConf();
//            daedalusData = new IOManagerData();
//            daedalusData.setPathToUserConf(pathToUserConf);
            transportData = new TransportData();
            // die Device-Namen des Clients stützen sich auf die Schlüssel-Werte in der
            // Geräte-Daten-Struktur der Transportschicht (so lässt sich Speicher-Overhead vermeiden)
            clientData = new ClientData(transportData.getDeviceNames());

            Node configRoot = configurationTree.getFirstChild();
            // oberste Ebene im Konfigurationsbaum
            NodeList topLevel = configRoot.getChildNodes();

            // Ordnung ist durch Software und XML-Schema sichergestellt
            for (int i = 0; i < topLevel.getLength(); i++)
            {
                Node currentSubtree = topLevel.item(i);

                switch (i)
                {
                    //1. IOManager-Daten
                    case 0:
                        break;
                    case 1:
                        // 2. TCP-Schicht-Daten
                        loadTransport(currentSubtree);
                        break;
                    //3. Client-Daten
                    case 2:
                        break;
                    default:
                        break;
                }
            }

            return true;
        }
        // Konfiguration nicht gelesen, da Datei  existiert nicht
        return false;
    }

    /**
     * Hiermit kann eine Anwender-Konfiguration zur Software im Filesystem abgelegt werden.
     *
     * @param configFile Zieldatei für die Konfigurations-Daten der Software
     */
    public boolean saveUserConfig(final File configFile) throws TransformerException
    {

        final String schemaLocation = "./" + daedalusData.getPATH_TO_USER_CONF_SCHEMA().getFileName().toString();
        // Baumwurzel für Konfigurations-Datei anlegen
        Document configTree = builderUserData.newDocument();
        // Daten-Wurzel nach Schema erzeugen und einhängen in Baum
        Element daedalusConfig = configTree.createElement("Daedalus_Config");
        configTree.appendChild(daedalusConfig);
        daedalusConfig.setAttributeNS(daedalusData.getDefaultNsURI(), daedalusData.getDefaultQualifiedN(), schemaLocation);
        // Subbäume der Modul-Daten anlegen
        Element IOManager = configTree.createElement("IOmanager");
        Element transport = configTree.createElement("transport");
        Element client = configTree.createElement("client");
        // Einhängen der Top-Level-Elemente der Subbäume
        daedalusConfig.appendChild(IOManager);
        daedalusConfig.appendChild(transport);
        daedalusConfig.appendChild(client);
         // 1. Erbaue den IOmanager-Subbaum
        // TODO noch keine variablen Daedalus-Software-Daten vorhanden

        // 2. Erbaue den Transport-Unterbaum in separater Methode
        buildTransportSubTree(configTree, transport);

        // 3. Erbaue den Client-Unterbaum
        // TODO noch keine variablen Client-Daten
        DOMSource source = new DOMSource(configTree);
        StreamResult result = new StreamResult(configFile);
        transformer.transform(source, result);

        return true;
    }

    /**
     * Auskunftsmethode für UI, um Zugriff auf Transportdaten zu erhalten
     */
    public TransportData getTransportData()
    {
        return transportData;
    }

    public ClientData getClientData()
    {
        return clientData;
    }

    public IOManagerData getDaedalusData()
    {
        return daedalusData;
    }

    // <editor-fold defaultstate="collapsed" desc="Hilfsmethoden">
    /**
     * Backend-Methode zum Parsen von Configurationsdateien
     *
     * @param configFile Konfigurationsdatei, die geparst werden soll
     * @return geparster XML-Baum der angegeben Konfigurations-Datei.
     */
    private Document parseConfig(final File configFile, final boolean userConfig) throws SAXException, IOException
    {

        Document readTree;
        if (userConfig)
        {
            readTree = builderUserData.parse(configFile);
        } else
        { // SW-Daten liegen vor
            readTree = builderSWData.parse(configFile);

        }

        return readTree;
    }

    private void buildTransportSubTree(Document configTree, Element transport) throws DOMException
    {

        Element submask = configTree.createElement("sub_mask");
//        ConfigUtils.assembleIPSubtree(submask, transportData.getSubnetMask(), configTree);
        submask.setTextContent(transportData.getSubnetMask() + "");
        transport.appendChild(submask);

        Element port = configTree.createElement("port");
        port.setTextContent(transportData.getPort() + "");
        transport.appendChild(port);

        Element devMap = configTree.createElement("dev_map");
        transport.appendChild(devMap);

        for (Integer dev_ID : transportData.getDevices().keySet())
        {

            // lege den Geräteknoten an und hänge ihn in die Geräte-Liste des Baumes
            Element device = configTree.createElement("device");
            devMap.appendChild(device);

            // die ID aus der intern gehalten Speicherstruktur in DOM anhängen
            Element devID = configTree.createElement("dev_ID");
            devID.setTextContent(dev_ID.toString());
            device.appendChild(devID);

            // hole Datencontainer mit Gerätenamen und zugehöriger IP, welche für
            // die nächsten Schritte gebraucht werden
            String[] devNameAndIP = transportData.getDevices().get(dev_ID);

            // den Device-Namen aus der Transport-Daten-Speicherstruktur in den Baum einhängen
            Element devName = configTree.createElement("dev_name");

            devName.setTextContent(devNameAndIP[0]);
            device.appendChild(devName);

            // IP-Unterknoten anlegen und einhängen
            Element IP = configTree.createElement("IP");
            device.appendChild(IP);

            // für jedes Gerät fülle den IP-Unterbaum mit den zugehörigen Daten
            IP.setTextContent(devNameAndIP[1]);

//            ConfigUtils.assembleIPSubtree(IP, transportData.getDevices().get(devN), configTree);
        }
    }

    private void loadIOManager(final Node currentSubtree)
    {

    }

    private void loadTransport(final Node currentSubtree) throws NumberFormatException, DOMException
    {

        // <editor-fold defaultstate="collapsed" desc=" Einlesen der Subnetzmaske">
        Node subnetNode = currentSubtree.getFirstChild();
//        NodeList subnetDataInTree = subnetNode.getChildNodes();
        final byte subnetMask = ConfigUtils.fetchSubnetMFromXML(subnetNode);

//        ConfigUtils.fetchIPFromXML(subnetDataInTree, subnetBytes);
        // Einsetzen der Subnetzmaske im gemeinsamen Daten-Container
        transportData.setSubnetMask(subnetMask);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Einlesen der Portnummer">
        Node portNode = currentSubtree.getFirstChild().getNextSibling();
        transportData.setPort(Integer.parseInt(portNode.getTextContent()));
        // </editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Einlesen der Devices">
        // Hinhangeln zum device_map node
        Node devMap = currentSubtree.getLastChild();
        // ziehe alle Devices heran
        NodeList devices = devMap.getChildNodes();
        // alles devices programmintern anlegen
        for (int i = 0; i < devices.getLength(); i++)
        {

            // den Geräteknoten insgesamt heranziehen
            Node device = devices.item(i);
            // Geräte_ID herausfinden
            Integer dev_ID = Integer.parseInt(device.getFirstChild().getTextContent());
            // den Device-Namen aus dem Device-Knoten herauslesen
            String devName = device.getFirstChild().getNextSibling().getTextContent();
            // den IP-Knoten des Gerätes heranziehen
            Node devIP = device.getLastChild();
            // temporärer IP-Behälter in Form eines Arrays
//            short[] IPBytes = new short[4];
            // auffüllen der IP zu dem Gerät
            final String IPv4 = ConfigUtils.fetchIPFromXML(devIP);
            // dynamisches Anlegen neuer Devices im programminternen Speicher(Heap)
            transportData.insertNovelDevice(dev_ID, devName, IPv4);

        }

//</editor-fold>
    }

    private void loadClient(final Node currentSubtree)
    {

    }

    // </editor-fold>
}
