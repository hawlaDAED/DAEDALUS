/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.communicationlayer;

/**
 *
 * @author Vanessa
 */
public class Request
{
    private final byte [] deviceID;
    private final byte [] address;
    private final byte namespaceID;
    private final byte counter;
    private final byte flag;
    private final byte value;
    private final boolean polling;

    public Request(byte [] device, byte [] addr, byte namespID, byte count, byte flag, byte val, boolean poll)
    {
        this.address=addr;
        this.counter=count;
        this.deviceID=device;
        this.flag=flag;
        this.namespaceID=namespID;
        this.value=val;
        this.polling = poll;
    }
    
    public boolean isPolling() {
        return polling;
    }
    
    public byte[] getDeviceID()
    {
        return deviceID;
    }

    public byte[] getAddress()
    {
        return address;
    }

    public byte getNamespaceID()
    {
        return namespaceID;
    }

    public byte getCounter()
    {
        return counter;
    }

    public byte getFlag()
    {
        return flag;
    }

    public byte getValue()
    {
        return value;
    }
    
}
