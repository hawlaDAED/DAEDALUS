/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.polling;

import hawla.daedalus.communicationlayer.ModbusClient;
import hawla.daedalus.configuration_module.IFPollingData;
import java.io.IOException;

/**
 *
 * @author s-plangg
 */
public class PollingManager 
{
    private PollingLogic pLogic;
    private ModbusClient mbc;
    private IFPollingData ifpd;
    public static final Boolean SYNC_RESP = true;
    
    public PollingManager(ModbusClient mbc,IFPollingData ifpd) 
    {        
        this.mbc = mbc;
        this.ifpd = ifpd;
        mbc.setPoller(this);
    }
    
    public byte[] receivingMessage(byte[] request) throws IOException
    {
        if(request.length != 8)
            throw new IllegalArgumentException("Request Length invalid");
        
        byte[] fulladdress = {
            request[0],     //DEVICE ID
            request[1],
            request[2],     //PIN ID
            request[3],
            request[4],     //NAMESPACE ID
            request[5],     //COUNT
        };
        
        byte flags = request[6];
        
        byte[] result = {
                request[0],     //DEVICE ID
                request[1],
                request[2],     //PIN ID
                request[3],
                request[4],     //NAMESPACE ID
                request[5],     //COUNT
                (byte)(request[6] & 0xFB),
                0
            };
        
        
        
        if((flags & 2) == 0)
            throw new IllegalArgumentException("No Request for Polling - Check Filter");
        
        boolean success = false;
        if((flags & 1) == 0)    //Code "10" erkannt - Polling Anmeldung
        {
            if(pLogic == null)
            {
                pLogic = new PollingLogic(mbc,ifpd);
            }
            
            success = pLogic.registerPolling(new FullAddress(fulladdress));
        }
        
        else                    //Code "11" erkannt - Polling Abmeldung
        {     
            
            if(pLogic != null)
            {
                success = pLogic.unregisterPolling(new FullAddress(fulladdress));
            }
        }
        
        if(success)
        {
            result[6] |= (byte)4;
        }      
    
        return result;        
    } 
    
    public void close()
    {
        if(pLogic != null)
            pLogic.close();
    }
    
    public void answerPolling(byte[] response) throws InterruptedException
    {
        pLogic.answerPolling(response);
    }
    
}
