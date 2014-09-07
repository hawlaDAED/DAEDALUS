/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.communicationlayer;

/**
 *
 * @author s-plangg
 */
public class PollADU 
{
    private byte[] ADU;
    private boolean isPolling;
    
    PollADU(byte[] ADU, boolean isPolling)
    {
        this.ADU = ADU;
        this.isPolling = isPolling;
    }

    public byte[] getADU() {
        return ADU;
    }

    public boolean isPolling() {
        return isPolling;
    }
    
    
}
