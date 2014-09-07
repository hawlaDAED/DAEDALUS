/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Daten-Container für das Transport-Layer-Modul
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class TransportData implements IFtransportDataRO

{

    // Struktur für die White-List der genutzten Device-IPs
    // String[2] besteht aus 1. Gerätenamen und 2. der IP
    // also stets devN_IP[0]:=<dev_name> und devN_IP[1]:=<IP>
    private final HashMap< Integer, String[]> devices;
    // Subnetzmaske des Netzwerks
    private byte subnetMask;
    // Modbus-Port
    private int port;

    /**
     *
     */
    public TransportData()
    {
        subnetMask = 0;
        devices = new HashMap<>();

    }

    public Set<String> getDeviceNames()
    {
        HashSet<String> deviceNames = new HashSet<>();

        for (Integer id : devices.keySet())
        {

            deviceNames.add(devices.get(id)[0]);

        }

        return deviceNames;

    }

    @Override
    public byte getSubnetMask()
    {
        return subnetMask;
    }

    public void setSubnetMask(byte subnetMask)
    {
        this.subnetMask = subnetMask;
    }

    void setPort(int port)
    {
        this.port = port;
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public HashMap<Integer, String[]> getDevices()
    {
        return devices;
    }

    void insertNovelDevice(final Integer ID, final String devName, final String IP)
    {

        String[] nameIP = new String[]
        {
            devName, IP
        };
        devices.put(ID, nameIP);

    }

}
