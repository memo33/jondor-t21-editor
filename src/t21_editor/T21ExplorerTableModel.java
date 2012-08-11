/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package t21_editor;

import java.util.ArrayList;
import jwb.jdbpf.properties.*;
import jwb.jdbpf.util.DBPFUtil;

/**
 *
 * @author Jon
 */
public class T21ExplorerTableModel extends javax.swing.table.AbstractTableModel{

    public ArrayList<ExemplarEncapsulator> backingStore = new ArrayList<>();
    
    private boolean singleFileModel = false;

    public T21ExplorerTableModel(boolean singleFileModel) {
        this.singleFileModel = singleFileModel;
    }
    
    public T21ExplorerTableModel() {
        this.singleFileModel = false;
    }
    
    
    @Override
    public Object getValueAt(int row, int col){
        if(row >= backingStore.size()) {
            switch(col) {
                case 0:
                    return "Add new...";
                default:
                    return "";
            }
        } else {
            ExemplarEncapsulator exEncap = backingStore.get(row);
            switch(col) {
                case 0:
                    DBPFStringProperty stringProp = (DBPFStringProperty) exEncap.ex.getProperty(0x20L);
                    return (stringProp != null ? stringProp.getValue() : "---");
                case 1:
                    DBPFLongProperty longProp = (DBPFLongProperty) exEncap.ex.getProperty(0xC9A5A1BEL);
                    return (longProp != null ? "0x" + DBPFUtil.toHex(longProp.getValue(), 8) : "---");
                case 2:
                    return "0x" + DBPFUtil.toHex(exEncap.ex.getIID(), 8);
                case 3:
                    return exEncap.filename.getName();
                default:
                    return "---";
            }
        }
    }

    @Override
    public int getColumnCount(){
        if(singleFileModel)
            return 3;
        else
            return 4;
    }

    @Override
    public String getColumnName(int col){
        switch(col) {
            case 0:
                return "Exemplar Name";
            case 1:
                return "Network Tile";
            case 2:
                return "IID";
            case 3:
                return "File Name";
            default:
                return "---";
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch(col) {
            case 0:
                return String.class;
            case 1:
                return Long.class;
            case 2:
                return Long.class;
            case 3:
                return String.class;
            default:
                return Object.class;
        }
    }

    @Override
    public int getRowCount(){
        if(singleFileModel)
            return backingStore.size() + 1;
        else
            return backingStore.size();
    }
}
