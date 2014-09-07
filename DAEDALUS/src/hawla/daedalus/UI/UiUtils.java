/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.UI;

import charvax.swing.JButton;
import charvax.swing.JMenuBar;

/**
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class UiUtils
{

    // nach Änderung der IP von short-Array auf String nicht mehr benötigt
//    static short[] disbandIPFromString(final String IP)
//    {
//
//        short[] IPasArray = new short[4];
//        String[] IPoktetts = IP.split("\\.");
//
//        for (int i = 0; i < IPoktetts.length; i++)
//        {
//            IPasArray[i] = Short.parseShort(IPoktetts[i]);
//
//        }
//
//        return IPasArray;
//    }
    static void shiftButtonState(final JButton[] buttons, final boolean activationState)
    {
        for (JButton button : buttons)
        {
            button.setVisible(activationState);
            button.setEnabled(activationState);
        }
    }

    static void shiftMenuBarSate(final JMenuBar menu, final boolean activateState)
    {

        menu.setVisible(activateState);
        menu.setEnabled(activateState);

    }

}
