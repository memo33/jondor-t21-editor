/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package t21_editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Jon
 */
public class SettingsManager {

    private static Properties settings = new Properties();
    private static final String settingsFileName = "settings.xml";
    private static final String settingsFileComment = "T21 Editor Settings";
    
    public static final String APPDIR = "appDir";
    public static final String USERPLUGINS = "userPlugins";
    public static final String CURRENTDIR = "currentDir";

    public static boolean readSettings() {
        try {
            File settingsFile = new File(settingsFileName);
            if(!settingsFile.exists())
                return false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(settingsFile));
            settings.loadFromXML(bis);
            return true;
        } catch(IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    public static void writeSettings() {
        try {
            File settingsFile = new File(settingsFileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(settingsFile));
            settings.storeToXML(bos, settingsFileComment);
        } catch(IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    public static String getSetting(String name) {
        return settings.getProperty(name);
    }

    public static boolean putSetting(String name, String value) {
        try {
            settings.setProperty(name, value);
            return true;
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }
}
