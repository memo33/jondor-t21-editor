/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package t21_editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import java.io.File;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.SwingWorker;
import javax.swing.UnsupportedLookAndFeelException;
import jwb.jdbpf.DBPFFile;
import jwb.jdbpf.DBPFTGI;

/**
 *
 * @author Jon
 */
public class T21Editor {

    public static File appDirFile, appPluginFile, userPluginFile; //appSKUFile, appLocaleFile, 

    public static MainAppWindow app;
    public static SettingsDialog settingsDiag;

    private static TreeSet<File> compileFilesList() {
        TreeSet<File> files = new TreeSet<>(new SimCityFileComparator());

        File simcity1 = new File(appDirFile, "SimCity_1.dat");
        if(simcity1.isFile() && DBPFFile.Reader.checkFileType(simcity1)) {
            files.add(simcity1);
        }

        File simcity2 = new File(appDirFile, "SimCity_2.dat");
        if(simcity2.isFile() && DBPFFile.Reader.checkFileType(simcity2)) {
            files.add(simcity2);
        }

        File simcity3 = new File(appDirFile, "SimCity_3.dat");
        if(simcity3.isFile() && DBPFFile.Reader.checkFileType(simcity3)) {
            files.add(simcity3);
        }

        File simcity4 = new File(appDirFile, "SimCity_4.dat");
        if(simcity4.isFile() && DBPFFile.Reader.checkFileType(simcity4)) {
            files.add(simcity4);
        }

        File simcity5 = new File(appDirFile, "SimCity_5.dat");
        if(simcity5.isFile() && DBPFFile.Reader.checkFileType(simcity5)) {
            files.add(simcity5);
        }
        
        scanPlugins(appPluginFile, files);
        scanPlugins(userPluginFile, files);

        return files;
    }

