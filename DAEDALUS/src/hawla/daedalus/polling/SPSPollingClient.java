/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.polling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author s-plangg
 */
public class SPSPollingClient
{

    private Socket pollingSocket;
    private final int PORT = 5022;
    private OutputStream out;
    private InetAddress ipSPS;

    public SPSPollingClient(InetAddress ipSPS) throws IOException
    {
        this.ipSPS = ipSPS;
        
        //this.ipSPS = InetAddress.getByName("127.0.0.1");

        // CREATED LATER
        //pollingSocket = new Socket(ipSPS, PORT);
        //out = pollingSocket.getOutputStream();
    }

    public void close() throws IOException
    {
        pollingSocket.close();
    }

    public void send(byte[] toSend) throws IOException
    {
        if (pollingSocket == null)
        {
            pollingSocket = new Socket(this.ipSPS, PORT);
            out = pollingSocket.getOutputStream();
        }

        out.write(toSend);
        out.flush();
    }

}
