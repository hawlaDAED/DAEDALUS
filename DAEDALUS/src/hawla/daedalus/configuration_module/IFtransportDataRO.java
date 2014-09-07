/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.util.HashMap;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public interface IFtransportDataRO
{

    HashMap<Integer, String[]> getDevices();

    int getPort();

    byte getSubnetMask();

}
