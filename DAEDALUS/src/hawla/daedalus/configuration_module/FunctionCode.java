/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

/**
 *
 * Aufzählungstyp der Modbus-Protokoll-Funktions-Code-Konstanten
 *
 * Vorgesehen für das Client-Modul, das mit diesen Funktions-Codes arbeitet
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
// Konstanten für Funktions-Codes des Modbus-Protokolls
public enum FunctionCode
{

    ReadDiscreteInputs((short) 0x02),
    ReadCoils((short) 0x01),
    WriteSingleCoil((short) 0x05),
    WriteMultipleCoils((short) 0x0F),
    ReadInputRegister((short) 0x04),
    ReadHoldingRegisters((short) 0x03),
    WriteSingleRegister((short) 0x06),
    WriteMultipleRegisters((short) 0x10),
    Read_WriteMultipleRegisters((short) 0x17),
    MaskWriteRegister((short) 0x16),
    ReadFIFOqueue((short) 0x18),
    ReadFileRecord((short) 0x14),
    WriteFileRecord((short) 0x15),
    EncapsulatedInterfaceTransport((short) 0x2B);

    private final short functionCode;

    FunctionCode(final short functionCode)
    {

        this.functionCode = functionCode;
    }

    public short code()
    {
        return functionCode;
    }

}
