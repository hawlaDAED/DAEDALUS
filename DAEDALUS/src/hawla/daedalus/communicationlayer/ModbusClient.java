/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.communicationlayer;

import hawla.daedalus.SPSInterconnection.SPSServer;
import hawla.daedalus.configuration_module.IFclientDataRO;
import hawla.daedalus.connectionlayer.ConnectionManager;
import hawla.daedalus.connectionlayer.Receivable;
import hawla.daedalus.polling.PollingManager;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vanessa
 */
public class ModbusClient implements Receivable {

    private final IFclientDataRO data;
    private final ConnectionManager tcpCon;
    private SPSServer spsServer;
    PollingManager poller;
    short protID;

    byte[] receivedADU;
    byte[] device = new byte[2];
    byte[] transID = new byte[2];
    static int counter = 0;

    HashMap<ByteBuffer, Request> requestList;
    RequestHandler requHand = new RequestHandler(this);

    @SuppressWarnings("LeakingThisInConstructor")
    public ModbusClient(IFclientDataRO cl, ConnectionManager cM) {
        requestList = new HashMap<>();
        data = cl;
        protID = data.getMODBUS_PROT_ID();
        tcpCon = cM;
        spsServer = null;

        tcpCon.registerReceiveHandler(this);
    }

    public void setPoller(PollingManager poller) {
        this.poller = poller;
    }

    public void setSpsServer(SPSServer spsServer) {
        this.spsServer = spsServer;
    }

    // erhalten anfrage von der sps übern server
    // geräteID - 2, pinID/addressID - 2, namespaceID - 1, counter - 1, flag - 1, value - 1
    public int receivingMessage(byte[] SPSrequest, boolean polling) //von der polling seie wird das auf true gesetzt, vom server auf false
    {
        Request request = new Request(new byte[]{
            SPSrequest[0], SPSrequest[1]
        }, new byte[]{
            SPSrequest[2],
            SPSrequest[3]
        }, SPSrequest[4], SPSrequest[5], SPSrequest[6], SPSrequest[7], polling);

        transID[0] = (byte) (counter >> 8);
        transID[1] = (byte) counter;

        // Matthias
        // Einpacken der Transaktions-ID, um konformes equals als Key in HashMap zu gewährleisten
        ByteBuffer keyForRequest = ByteBuffer.wrap(transID);

        requestList.put(keyForRequest, request);

        try {
            device[0] = SPSrequest[0];
            device[1] = SPSrequest[1];

            if (requHand.handleADUData(SPSrequest) == 0) {
                throw new RequestException("An error occurred handling the Data given in the Request.");
            }

            receivedADU = requHand.getSignificantADUParts();

            //Standardwerte setzen
            receivedADU[0] = (byte) (counter >> 8);   // Transaction ID
            receivedADU[1] = (byte) counter;
            receivedADU[2] = (byte) (protID >> 8);    // Konvertierung des Short Wert aus der Data zu 2 bytes
            receivedADU[3] = (byte) protID;
            receivedADU[6] = data.getUnitIdentifier();

            tcpCon.sendRequest(receivedADU, device); //device byte array 2 elemente
            counter++;
        } catch (Exception ex) {
            spsServer.answerSPS(requHand.getErrorADU());

            Logger.getLogger(ModbusClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
    }

    @Override
    // erhalten Antwort vom TCP/ConnectionLayer
    public void receiveConfirmation(byte[] TcpIpADU) {
        transID[0] = TcpIpADU[0];
        transID[1] = TcpIpADU[1];

        boolean polling = false;
        try {

            PollADU padu = ResponseHandler.handleResponse(TcpIpADU, requestList);
            receivedADU = padu.getADU();
            polling = padu.isPolling();
            
            receivedADU[7] = TcpIpADU[9];

            Logger.getLogger(ModbusClient.class.getName()).log(Level.SEVERE, "SPSADU: " + Arrays.toString(receivedADU), "");

            if (polling) {

                poller.answerPolling(receivedADU);
            } else {
                spsServer.answerSPS(receivedADU);
            }

        } catch (Exception ex) {
            byte[] error = new String("error").getBytes();

            Logger.getLogger(ModbusClient.class.getName()).log(Level.SEVERE, null, ex);

            if (polling) {
                try {
                    poller.answerPolling(error);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ModbusClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } else {
                spsServer.answerSPS(error);
            }

        }
    }

}
