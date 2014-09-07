/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.communicationlayer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vanessa
 */
public class ResponseHandler
{

    //private static boolean polling;

    static PollADU handleResponse(byte[] responseADU, HashMap<ByteBuffer, Request> requList) throws Exception
    {
        byte[] toSend = new byte[8];

        byte[] transID = new byte[]
        {
            responseADU[0], responseADU[1]
        };
        Request requData = null;

        // Einpacken der Transaktions-ID, um mit konformem equals den zugehörigen Request aus der Request-Map-Struktur zu ziehen
        ByteBuffer keyForRequest = ByteBuffer.wrap(transID);

        Set<ByteBuffer> keysOfRequs = requList.keySet();

        if (keysOfRequs.contains(keyForRequest))
        {
            requData = requList.get(keyForRequest);
        }

        if (requData != null)
        {
            boolean isPolling = requData.isPolling();
            toSend[0] = requData.getDeviceID()[0];
            toSend[1] = requData.getDeviceID()[1];
            toSend[2] = requData.getAddress()[0];
            toSend[3] = requData.getAddress()[1];
            toSend[4] = requData.getNamespaceID();
            toSend[5] = requData.getCounter();

            if (responseADU[7] >= 0x80)
            {
                toSend[6] = (byte) (requData.getFlag() & 0x03);  //bei Fehlerfall
            } else
            {
                toSend[6] = (byte) (requData.getFlag() | 0x04);  //bei Erfolg
            }
            return new PollADU(toSend,isPolling);
        } else
        {
            throw new Exception("Not match found - TRANSID");
        }
//    public static boolean isPolling()
//    {
//        return polling;
//    }
}}
