/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.communicationlayer;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vanessa
 */
public class RequestHandler
{

    byte[] sigADU;
    byte[] errorADU;
    ModbusClient mbC;
    byte functionCode;

    Check check = new Check();

    public RequestHandler(ModbusClient mbC)
    {
        this.mbC = mbC;
    }

    public byte[] getSignificantADUParts()
    {
        return sigADU;
    }

    // geräteID - 2, pinID/addressID - 2, namespaceID - 1, counter - 1, flag - 1, value - 1
    public int handleADUData(byte[] request)
    {
        try
        {
            calcFunctionCode(request);

            if (request.length != 8) //der Request benötigt bestimmte Informationen, wenn diese nicht gegeben sind, dann kann auch keine fehlerfreie Verbindung hergestellt werden
            {
                throw new RequestException("Invalid request length.");
            } else
            {
                switch (functionCode)
                {   //Polling case 0x00: wird nie bei uns ankommen
                    case 0x01:
                    case 0x02:
                    case 0x03:
                    case 0x04:
                    case 0x05:
                    case 0x06:
                        if (fC123456(request) == 0)
                        {
                            throw new RequestException("An error occurred in creating the ADU.");
                        } else
                        {
                            break;
                        }
                    default:
                        createError((byte) 0x80, (byte) 0x01);
                        throw new RequestException("Exception Code: 01 - Unsupported Function Code.");

                }
            }
            return 1;
        } catch (Exception ex)
        {
            Logger.getLogger(ModbusClient.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    //für die aufgezählten functionCodes ist die ADU Größe - und somit Vorgehensweise - dieselbe
    public int fC123456(byte[] request) // functionCode 1, 2, 3, 4, 5, 6
    {
        //transID - 2, protID - 2, längefolgender - 2, unitID - 1, functCode - 1, startAdr - 2, #coils/values - 2
        sigADU = new byte[12];

        sigADU[4] = 0x00;  //Länge der noch folgenden bytes
        sigADU[5] = 0x06;
        sigADU[7] = functionCode;

        //System.out.println("*******" + sigADU[7] + "*******");
        // Überprüfung auf den Bereich, in dem sich die Adresse befinden muss
        if (check.checkAddress(request))
        {
            sigADU[8] = request[2]; //Address
            sigADU[9] = request[3];
        } else
        {
            createError(getErrorCode(functionCode), (byte) 0x02);
            return 0;
        }

        // Überprüfung auf Bereich der Quantity, die letzten Werte im Request bei FunctionCodes 01/02/03/04
        if (functionCode == 0x01 || functionCode == 0x02 || functionCode == 0x03 || functionCode == 0x04)
        {
            if (check.checkLast123456(request, functionCode))
            {
                sigADU[10] = (byte) 0x00;
                sigADU[11] = request[5];
            }
        } else if (functionCode == 0x05)   //wenn value =01  dann ff00 wenn =00 dann 0000 bei fC 05
        {

            if (request[7] == 0)
            {
                sigADU[10] = (byte) 0x00;
                sigADU[11] = (byte) 0x00;

            } else
            {
                sigADU[10] = (byte) 0xFF;
                sigADU[11] = (byte) 0x00;
            }
        } else if (functionCode == 0x06)
        {
            sigADU[10] = (byte) 0x00;
            sigADU[11] = request[7];
        } else
        {
            createError(getErrorCode(functionCode), (byte) 0x03);
            // Fehler
            return 0;
        }

        // Erfolg
        return 1;
    }

    public int fC0F10(byte[] request) // functionCode 0F, 10
    {
        //transID - 2, protID - 2, längefolgender - summe, unitID - 1, functCode - 1, startAdr - 2, # - 2, byteCount - #/8 // 2*#, value - #/8*1 byte // #*2 bytes

        // es sind sicher mindestens 13 bytes, so wie es in der Definition angegeben ist. Die Output Values werden übergeben, dh. sind im Request drin (TrID: 2, Device: 2, FuncCode: 1, StartAddr: 2, #Outputs: 2, OutpVal: X)
        // Was sicher im request ist wissen wir durch unser Protokoll die Differenz zuu dem was wir mindestens brauchen und zu dem was tatsächlich drin ist an bytes ist die anzahl der output values
        // die werden zu den bekannten 13 bytes dazugenommen
        sigADU = new byte[13 + request.length - 9]; //?

        short length = (short) (sigADU.length - 4);
        sigADU[4] = (byte) (length >> 8);  //Länge der noch folgenden bytes
        sigADU[5] = (byte) length;

        // Überprüfung auf den Bereich, in dem sich die Adresse befinden muss
        if (check.checkAddress(request))
        {
            sigADU[8] = request[2];
            sigADU[9] = request[3];
        } else
        {
            createError(getErrorCode(functionCode), (byte) 0x02);
            return 0;
        }
        // Überprüfung der Werte des Quantity of Outputs
        if (check.check0F10Quant(request))
        {
            sigADU[10] = (byte) 0x00;
            sigADU[11] = request[7];
        } else
        {
            createError(getErrorCode(functionCode), (byte) 0x03);
            return 0;
        }
        //byteCount ausrechnen und setzen //byte count = n = quant/8 falls rest 0
        byte byteCount = 0;
        byte[] quant =  //quantitiy benötigt zum ausrechnen
        {
            sigADU[10], sigADU[11]
        };
        ByteBuffer wrappedQ = ByteBuffer.wrap(quant);
        short byteC = wrappedQ.getShort();

        switch (functionCode)
        {
            case 0x0F:
            {
                if (byteC % 8 != 0)
                {
                    byteCount = (byte) (byteC / 8 + 1);
                } else
                {
                    byteCount = (byte) byteC;
                }
                break;
            }
            case 0x10:
            {
                byteCount = (byte) (2 * byteC);
                break;
            }
        }
        if (byteCount != 0)
        {
            sigADU[12] = byteCount;
        } else
        {
            createError(getErrorCode(functionCode), (byte) 0x03);
            return 0;
        }
        //der Rest der angegeben ist im Request sind die OutputValues, die hier gesetzt werden
        for (int i = 0; 13 + i <= sigADU.length; i++)
        {
            sigADU[13 + i] = request[8 + i];
        }
        return 1;
    }

    void createError(byte errorCode, byte exceptionCode)
    {
        errorADU = new byte[2];
        errorADU[0] = errorCode;
        errorADU[1] = exceptionCode;
    }

    public byte[] getErrorADU()
    {
        return errorADU;
    }

    byte getErrorCode(byte functionCode)
    {
        byte errorCode;
        errorCode = (byte) (functionCode + (byte) 0x80);
        return errorCode;
    }

    private byte calcFunctionCode(byte[] request) throws RequestException
    {
        byte flag = request[6];
        byte namespaceID = request[4];
        switch (flag)
        {   //Polling wird nie bei uns ankommen case 0x10: case 0x11: return 0x00;
            //Read?
            case 0x00:
                switch (namespaceID)
                {
                    //coils?
                    case 0x01:
                        functionCode = 0x01;
                        return functionCode;
                    //discrete inputs?
                    case 0x02:
                        functionCode = 0x02;
                        return functionCode;
                    //register input
                    case 0x03:
                        functionCode = 0x04;
                        return functionCode;
                    //holding registers
                    case 0x04:
                        functionCode = 0x03;
                        return functionCode;
                }
            //write?
            case 0x01:
                switch (namespaceID)
                {
                    //coils?
                    case 0x01:
                        functionCode = 0x05;
                        return functionCode;
                    //discrete inputs
                    case 0x02:  //!error
                        createError((byte) 0x80, (byte) 0x01);
                        throw new RequestException("Exception Code: 01 - Discrete inputs cannot be written.");
                    // register input
                    case 0x03:  //!error
                        createError((byte) 0x80, (byte) 0x01);
                        throw new RequestException("Exception Code: 01 - Register inputs cannot be written.");
                    //single registers
                    case 0x04:
                        functionCode = 0x06;
                        return functionCode;
                }
            default:
                createError((byte) 0x80, (byte) 0x01);
                throw new RequestException("Exception Code: 01 - Function Code not registered.");
        }
    }
}
