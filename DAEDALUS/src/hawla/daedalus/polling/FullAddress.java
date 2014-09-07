/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.polling;

import java.util.Arrays;

/**
 *
 * @author s-plangg
 */
public class FullAddress 
{
    private byte[] fullAddress;
    
    public FullAddress(byte[] fullAddress)
    {
        if(fullAddress.length != 6)
            throw new IllegalArgumentException("Address Length invalid");
        this.fullAddress = fullAddress;
    }

    @Override
    public int hashCode() 
    {
        return Arrays.hashCode(this.fullAddress);
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FullAddress other = (FullAddress) obj;
        if (!Arrays.equals(this.fullAddress, other.fullAddress)) {
            return false;
        }
        return true;
    }

    public byte[] toByteArray() 
    {
        return fullAddress;
    }
}
