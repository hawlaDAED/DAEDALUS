/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.connectionlayer;

//import hawla.daedalus.communicationlayer.ADU;
/**
 *
 * @author pho
 */
public interface Receivable
{

    public void receiveConfirmation(byte[] TcpIpADU);
}
