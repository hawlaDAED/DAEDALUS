/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.UI;

import charvax.swing.table.AbstractTableModel;
import hawla.daedalus.configuration_module.DataManager;
import java.util.HashMap;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class DeviceTableModel extends AbstractTableModel
{

    private final DataManager dm;

    /**
     * Modelschicht-Konstruktor, um UI von eigentlicher Datenhaltung der Daedalus-Software zu entkoppeln
     */
    public DeviceTableModel()
    {
        this.dm = null;
    }

    /**
     * Modelschicht-Konstruktor, um UI von eigentlicher Datenhaltung der Daedalus-Software zu entkoppeln
     *
     * @param dm Der Handle zur eigentlichen Datenhaltungsschicht der Software
     */
    public DeviceTableModel(DataManager dm)
    {
        this.dm = dm;
    }

    // Tabellenüberschriften für Gerätetabelle
    private final String[] columns = new String[]
    {
        "Geräte_ID", "Geräte_Namen", "IP"
    };
    // Spaltenkonstanten
    private final int DEV_ID_COL = 0;
    private final int DEV_NAME_COL = 1;
    private final int IP_COL = 2;

    @Override
    public String getColumnName(int column_)
    {
        return columns[column_];
    }

    @Override
    public int getRowCount()
    {
        return dm.getTransportData().getDevices().size();
    }

    @Override
    public int getColumnCount()
    {
        // 1. Spalte für Gerätenamen und 2. Spalte für zugehörige IPs
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex_, int columnIndex_)
    {

        final int BOUNDARY_VAL = 0;
        HashMap<Integer, String[]> devices = dm.getTransportData().getDevices();

        // so lange durch die Gerätenamen iterieren, bis die gewünscht Stelle laut rowIndex erreicht wird
        for (Integer deviceID : devices.keySet())
        {

            // die Gerätedatenstruktur in den Transportdaten durchiteriert bis zu der relevanten Stelle
            if (rowIndex_ == BOUNDARY_VAL)
            {

                // falls der Renderer sich in der ID-Spalte befindet
                if (columnIndex_ == DEV_ID_COL)
                {
                    return deviceID;
                } // falls der Renderer sich in der Gerätenamen-Spalte befindet
                else if (columnIndex_ == DEV_NAME_COL)
                {
                    return devices.get(deviceID)[0];

                } // falls der Renderer sich in der IP-Spalte befindet
                else if (columnIndex_ == IP_COL)
                {
                    return devices.get(deviceID)[1];

                } else
                {
                    throw new IllegalArgumentException("no such column provided for this table ");
                }

            } else
            {
                rowIndex_--;
            }
        }

        return null;

    }

    /**
     *
     * @param devName Gerätename
     * @param IPv4 IP-Adresse des neuen Gerätes
     * @return TRUE: Geräte noch nicht vorhanden und damit hinzugefügt oder FALSE: Gerät schon vorhanden und deswegen
     * nicht hinzugefügt
     */
    boolean expandDevices(final Integer devID, final String devName, final String IPv4)
    {
        if (dm.getTransportData().getDevices().keySet().contains(devID))
        {
            return false;
        } else

        {

            dm.getTransportData().getDevices().put(devID, new String[]
            {
                devName, IPv4
            });
            // update der JTable aufgrund des neuen Gerätes
            fireTableDataChanged();
            return true;
        }
    }

    /**
     * Methode für das UI, um vom Nutzer ausgewählte Verbindungen zu Geräten zu beenden.
     *
     * @return Boolean-Wert, der besagt, ob Entfernung der Geräte erfolgreich verlief.
     */
    boolean diminishDevices(final Integer devID)
    {

        dm.getTransportData().getDevices().remove(devID);
        return true;

    }

    public int getDEV_ID_COL()
    {
        return DEV_ID_COL;
    }

    public int getDEV_NAME_COL()
    {
        return DEV_NAME_COL;
    }

    /**
     *
     * @return Die IP-Spalten-Nummer der Gerätetabelle
     */
    public int getIP_COL()
    {
        return IP_COL;
    }

}
