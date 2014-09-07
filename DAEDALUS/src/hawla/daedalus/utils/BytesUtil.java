/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hawla.daedalus.utils;

/**
 *
 * @author pho
 */
public class BytesUtil
{
    
    public static int byteToInt32(byte a)
    {
        return a & 0xFF;
    }
    
    public static int twoBytesToInt32(int a, int b)
    {
        return (int)a * 0xff + (int)b;
    }
}
