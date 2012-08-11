/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package t21_editor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import jwb.jdbpf.DBPFFile;
import jwb.jdbpf.DBPFTGI;

/**
 *
 * @author Jon
 */
public class TGIManager {
    
    private static final Map<DBPFTGI, DBPFFile> tgiMap = Collections.synchronizedMap(new HashMap<DBPFTGI, DBPFFile>());
    
    private static final List<TGIListener> listenerList = Collections.synchronizedList(new LinkedList<TGIListener>());
    
    public static DBPFFile getFile(DBPFTGI tgi) {
        return tgiMap.get(tgi);
    }
    
    public static Map<DBPFTGI, DBPFFile> getFiles(DBPFTGI checkTGI) {
        HashMap<DBPFTGI, DBPFFile> map = new HashMap<>();
        synchronized(tgiMap) {
            for(DBPFTGI tgi : tgiMap.keySet())
                if(tgi.compareTGI(checkTGI))
                    map.put(tgi, tgiMap.get(tgi));
        }
        return map;
    }
    
    public static DBPFFile putFile(DBPFTGI tgi, DBPFFile file) {
        DBPFFile oldFile = tgiMap.put(tgi, file);
        LinkedList<TGIListener> listeners;
        synchronized(listenerList) {
            listeners = new LinkedList<>(listenerList);
        }
        for(TGIListener listener : listeners)
            for(DBPFTGI subTGI : listener.tgis)
                if(tgi.compareTGI(subTGI))
                    listener.tgiAdded(tgi, file);
        return oldFile;
    }
    
    public static Set<DBPFTGI> getKeys() {
        return tgiMap.keySet();
    }
    
    public static Collection<DBPFFile> getValues() {
        return tgiMap.values();
    }
    
    public static void addTGIListener(TGIListener listener) {
        synchronized(listenerList) {
            new ListenerWorker(listener).execute();
            listenerList.add(listener);
        }
    }
    
    public static void removeTGIListener(TGIListener listener) {
        synchronized(listenerList) {
            listenerList.remove(listener);
        }
    }
    
    public static abstract class TGIListener {
        private LinkedList<DBPFTGI> tgis;

        public TGIListener(DBPFTGI tgi) {
            this.tgis = new LinkedList<>();
            this.tgis.add(tgi);
        }
        
        public TGIListener(Collection<DBPFTGI> tgis) {
            this.tgis = new LinkedList<>(tgis);
        }
        
        public abstract void tgiAdded(DBPFTGI tgi, DBPFFile file);
    }
    
    private static class ListenerWorker extends SwingWorker<Void, Void> {
    
        TGIListener listener;

        public ListenerWorker(TGIListener listener) {
            this.listener = listener;
        }
        
        @Override
        protected Void doInBackground() {
            for(DBPFTGI tgi : listener.tgis) {
                Map<DBPFTGI, DBPFFile> files = getFiles(tgi);
                for(DBPFTGI subTGI : files.keySet())
                    listener.tgiAdded(subTGI, files.get(subTGI));
            }
            return null;
        }
    }
}
