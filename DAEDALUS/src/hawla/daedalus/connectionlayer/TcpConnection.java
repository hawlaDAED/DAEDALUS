/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.connectionlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pho
 */
class TcpConnection
{

    public static final Boolean SYNC_TCPCONN = true;

    private Socket m_socket;
    private static int m_port;
    private final InetAddress m_inetAddress;
    private DataOutputStream os;
    private DataInputStream is;

    public TcpConnection(byte[] deviceName) throws IOException
    {
        //IP RESOLUTION IFTransportDataRO
        HashMap<Integer, String[]> currDevices = ConnectionManager.getConfig().getDevices();
        int deviceID = deviceName[0] * 0xff + deviceName[1];

        String Ip = currDevices.get(deviceID)[1];
        m_inetAddress = InetAddress.getByName(Ip);
        m_port = ConnectionManager.getConfig().getPort();
        init_connection();
    }

    byte[] sendRequest(byte[] TcpIpADU) throws IOException
    {
        byte[] adu_pre = new byte[512];

        synchronized (SYNC_TCPCONN)
        {
            os.write(TcpIpADU);
            os.flush();

            is.read(adu_pre);
        }

        int nmbOfBytes = adu_pre[5] + (0xff * adu_pre[4]) + 7;

        byte[] adu = new byte[nmbOfBytes];
        

        System.arraycopy(adu_pre, 0, adu, 0, nmbOfBytes);
  
        return adu;
    }

    boolean close() throws IOException
    {
        os.close();
        is.close();
        m_socket.close();
        return m_socket.isClosed();
    }

    private void init_connection() throws IOException
    {
        m_socket = new Socket(m_inetAddress, m_port);
        os = new DataOutputStream(m_socket.getOutputStream());
        is = new DataInputStream(m_socket.getInputStream());
    }

}
