/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.SPSInterconnection;

import hawla.daedalus.communicationlayer.ModbusClient;
import hawla.daedalus.configuration_module.IFPollingData;
import hawla.daedalus.polling.PollingManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class Filter
{

    // Position des entscheidenden Bytes in den empfangenen Daten -> das 7. Byte entsprechend dem Protokoll
    private final static int decisiveBytePos = 6;
    // Zugriffs-Handle auf den Modbus-Client
    private final ModbusClient modbusClient;
    // Handle auf den Polling-Manager
    private final PollingManager pollingManager;
    // Bitmaske zur Entscheidungshilfe: 00000010
    private final static byte decisiveBitMask = 2;

    public Filter(final ModbusClient modbusClient, final IFPollingData pollingData)
    {
        this.modbusClient = modbusClient;
        this.pollingManager = new PollingManager(modbusClient, pollingData);

        //TESTEN!
//        byte[] testA =
//        {
//            0x00,
//            0x01,
//            0x00,
//            0x01,
//            0x01,
//            0x00,
//            0x01,
//            0x01,
//            0x01
//        };
//        this.forward(testA);
    }

    public void forward(final byte[] receivedData)
    {

        // Herauspulen des Bytes, aufgrund dessen die Weiterleitungsentscheidung basiert
        byte decisiveByte = receivedData[decisiveBytePos];

        // Falls das siebte Bit des siebten Bytes des Protokollpaketes gesetzt ist,
        // dann gehen die Daten an den Polling-Sub-Ast der Softwares
        if ((decisiveByte & decisiveBitMask) == decisiveBitMask)
        {
            try
            {
                pollingManager.receivingMessage(receivedData);
            } catch (IOException ex)
            {
                Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, "switching data to polling part failed", ex);
            }
        } else
        {
            // Aufruf der asynchronen Nachrichtenübergabe -> Asynchronität spezifiziert mittels des zweiten Parameters
            modbusClient.receivingMessage(receivedData, false);
        }

    }

    public void shutdownPolling()
    {
        pollingManager.close();
    }

}
