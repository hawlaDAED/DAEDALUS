/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.connectionlayer;

import hawla.daedalus.configuration_module.IFtransportDataRO;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pho
 */
public class ConnectionManager
{

    public final int MAXIMUM_CONNECTIONS = 10;
    private HashMap<byte[], TcpConnection> connectionPool;
    private Receivable receiveHandler;
    private static IFtransportDataRO config;
    private static boolean isCreated = false;
//    private static byte[] testADU = {
//     0x00, //Transaktions ID
//     0x01,
//     0x00, //Protokoll ID, must be zero
//     0x00,
//     0x00, //Länge der noch folgenden Bytes, PDU + 1
//     0x06,
//     0x0B, //Unit ID, irrelevant
//     0x01, //Function Code
//     0x00, //Startadresse
//     0x00,
//     0x00, //Anzahl der Coils / Bits
//     0x01};
    private static byte[] testADU =
    {
        0x00, //Transaktions ID
        0x01,
        0x00, //Protokoll ID, must be zero
        0x00,
        0x00, //Länge der noch folgenden Bytes, PDU + 1
        0x06,
        0x0B, //Unit ID, irrelevant
        0x05, //Function Code
        0x00, //Startadresse
        0x00,
        (byte) 0xff, //
        (byte) 0x00
    };

    public ConnectionManager(IFtransportDataRO config)
    {
        if (isCreated)
        {
            throw new IllegalStateException("Connection Manager existiert bereits");
        }
        isCreated = true;
        connectionPool = new HashMap<>();
        this.config = config;
    }

    public void sendRequest(byte[] TcpIpADU, byte[] deviceName) throws UnknownHostException, IOException
    {
        //Thread rThread = new RequestThread(deviceName, TcpIpADU);
        Thread rThread = new RequestThread(deviceName, TcpIpADU);
        rThread.start();
    }

    public void registerReceiveHandler(Receivable handler)
    {
        receiveHandler = handler;
    }

    static IFtransportDataRO getConfig()
    {
        return config;
    }

    public void closeAll() throws IOException
    {
        for (TcpConnection con : connectionPool.values())
        {
            con.close();
        }
    }

    private class RequestThread extends Thread
    {

        byte[] deviceName;
        byte[] TcpIpADU;

        public RequestThread(byte[] devName, byte[] ADU)
        {
            deviceName = devName;
            TcpIpADU = ADU;
        }

        @Override
        public void run()
        {
            try
            {
                if (!(connectionPool.containsKey(deviceName)))
                {
                    if (connectionPool.size() <= MAXIMUM_CONNECTIONS)
                    {
                        try
                        {
                            connectionPool.put(deviceName, new TcpConnection(deviceName));
                        } catch (IOException ex)
                        {
                            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else
                    {
                        throw new Exception("Connection Overflow");
                    }
                }
                try
                {
                    byte[] responseAdu = connectionPool.get(deviceName).sendRequest(TcpIpADU);
                    Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, Arrays.toString(responseAdu), "");
                    receiveHandler.receiveConfirmation(responseAdu);
                } catch (IOException ex)
                {
                    Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (Exception ex)
            {
                Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "Unexpected exception", ex);
            }

        }
    }
}
