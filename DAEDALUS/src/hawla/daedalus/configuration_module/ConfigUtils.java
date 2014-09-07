/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.io.File;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * Utility-Klasse für das Datenmodul
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
class ConfigUtils
{

    static boolean checkFileExists(final File configFile)
    {

        return configFile.exists();

    }

    static byte fetchSubnetMFromXML(final Node subnetNode)
    {
        return Byte.parseByte(subnetNode.getTextContent());
    }

    ;

    /**
     *
     * Auslesen der IP-Daten aus einem Subbaum im DOM
     *
     * @param dataInTree the value of dataInTree
     * @param IPBytes the value of subnetBytes
     * @throws NumberFormatException
     * @throws DOMException
     */
    static String fetchIPFromXML(final Node dataInTree) throws NumberFormatException, DOMException
    {

        return dataInTree.getTextContent();

    }

    /**
     * Beim Rausschreiben der Konfiguration kann man diese Methode beim Aufbau von IP-Subbäumen nutzen.
     *
     * @param parentForIP
     * @param IP
     * @param tree
     * @return
     */
    static Element assembleIPSubtree(Element parentForIP, final short[] IP, final Document tree)
    {
        for (int i = 1; i <= IP.length; i++)
        {

            int adaptedIndex = i - 1;
            Element bytePart = tree.createElement("byte" + i);
            parentForIP.appendChild(bytePart);
            bytePart.setTextContent((IP[adaptedIndex]) + "");
        }
        return parentForIP;
    }

}