    private static void scanPlugins(File currentFile, TreeSet<File> files) {
        if(currentFile.isFile() && DBPFFile.Reader.checkFileType(currentFile)) {
            files.add(currentFile);
            //System.out.println("*" + currentFile.toString());
        } else if(currentFile.isDirectory()) {
            for(File child : currentFile.listFiles()) {
                scanPlugins(child, files);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    java.util.logging.Logger.getLogger(T21Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
                }
                boolean settingsRead = SettingsManager.readSettings();
                
                app = new MainAppWindow();
                app.setVisible(true);
                settingsDiag = new SettingsDialog(app, true);

                if(!settingsRead) {
                    settingsDiag.forceSettings();
                }
                String appDir = SettingsManager.getSetting(SettingsManager.APPDIR),
                       userPlugins = SettingsManager.getSetting(SettingsManager.USERPLUGINS);
                
                if(appDir == null || userPlugins == null) {
                    settingsDiag.forceSettings();
                    appDir = SettingsManager.getSetting(SettingsManager.APPDIR);
                    userPlugins = SettingsManager.getSetting(SettingsManager.USERPLUGINS);
                }
                
                appDirFile = new File(appDir);
                //appSKUFile = new File(appDirFile, "Sku_Data");
                //appLocaleFile = new File(appDirFile, SettingsManager.getSetting("appLocale"));
                appPluginFile = new File(appDirFile, "Plugins");
                userPluginFile = new File(userPlugins);

                TGILoader loader = new TGILoader();

                loader.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        switch (pce.getPropertyName()) {
                            case "progress":
                                app.progBar.setIndeterminate(false);
                                app.progBar.setValue((Integer)pce.getNewValue());
                                app.progBar.setString(pce.getNewValue().toString() + "%");
                                break;
                            case "fileName":
                                app.progBar.setIndeterminate(false);
                                app.filenameLabel.setText("Loading " + pce.getNewValue() + "...");
                                app.progBar.setValue(0);
                                app.progBar.setString("0%");
                                break;
                        }
                    }
                    
                });
                loader.execute();
            }
        });
    }

    private static class TGILoader extends SwingWorker<Void, Void> {
        
        private String fileName = "";
        
        @Override
        public Void doInBackground() {
            app.progContainer.setVisible(true);
            TreeSet<File> files = compileFilesList();

            for(File filename : files) {
                try {
                    this.setFileName(filename.getName());
                    //System.out.println(filename);
                    DBPFFile dbpfFile = DBPFFile.Reader.read(filename);
                    Collection<DBPFTGI> tgis = dbpfFile.getTGIs();
                    int x = 1, count = tgis.size();
                    for(DBPFTGI tgi : tgis) {
                        TGIManager.putFile(tgi, dbpfFile);
                        this.setProgress(Math.round(((float)x)/count * 100));
                        x++;
                    }
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
            return null;
        }

        @Override
        public void done() {
            app.progContainer.setVisible(false);
        }
        
        public String getFileName() {
            return this.fileName;
        }
        
        private void setFileName(String fileName) {
            String oldFile = this.fileName;
            this.fileName = fileName;
            this.firePropertyChange("fileName", oldFile, fileName);
        }
    }
    
    private static final class SimCityFileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            File file1 = f1, file2 = f2;
            if(f1.equals(new File(f1.getPath().toUpperCase()))) {
                file1 = new File(f1.getPath().toUpperCase());
                file2 = new File(f2.getPath().toUpperCase());
            }
            FileLocation fl1 = FileLocation.NOWHERE, fl2 = FileLocation.NOWHERE;
            File temp1 = file1, temp2 = file2;
            while((temp1 = temp1.getParentFile()) != null) {
                if(temp1.equals(userPluginFile)) {
                    fl1 = FileLocation.USER_PLUGIN_DIR;
                    break;
                }
                if(temp1.equals(appPluginFile)) {
                    fl1 = FileLocation.APP_PLUGIN_DIR;
                    break;
                }
/*
                if(temp1.equals(appSKUFile)) {
                    fl1 = FileLocation.APP_SKU_DIR;
                    break;
                }
                if(temp1.equals(appLocaleFile)) {
                    fl1 = FileLocation.APP_LOCALE_DIR;
                    break;
                }
*/
                if(temp1.equals(appDirFile) && !file1.getPath().substring(appDirFile.getPath().length() + 1).contains(File.separator)) {
                    fl1 = FileLocation.APP_DIR;
                    break;
                }
            }
            while((temp2 = temp2.getParentFile()) != null) {
                if(temp2.equals(userPluginFile)) {
                    fl2 = FileLocation.USER_PLUGIN_DIR;
                    break;
                }
                if(temp2.equals(appPluginFile)) {
                    fl2 = FileLocation.APP_PLUGIN_DIR;
                    break;
                }
/*
                if(temp2.equals(appSKUFile)) {
                    fl1 = FileLocation.APP_SKU_DIR;
                    break;
                }
                if(temp2.equals(appLocaleFile)) {
                    fl1 = FileLocation.APP_LOCALE_DIR;
                    break;
                }
*/
                if(temp2.equals(appDirFile) && !file2.getPath().substring(appDirFile.getPath().length() + 1).contains(File.separator)) {
                    fl2 = FileLocation.APP_DIR;
                    break;
                }
            }
            
            if(fl1.priority < fl2.priority)
                return -1;
            else if(fl1.priority > fl2.priority)
                return 1;
            else {
                boolean dat1 = false, dat2 = false;
                if(file1.getName().contains("."))
                    dat1 = file1.getName().substring(file1.getName().lastIndexOf(".")).equalsIgnoreCase(".dat");
                if(file2.getName().contains("."))
                    dat2 = file2.getName().substring(file2.getName().lastIndexOf(".")).equalsIgnoreCase(".dat");

                if(!dat1 && dat2)
                    return -1;
                else if(dat1 && !dat2)
                    return 1;
                else {
                    File lcd = null;
                    temp1 = file1;
                    out: do {
                        while((temp1 = temp1.getParentFile()) != null) {
                            temp2 = file2;
                            while((temp2 = temp2.getParentFile()) != null) {
                                if(temp1.equals(temp2)) {
                                    lcd = temp1;
                                    break out;
                                }
                            }
                        }
                    } while(false);
                    if(lcd == null)
                        return 0;
                    String s1 = file1.getPath().substring(lcd.getPath().length()),
                           s2 = file2.getPath().substring(lcd.getPath().length());
                    int count1 = 0, count2 = 0, index1 = 0, index2 = 0;

                    while((index1 = s1.indexOf(File.separator, index1)) != -1) {
                        count1++;
                        index1++;
                    }
                    while((index2 = s2.indexOf(File.separator, index2)) != -1) {
                        count2++;
                        index2++;
                    }

                    if(count1 == 1 && count2 != 1)
                        return -1;
                    else if(count1 != 1 && count2 == 1)
                        return 1;
                    else 
                        return file1.getPath().compareTo(file2.getPath());
                }
            }
        }
        
        private enum FileLocation {
            APP_DIR(0),
            APP_LOCALE_DIR(1),
            APP_SKU_DIR(2),
            APP_PLUGIN_DIR(3),
            USER_PLUGIN_DIR(4),
            NOWHERE(Integer.MAX_VALUE);
            
            private int priority;
            
            FileLocation(int priority) {
                this.priority = priority;
            }
        }
    }
}
