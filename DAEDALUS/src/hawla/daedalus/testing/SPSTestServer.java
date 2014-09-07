/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.testing;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author pho
 */
public class SPSTestServer extends Thread
{

    private int nextMask = -1;
    private boolean waitFor;
    private final Thread mainThread;
    public volatile boolean syncWait = false;

    public SPSTestServer(Thread mainThread)
    {
        this.mainThread = mainThread;
    }

    @Override
    public void run()
    {

        try (ServerSocket srvSocket = new ServerSocket(5022);
                Socket connSocket = srvSocket.accept();
                InputStream fromDaed = connSocket.getInputStream())
        {
            while (true)
            {
                byte[] buffer = new byte[8];

                int suc = fromDaed.read(buffer);

                refreshSensors(buffer[7]);

                System.out.println("##SERVER## " + Arrays.toString(buffer));

                if (isInterrupted())
                {
                    break;
                }
            }

        } catch (IOException | InterruptedException ex)
        {
            System.err.println("ERR: " + ex.getMessage());
        }

    }

    public void setNextInterest(int nextMask, boolean waitFor)
    {
        this.nextMask = nextMask;
        this.waitFor = waitFor;
    }

    private void refreshSensors(byte data) throws InterruptedException
    {

        System.out.println("REFRESHED");

        if (((data & nextMask) > 0) == waitFor)
        {
//            syncWait = false;
            synchronized (mainThread)
            {
                syncWait = true;
                mainThread.notify();
            }
        }

    }

    public boolean isSyncWait()
    {
        return syncWait;
    }

}
