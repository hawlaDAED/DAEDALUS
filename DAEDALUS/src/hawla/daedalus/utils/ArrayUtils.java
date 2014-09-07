/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.utils;

import java.lang.reflect.Array;

/**
 *
 * @author s-plangg
 */
public class ArrayUtils 
{
    private ArrayUtils(){}
    
    public static byte[] concatenate (byte[] A, byte[] B) {
    int aLen = A.length;
    int bLen = B.length;

    byte[] C= new byte[aLen+bLen];
    System.arraycopy(A, 0, C, 0, aLen);
    System.arraycopy(B, 0, C, aLen, bLen);

    return C;
}
    
    public static boolean compareTwoByteArrays(Byte[] left, Byte[] right)
    {
        if(left.length != right.length)
            return false;
        
        for(int i = 0; i < left.length; i++)
        {
            if(left[i] != right[i])
                return false;
        }
        
        return true;
    }
    
    
}
