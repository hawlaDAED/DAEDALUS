/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.util.Set;

/**
 *
 * Daten-Container für das Client-Modul
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class ClientData implements IFclientDataRO
{

    // ID des Modbus-Protokolss
    private final short MODBUS_PROT_ID = 0x0000;
    // serieller Device-Identifizierer (da keine serielle Verbindung genutzt, Default-Ausmaskierung mit 0XFF)
    private final byte unitIdentifier = (byte) 0xFF;
    // Zeiger auf Gerätenamen
    private final Set<String> deviceNames;

    public ClientData(final Set<String> initDev)
    {
        // Referenz auf einen Device-Namen-Satz festhalten
        deviceNames = initDev;

    }

    @Override
    public final short getMODBUS_PROT_ID()
    {
        return MODBUS_PROT_ID;
    }

    @Override
    public final byte getUnitIdentifier()
    {
        return unitIdentifier;
    }

    @Override
    public final Set<String> getDeviceNames()
    {
        return deviceNames;
    }

}
