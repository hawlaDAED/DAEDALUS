/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author pho
 */
public class SPSTest
{
    private static final byte lichtschrHintenMask = 1;
    private static final byte lichtschrVorneMask = 2;
    private static final byte stanzeObenMask = 4;
    private static final byte stanzeUntenMask = 8;

    private static SPSTestServer server;
    private static Socket clSocket;
    private static OutputStream toDaed;
    private static InputStream fromDaed;

    public static void main(String[] args) throws IOException, InterruptedException
    {

        server = new SPSTestServer(Thread.currentThread());
        server.start();
        clSocket = new Socket("127.0.3.1", 5021);
        toDaed = clSocket.getOutputStream();
        fromDaed = clSocket.getInputStream();

        byte[] initPolling
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x02, // Namepace
                    0x04, // Count
                    0x02, // Flag: S/RWP
                    0x00 // Value
                };

        byte[] stopPolling
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x02, // Namepace
                    0x04, // Count
                    0x03, // Flags: S/RWP
                    0x33 // Value
                };

//        Minimaltest
//        execCmd(initPolling, "Init Polling", false);
//
//        while(true)
//        {
//            lineBackAndForth();
//            //Thread.sleep(3000);
//        }
        
        execCmd(initPolling, "Init Polling", false);

        System.out.println("STARTED POLLING");
        System.out.println("READY");

        /* BEGIN MAIN BLOCK */
        //blockUntilIn();
        while (true)
        {

            lineIn();
            punchDown();


            punchUp();

            lineOut();

            if (1 == 2)
            {
                break;
            }
        }

        //stopLine();
        /* END MAIN BLOCK */

        execCmd(stopPolling, "Stop Polling", false);

        server.interrupt();

        System.out.println("---DONE---");

    }

    private static void lineBackAndForth() throws IOException, InterruptedException
    {
        byte[] startLineUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopLineUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        byte[] startLineDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x01, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopLineDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x01, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        execCmd(startLineUp, "Line Up Start", true);
        Thread.sleep(2000);
        execCmd(stopLineUp, "Line Up Stop", true);

    }

    private static void lineIn() throws IOException, InterruptedException
    {
        byte[] startLineUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopLineUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x00, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        execCmd(startLineUp, "Line Up Start", true);
        
        synchronized (Thread.currentThread())
        {

            server.setNextInterest(lichtschrVorneMask, false);
            while (!server.syncWait)
            {
                Thread.currentThread().wait();
            }
            server.syncWait = false;

        }
        
        execCmd(stopLineUp, "Line Up Stop", true);
    }

    private static void lineOut() throws IOException, InterruptedException
    {
        byte[] startLineDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x01, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopLineDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x01, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        execCmd(startLineDown, "Line Down Start", true);
        
        synchronized (Thread.currentThread())
        {

            server.setNextInterest(lichtschrHintenMask, false);
            while (!server.syncWait)
            {
                Thread.currentThread().wait();
            }
            server.syncWait = false;

        }
        
        execCmd(stopLineDown, "Line Down Stop", true);
    }

    private static void punchUp() throws IOException, InterruptedException
    {
        byte[] startPunchUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x02, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopPunchUp
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x02, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        execCmd(startPunchUp, "Punch Up Start", true);
        
        synchronized (Thread.currentThread())
        {

            server.setNextInterest(stanzeObenMask, true);
            while (!server.syncWait)
            {
                Thread.currentThread().wait();
            }
            server.syncWait = false;

        }
        
        execCmd(stopPunchUp, "Punch Up Stop", true);
    }

    private static void punchDown() throws IOException, InterruptedException
    {
        byte[] startPunchDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x03, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x01 // Value
                };

        byte[] stopPunchDown
                =
                {
                    0x00,
                    0x01, // Device ID ^
                    0x00,
                    0x03, // Pin ID ^
                    0x01, // Namepace
                    0x01, // Count
                    0x01, // Flags: S/RWP
                    0x00 // Value
                };

        execCmd(startPunchDown, "Punch Down Start", true);
        
        synchronized (Thread.currentThread())
        {

            server.setNextInterest(stanzeUntenMask, true);
            while (!server.syncWait)
            {
                Thread.currentThread().wait();
            }
            server.syncWait = false;

        }

        execCmd(stopPunchDown, "Punch Down Stop", true);
    }

    public static byte[] execCmd(byte[] tosend, String desc, boolean wait) throws IOException, InterruptedException
    {
        System.out.println("--------");
        System.out.println(desc);

        byte[] buffer = new byte[8];

        System.out.println("SEND: " + Arrays.toString(tosend));

        toDaed.write(tosend);
        toDaed.flush();

        if (wait)
        {

            fromDaed.read(buffer);

            System.out.println("RECV: " + Arrays.toString(buffer));
        }

        return buffer;

    }

}
