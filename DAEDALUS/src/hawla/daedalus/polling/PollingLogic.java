/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.polling;

import hawla.daedalus.communicationlayer.ModbusClient;
import hawla.daedalus.configuration_module.IFPollingData;
import hawla.daedalus.utils.ArrayUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s-plangg
 */
public class PollingLogic
{

    private final HashMap<FullAddress, Byte> memory;
    private PollingThread TPolling;
    private static boolean isCreated = false;
    private int polling_interval;   //Millis
    private ModbusClient mbc;
    private IFPollingData ifpd;
    private volatile boolean syncWait = false;

    public PollingLogic(ModbusClient mbc, IFPollingData ifpd)
    {
        this.mbc = mbc;
        this.ifpd = ifpd;
        polling_interval = ifpd.getPollingInterval();
        memory = new HashMap<>();
        if (isCreated)
        {
            throw new IllegalStateException("PollingLogic existiert bereits");
        }
        isCreated = true;
    }

    public boolean registerPolling(FullAddress fullad) throws IOException
    {
        synchronized (memory)
        {
            memory.put(fullad, null);
        }
        if (TPolling == null)
        {
            TPolling = new PollingThread();
            TPolling.start();
        }
        return true;
    }

    public boolean unregisterPolling(FullAddress fullad)
    {

        synchronized (memory)
        {
            memory.remove(fullad);
            if (memory.isEmpty())
            {
                TPolling.interrupt();
                TPolling = null;
            }
        }
        return true;
    }

    public void answerPolling(byte[] response) throws InterruptedException
    {
        TPolling.setResponse(response);
        syncWait = false;
        synchronized (TPolling)
        {
            syncWait = true;
            TPolling.notify();
        }
    }

    private class PollingThread extends Thread
    {

        private SPSPollingClient spc;
        public boolean waitFlag;
        private byte[] response;

        public PollingThread() throws IOException
        {
            spc = new SPSPollingClient(ifpd.getPollingIp());
        }

        @Override
        public void run()
        {

            while (!isInterrupted())
            {
                try
                {
                    Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "LOOOOOOOOOOOOOOP", "");
                    synchronized (memory)
                    {
                        for (Map.Entry<FullAddress, Byte> entry : memory.entrySet())
                        {

                            byte[] readRequest = ArrayUtils.concatenate(entry.getKey().toByteArray(), new byte[]
                            {
                                0, 0
                            });
                            mbc.receivingMessage(readRequest, true);
                            synchronized (this)
                            {
                                while (!syncWait)
                                {
                                    this.wait();
                                }

                            }

                            //Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "---AFTER WAIT: " + Arrays.toString(response), "");
                            if (response.length != 8)
                            {
                                throw new IllegalStateException("Response Length invalid");
                            }
                            if ((response[6] & 4) > 0) //SUCCESS FLAG = "1"
                            {

                                byte value = response[7];

                                Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "VALUE:" + value, "");

                                if ((entry.getValue() == null) || (entry.getValue().byteValue() != value))
                                {

                                    memory.put(entry.getKey(), value);

                                    byte[] responseToSPS = ArrayUtils.concatenate(entry.getKey().toByteArray(), new byte[]
                                    {
                                        6, value
                                    });
                                    try
                                    {
                                        spc.send(responseToSPS);
                                    } catch (IOException ex)
                                    {
                                        Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "Polling Logic: Send failed", ex);
                                    }
                                }
                            } else
                            {
                                byte[] responseToSPS = ArrayUtils.concatenate(entry.getKey().toByteArray(), new byte[]
                                {
                                    2, 0
                                });
                            }

                        }
                    }
                    try
                    {
                        sleep(polling_interval);
                    } catch (InterruptedException ex)
                    {
                        break;
                    }
                } catch (Exception ex)
                {
                    Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "general polling", ex);
                }
            }
            try
            {
                spc.close();
            } catch (IOException ex)
            {
                Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex)
            {
                Logger.getLogger(PollingLogic.class.getName()).log(Level.SEVERE, "general polling", ex);
            }
        }

        public void unsetWaitFlag()
        {
            waitFlag = false;
        }

        public void setResponse(byte[] response)
        {
            this.response = response;
        }

    }

    public void close()
    {
        if (TPolling != null)
        {
            TPolling.interrupt();
        }
    }

}
