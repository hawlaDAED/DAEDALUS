/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.configuration_module;

import java.net.InetAddress;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public interface IFPollingData
{

    public int getPollingInterval();
    
    public InetAddress getPollingIp(); 

}
