/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.net.InetAddress;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 *
 * Datencontainer für etwaige Systemdaten
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class IOManagerData implements IFPollingData
{

    // Standard-Namensraum-Identifizierer, der bereits vor dem Laden der Konfiguration vorliegen muss
    private final String DEFAULT_NS_URI;
    // Attribut zur Spezifizierung des Namensraums
    private final String DEFAULT_QUALIFIED_N;
    // Regulärer Ausdruck für IP-Adressen nach Version 4 nach RFC 791
    private final String IPADDRESS_PATTERN;
    private final String DEVICE_NAME_PATTERN;
    // Pfad zur standardmäßigen Schemadefinition zur Userkonfiguration des SW
    private final Path PATH_TO_USER_CONF_SCHEMA;
    // Pfad zur software-internen Standard-Schemadefinition
    private final Path PATH_TO_SW_CONF_SCHEMA;
    // Pfad zu den software-internen, dynamischen Daten, deren Pfad fix bleibt
    private final Path PATH_TO_SW_CONF;
    // Pfad zum Paket von Daedalus
    private final URL DAED_LOCATION;
    // initiales Zeitintervall zwischen zwei Polling-Vorgängen in Millisekunden
    private int pollingInterval;
    // IP Adresse der Gegenstelle
    private InetAddress SPSIP;
    // Pfad zur User-Konfiguration, die bei jedem Neuladen angepasst wird,
    // um die Nutzereinstellungen somit flexibel zu halten
    private String pathToUserConf;

//    // Initialisierer für den Pfad zum zentralen Konfigurations-Schema
//    static
//    {
//        // festgelegter, relativer Pfad zur fixen Schemadefinition zur Validierung aller Konfigurationsdateien
//        PATH_TO_USER_CONF_SCHEMA = FileSystems.getDefault().getPath("src", "hawla", "daedalus", "configuration_module", "user_config_schema.xsd").toAbsolutePath();
//        PATH_TO_SW_CONF_SCHEMA = FileSystems.getDefault().getPath("src", "hawla", "daedalus", "configuration_module", "daed_config_schema.xsd").toAbsolutePath();
//        PATH_TO_SW_CONF = FileSystems.getDefault().getPath("src", "hawla", "daedalus", "configuration_module", "sw_config.xml").toAbsolutePath();
//    }
    /**
     *
     */
    public IOManagerData()
    {
        DEFAULT_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
        DEFAULT_QUALIFIED_N = "xsi:noNamespaceSchemaLocation";
        IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        DEVICE_NAME_PATTERN = "\\p{Graph}{1,15}";
        
        // Lage der Binary von Daedalus im File-System-Baum 
        DAED_LOCATION = getClass().getProtectionDomain().getCodeSource().getLocation();

        // Pfad zum Daedalus-Projekt-Verzeichnis, um zu XML-Schemata und die Software-Konfiguration abzusteigen
        Path softwareMainDir = FileSystems.getDefault().getPath(DAED_LOCATION.getPath());
        softwareMainDir = softwareMainDir.getParent().getParent();

        // festgelegter, relativer Pfad zur fixen Schemadefinition zur Validierung aller Konfigurationsdateien
        PATH_TO_USER_CONF_SCHEMA = FileSystems.getDefault().getPath(softwareMainDir.toString(), "src", "hawla", "daedalus", "configuration_module", "user_config_schema.xsd").toAbsolutePath();
        PATH_TO_SW_CONF_SCHEMA = FileSystems.getDefault().getPath(softwareMainDir.toString(), "src", "hawla", "daedalus", "configuration_module", "daed_config_schema.xsd");
        PATH_TO_SW_CONF = FileSystems.getDefault().getPath(softwareMainDir.toString(), "src", "hawla", "daedalus", "configuration_module", "sw_config.xml");
        
        
    }

    @Override
    public int getPollingInterval()
    {
        return pollingInterval;
    }

    void setPollingIntervall(final int newInterval)
    {
        pollingInterval = newInterval;
    }

    public void setSPSIP(InetAddress SPSIP) {
        this.SPSIP = SPSIP;
    }

    
    
    String getDefaultNsURI()
    {
        return DEFAULT_NS_URI;
    }

    String getDefaultQualifiedN()
    {
        return DEFAULT_QUALIFIED_N;
    }

    public String getDEVICE_NAME_PATTERN()
    {
        return DEVICE_NAME_PATTERN;
    }

    public String getIPADDRESS_PATTERN()
    {
        return IPADDRESS_PATTERN;
    }

    public Path getPATH_TO_USER_CONF_SCHEMA()
    {
        return PATH_TO_USER_CONF_SCHEMA;
    }

    public Path getPATH_TO_SW_CONF_SCHEMA()
    {
        return PATH_TO_SW_CONF_SCHEMA;
    }

    public Path getPATH_TO_SW_CONF()
    {
        return PATH_TO_SW_CONF;
    }

    public String getPathToUserConf()
    {
        return pathToUserConf;
    }

    public URL getDAED_LOCATION()
    {
        return DAED_LOCATION;
    }

    public void setPathToUserConf(String pathToUserConf)
    {
        this.pathToUserConf = pathToUserConf;
    }

    @Override
    public InetAddress getPollingIp()
    {
        return SPSIP;
    }

}
