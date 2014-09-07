/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hawla.daedalus.UI;

import hawla.daedalus.configuration_module.DataManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ablaufrahmen-Klasse des UI-Interfaces, das alles bündelt und parallel zur
 * Hauptsoftware läuft
 *
 * @author Matthias Tafelmeier <s-mtafel@haw-landshut.de>
 */
public class UserInterface implements Runnable {

    private final DaedalusFrame majorFrame;

    /**
     *
     * Custom-Konstruktor zu UI
     *
     * @param dm Handle auf die Hauptdatenhaltung der Daedalus-Software
     */
    public UserInterface(final DataManager dm) {
        majorFrame = new DaedalusFrame("DAEDALUS v1.0", dm, this);
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            majorFrame.show();

            synchronized (majorFrame) {
                majorFrame.wait();

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, "UI has shut down due to the user commanding it to do so", ex);
        } catch (Exception ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, "general UI glitch", ex);
        }
    }

    void endUI() {
        majorFrame.setVisible(false);
        try {
            synchronized (majorFrame) {
                majorFrame.notify();
            }
        } catch (Exception ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, "glitch at ending software", ex);
        }
    }

}
