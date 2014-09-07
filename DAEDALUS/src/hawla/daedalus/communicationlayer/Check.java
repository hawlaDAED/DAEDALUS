 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.communicationlayer;

import static hawla.daedalus.utils.BytesUtil.byteToInt32;
import java.nio.ByteBuffer;

/**
 *
 * @author Vanessa
 */
public class Check
{

    boolean checkAddress(byte[] request)
    {
        //die Addresse befindet sich immer an derselben Stelle, sie muss sich immer in demselben Bereich befinden
        //konvertierung des byte arrays mit der enthaltenen adresse zu short damit man damit rechnen und vergleichen kann
        /*byte[] temp =
         {
         request[2], request[3]
         };
         ByteBuffer wrapped = ByteBuffer.wrap(temp);
         short num = wrapped.getShort();



         // kleinste adresse
         byte[] minAdr =
         {
         (byte) 0x00, (byte) 0x00
         };
         ByteBuffer wrappedMin = ByteBuffer.wrap(minAdr);
         short min = wrappedMin.getShort();
         //größte addresse
         byte[] maxAdr =
         {
         (byte) 0xFF, (byte) 0xFF
         };
         ByteBuffer wrappedMax = ByteBuffer.wrap(maxAdr);
         short max = wrappedMax.getShort();



         min = 0;
         max = (short)0xFFFF;

         System.err.print("min: " + min);
         System.err.print("num: " + num);
         System.err.println("max: " + max);

         if (min <= num && num <= max)
         {
         return true;
         } else
         {
         return false;
         }*/
        return true; // in 0 - ffff immer
    }

    boolean checkLast123456(byte[] request, byte functionCode)
    {
        //zu überprüfender Wert umformen
        int temp = byteToInt32(request[5]);
        boolean span = false;

        switch (functionCode)
        {
            case 0x01:
            case 0x02:
            {// kleinste Quantity of Inpus/Coils = 0x0001
                if (1 <= temp && temp <= 2000)
                {
                    span = true;
                }
                break;
            }
            case 0x03:
            case 0x04:
            {
                //kleinste Quantity of Registers = 0x0001
                if (1 <= temp && temp <= 125)
                {
                    span = true;
                }
                break;
            }

            case 0x05:
            {
                //1. output/register value wert = 0x0000
                span = true;
                break;
            }
            case 0x06:
            {
                //1. output/register value wert = 0x0000
                if (0 <= temp && temp <= 65535)
                {
                    span = true;
                }
                break;
            }
        }
        return span;
    }

    boolean check0F10Quant(byte[] request)
    {
        //zu überprüfender Wert umformen
        byte[] temp =
        {
            request[5]
        };
        ByteBuffer wrapped = ByteBuffer.wrap(temp);
        short num = wrapped.getShort();

        // min of # Outputs
        byte[] minO =
        {
            (byte) 0x00, (byte) 0x01
        };
        ByteBuffer wrappedO = ByteBuffer.wrap(minO);
        short min = wrappedO.getShort();

        byte functionCode = request[4];

        switch (functionCode)
        {
            case 0x0F:
            {// max of # Outputs
                byte[] maxO =
                {
                    (byte) 0x07, (byte) 0xB0
                };
                ByteBuffer wrappedOx = ByteBuffer.wrap(maxO);
                short max = wrappedOx.getShort();
                if (min < num && num < max)
                {
                    return true;
                } else
                {
                    return false;
                }
            }
            case 0x10:
            {// max of # registers
                byte[] maxR =
                {
                    (byte) 0x00, (byte) 0x7B
                };
                ByteBuffer wrappedOx = ByteBuffer.wrap(maxR);
                short max = wrappedOx.getShort();
                if (min < num && num < max)
                {
                    return true;
                } else
                {
                    return false;
                }
            }
        }
        return false;
    }
}
