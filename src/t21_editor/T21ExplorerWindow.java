/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * T21ExplorerWindow.java
 *
 * Created on Jan 31, 2012, 11:39:27 AM
 */
package t21_editor;

import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import jwb.jdbpf.DBPFFile;
import jwb.jdbpf.DBPFTGI;
import jwb.jdbpf.properties.*;
import jwb.jdbpf.types.*;
import jwb.jdbpf.util.DBPFUtil;

/**
 *
 * @author Jon
 */
public class T21ExplorerWindow extends javax.swing.JInternalFrame {

    public T21ExplorerTableModel t21ExplorerBacking;

    /** Creates new form T21ExplorerWindow */
    public T21ExplorerWindow(File file, MainAppWindow.T21Loader loader) {
        this.dbpfFile = DBPFFile.Reader.read(file);
        this.t21ExplorerBacking = new T21ExplorerTableModel(true);

        Collection<Long> indicies = dbpfFile.getSubFiles();
        int x = 1, count = indicies.size();
        for(Long index : indicies) {
            DBPFTGI tgi = dbpfFile.getTGIForIndex(index);
            try {
                if(tgi.compareTGI(DBPFTGI.EXEMPLAR)) {
                    DBPFExemplar exemplar = dbpfFile.converter.createExemplar(tgi);
                    outFiles.add(exemplar);
                    DBPFLongProperty longProp = (DBPFLongProperty) exemplar.getProperty(0x00000010L);
                    if(longProp != null) {
                        if(longProp.getValue() == 0x21L) {
                            ExemplarEncapsulator exEncap = new ExemplarEncapsulator(exemplar, dbpfFile);
                            this.t21ExplorerBacking.backingStore.add(exEncap);
                        }
                    }
                } else {
                    outFiles.add(dbpfFile.converter.createType(index, true));
                }
            } catch (Exception e) {
                System.err.println("Could not load entry! Filename: " + file.getName() + " TGI: " + tgi);
                e.printStackTrace(System.err);
            }
            loader.updateProgress(Math.round(((float)x)/count * 100));
            x++;
        }
        this.setTitle(file.getName());
        this.t21ExplorerBacking.fireTableDataChanged();

        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        t21ExplorerScrollPane = new javax.swing.JScrollPane();
        t21ExplorerTable = new javax.swing.JTable();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameDeactivated(evt);
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        t21ExplorerTable.setModel(t21ExplorerBacking);
        t21ExplorerTable.setFillsViewportHeight(true);
        t21ExplorerTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t21ExplorerTable.getTableHeader().setReorderingAllowed(false);
        t21ExplorerTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                t21ExplorerTableMouseClicked(evt);
            }
        });
        t21ExplorerTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                t21ExplorerTableKeyReleased(evt);
            }
        });
        t21ExplorerScrollPane.setViewportView(t21ExplorerTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(t21ExplorerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(t21ExplorerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void t21ExplorerTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_t21ExplorerTableMouseClicked
        if(evt.getClickCount() == 2) {
            java.awt.Point p = evt.getPoint();
            int rowIndex = t21ExplorerTable.rowAtPoint(p);
            T21EditWindow t21Edit;
            DBPFExemplar ex;
            if(rowIndex != -1) {
                if(rowIndex >= t21ExplorerBacking.backingStore.size()) {
                    ex = new DBPFExemplar(DBPFTGI.EXEMPLAR.modifyTGI(-1, 0x89AC5643L, 0), true, DBPFUtil.FORMAT_BINARY);
                    this.setModified(true);
                } else {
                    ex = t21ExplorerBacking.backingStore.get(rowIndex).ex;
                }
                t21Edit = subFiles.get(ex);
                if(t21Edit == null) {
                    t21Edit = new T21EditWindow(this, ex);
                    this.getParent().add(t21Edit);
                    subFiles.put(ex, t21Edit);
                    t21Edit.setVisible(true);
                } else {
                    t21Edit.setVisible(true);
                    t21Edit.moveToFront();
                    try {
                        t21Edit.setSelected(true);
                    } catch (PropertyVetoException e) {
                        //Bad Java, Select my stupid frame
                    }
                }
            }
        }
    }//GEN-LAST:event_t21ExplorerTableMouseClicked

    private void t21ExplorerTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_t21ExplorerTableKeyReleased
            if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
            int selectedRow = t21ExplorerTable.getSelectedRow();
            if(selectedRow < 0 || selectedRow > t21ExplorerTable.getRowCount() - 1)
                return;
            int choice = JOptionPane.showInternalConfirmDialog(this, "Are you sure you want to delete this exemplar?", "Delete Exemplar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice == JOptionPane.YES_OPTION)
            {
                DBPFExemplar ex = t21ExplorerBacking.backingStore.remove(selectedRow).ex;
                if(outFiles.contains(ex))
                    outFiles.remove(ex);
                if(subFiles.containsKey(ex)) {
                    T21EditWindow t21 = subFiles.remove(ex);
                    t21.setVisible(false);
                    this.getParent().remove(t21);
                }
            }
        }
    }//GEN-LAST:event_t21ExplorerTableKeyReleased

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        T21Editor.app.setActiveT21(this);
    }//GEN-LAST:event_formInternalFrameActivated

    private void formInternalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameDeactivated
        T21Editor.app.setActiveT21(null);
    }//GEN-LAST:event_formInternalFrameDeactivated

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        if(modified) {
            int choice = JOptionPane.showInternalConfirmDialog(this, "Save changes before closing this file?", "Close File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice == JOptionPane.CANCEL_OPTION) {
                return;
            } else if(choice == JOptionPane.YES_OPTION) {
                SaveState ss = saveFile(null);
                if(ss == SaveState.FAILED) {
                    JOptionPane.showInternalMessageDialog(this, "Error saving the file, please try saving to another filename.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if(ss == SaveState.CANCELLED) {
                    return;
                }
            }
        }
        for(T21EditWindow t21 : subFiles.values()) {
            t21.dispose();
            this.getParent().remove(t21);
        }
        T21Editor.app.closeFile(this.dbpfFile.getFile());
    }//GEN-LAST:event_formInternalFrameClosing

    private void setModified(boolean modified) {
        this.modified = modified;
        setTitle(dbpfFile.getName() + (this.modified ? " *" : ""));
    }

    public boolean isModified() {
        return modified;
    }

    public File getFile() {
        return dbpfFile.getFile();
    }
    
    public boolean forceSave() {
        for(T21EditWindow t21 : subFiles.values()) {
            if(t21.isModified()) {
                t21.triggerSave();
            }
        }
        if(DBPFFile.Writer.write(dbpfFile, outFiles)) {
            return true;
        }
        return false;
    }
    
    public SaveState saveFile(File newFile) {
        boolean flag = false;
        for(T21EditWindow t21 : subFiles.values()) {
            if(t21.isModified()) {
                flag = true;
                break;
            }
        }
        if(flag) {
            int choice = JOptionPane.showInternalConfirmDialog(this, "This file has open exemplars with unsaved changes, save them?", "Save Exemplars", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice == JOptionPane.YES_OPTION)
            {
                for(T21EditWindow t21 : subFiles.values()) {
                    if(t21.isModified()) {
                        t21.triggerSave();
                    }
                }
            } else if(choice == JOptionPane.CANCEL_OPTION) {
                return SaveState.CANCELLED;
            }
        }
        if(newFile == null) {
            if(DBPFFile.Writer.write(dbpfFile, outFiles)) {
                this.setModified(false);
                return SaveState.SAVED;
            }
        } else {
            if(DBPFFile.Writer.write(dbpfFile, outFiles, newFile)) {
                this.setModified(false);
                this.dbpfFile = DBPFFile.Reader.read(newFile);
                return SaveState.SAVED;
            }
        }
        return SaveState.FAILED;
    }
    
    public void updateExemplar(DBPFExemplar ex, boolean close) {
        setModified(true);
        if(!outFiles.contains(ex)) {
            outFiles.add(ex);
            t21ExplorerBacking.backingStore.add(new ExemplarEncapsulator(ex, dbpfFile));
            if(t21ExplorerTable.getModel() instanceof AbstractTableModel) {
                ((AbstractTableModel)t21ExplorerTable.getModel()).fireTableDataChanged();
            }
        }
        
        if(close)
            closeEditor(ex);
    }
    
    public void closeEditor(DBPFExemplar ex) {
        if(subFiles.containsKey(ex)) {
            T21EditWindow t21 = subFiles.remove(ex);
            t21.dispose();
            this.getParent().remove(t21);
            this.moveToFront();
            try {
                this.setSelected(true);
            } catch (PropertyVetoException e) {
                //Bad Java, Select my stupid frame
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane t21ExplorerScrollPane;
    private javax.swing.JTable t21ExplorerTable;
    // End of variables declaration//GEN-END:variables

    public static enum SaveState {
        SAVED,
        FAILED,
        CANCELLED;
    }
    
    private DBPFFile dbpfFile;
    private boolean modified = false;
    private HashMap<DBPFExemplar, T21EditWindow> subFiles = new HashMap<>();
    private ArrayList<DBPFType> outFiles = new ArrayList<>();
}
