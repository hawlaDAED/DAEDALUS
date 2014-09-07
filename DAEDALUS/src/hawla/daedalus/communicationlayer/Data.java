
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.communicationlayer;

import hawla.daedalus.configuration_module.IFclientDataRO;

/**
 *
 * @author Vanessa
 */
public class Data
{
    IFclientDataRO cDro;
    
    public Data (IFclientDataRO cdRO)
    {
        cDro = cdRO;
    }
}
