/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * T21EditWindow.java
 *
 * Created on Sep 13, 2011, 10:59:32 PM
 */
package t21_editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;
import jwb.jdbpf.DBPFFile;
import jwb.jdbpf.DBPFTGI;
import jwb.jdbpf.types.DBPFExemplar;
import jwb.jdbpf.properties.*;
import jwb.jdbpf.types.DBPFFSH;
import jwb.jdbpf.types.DBPFS3D;
import jwb.jdbpf.util.DBPFUtil;


/**
 *
 * @author Jon
 */
public class T21EditWindow extends javax.swing.JInternalFrame {

    /** Creates new form T21EditWindow */
    public T21EditWindow(T21ExplorerWindow editParent, DBPFExemplar ex) {
        this.editParent = editParent;
        this.ex = ex;
        this.pfEditModel = new T21PropFloraEditModel();
        
        initComponents();
        addActionHandlers();
        
        DefaultCellEditor dce = new DefaultCellEditor(typeColEditor);
        dce.setClickCountToStart(2);
        propTable.getColumnModel().getColumn(0).setCellEditor(dce);
        dce = new DefaultCellEditor(lodColEditor);
        dce.setClickCountToStart(2);
        propTable.getColumnModel().getColumn(1).setCellEditor(dce);
        TableCellEditor tce = new PropIIDEditor();
        propIIDEditorText.addActionListener((ActionListener)tce);
        propIIDEditorText.setActionCommand(propIIDEditorTextAction);
        propIIDEditorButton.addActionListener((ActionListener)tce);
        propIIDEditorButton.setActionCommand(propIIDEditorButtonAction);
        propTable.getColumnModel().getColumn(4).setCellEditor(tce);
        
        propTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                if(!evt.getValueIsAdjusting()) {
                    int selectedRow = propTable.getSelectedRow();
                    if(selectedRow < 0 || selectedRow > propFloraList.size() - 1)
                        currentPFE = null;
                    else
                        currentPFE = propFloraList.get(selectedRow);
                    updatePropPos();
                }
            }
        });
        
        initData();
        
    }
    private void addActionHandlers() {
        ModifiedActionHandler mah = new ModifiedActionHandler();
        PatternActionHandler pah = new PatternActionHandler();
        DrawingActionHandler dah = new DrawingActionHandler();
        for(int x=0;x<16;x++) {
            patternButton[x].addActionListener(mah);
            patternButton[x].addActionListener(pah);
            zonesCheck[x].addActionListener(mah);
        }
        patternSize3.addActionListener(pah);
        patternSize4.addActionListener(pah);
        wealthNone.addActionListener(mah);
        wealthLow.addActionListener(mah);
        wealthMed.addActionListener(mah);
        wealthHigh.addActionListener(mah);
        flipsCombo.addActionListener(mah);
        rotsNorth.addActionListener(mah);
        rotsEast.addActionListener(mah);
        rotsSouth.addActionListener(mah);
        rotsWest.addActionListener(mah);
        boundingCheck.addActionListener(dah);
        vertexCheck.addActionListener(dah);
    }
    
    private void initData() {
        if(tgiListener != null)
            TGIManager.removeTGIListener(tgiListener);
        this.name = "Untitled";
        this.iid = 0;
        cancelCheck.setSelected(false);
        this.tile = 0;
        this.minSlope = 0;
        this.maxSlope = 64;
        patternSize4.setSelected(true);
        for(int x=0;x<16;x++) {
            patternButton[x].setSelected(false);
            zonesCheck[x].setSelected(false);
        }
        wealthNone.setSelected(false);
        wealthLow.setSelected(false);
        wealthMed.setSelected(false);
        wealthHigh.setSelected(false);
        flipsCombo.setSelectedIndex(0);
        rotsNorth.setSelected(false);
        rotsEast.setSelected(false);
        rotsSouth.setSelected(false);
        rotsWest.setSelected(false);
        xPosText.setText("");
        yPosText.setText("");
        zPosText.setText("");
        xMinText.setText("");
        xMaxText.setText("");
        zMinText.setText("");
        zMaxText.setText("");
        rotCombo.setSelectedIndex(0);
        
        DBPFLongProperty typeProp = (DBPFLongProperty) ex.getProperty(EX_TYPE);
        if(typeProp != null) {
            if(typeProp.getValue() == 0x21L) {
                DBPFStringProperty nameProp = (DBPFStringProperty) ex.getProperty(EX_NAME);
                if(nameProp != null)
                    this.name = nameProp.getValue();
                nameText.setText(this.name);
                setTitle(this.name);

                this.iid = ex.getIID();
                iidText.setText("0x" + DBPFUtil.toHex(this.iid, 8));

                if(ex.getNumProperties() < 13) {
                    cancelCheck.setSelected(false);
                    cancelCheck.doClick();
                }

                DBPFLongProperty tileProp = (DBPFLongProperty) ex.getProperty(TILE_IID);
                if(tileProp != null)
                    this.tile = tileProp.getValue();
                tileText.setText("0x" + DBPFUtil.toHex(this.tile, 8));

                DBPFFloatProperty minSlopeProp = (DBPFFloatProperty) ex.getProperty(MIN_SLOPE);
                if(minSlopeProp != null)
                    minSlope = minSlopeProp.getValue();
                minSlopeText.setText(Float.toString(minSlope));

                DBPFFloatProperty maxSlopeProp = (DBPFFloatProperty) ex.getProperty(MAX_SLOPE);
                if(maxSlopeProp != null)
                    maxSlope = maxSlopeProp.getValue();
                maxSlopeText.setText(Float.toString(maxSlope));

                DBPFLongProperty patternSizeProp = (DBPFLongProperty) ex.getProperty(PATTERN_SIZE);
                if(patternSizeProp != null && patternSizeProp.getValue() == 3)
                    patternSize3.doClick();
                else
                    patternSize4.doClick();

                DBPFLongProperty patternProp = (DBPFLongProperty) ex.getProperty(PATTERN);
                if(patternProp != null) {
                    long[] pattern = {patternProp.getValue(0), patternProp.getValue(1),
                                      patternProp.getValue(2), patternProp.getValue(3)};

                    for(int x=0;x<4;x++)
                        for(int y=0;y<4;y++)
                            patternButton[4*x + y].setSelected((pattern[x] & (1 << y)) != 0);
                    if(patternSize4.isSelected())
                        patternHexLabel.setText("Hex: 0x" + DBPFUtil.toHex(pattern[0], 1) +
                                                ", 0x" + DBPFUtil.toHex(pattern[1], 1) +
                                                ", 0x" + DBPFUtil.toHex(pattern[2], 1) +
                                                ", 0x" + DBPFUtil.toHex(pattern[3], 1));
                    else
                        patternHexLabel.setText("Hex: 0x" + DBPFUtil.toHex(pattern[0] & 0x7, 1) +
                                                ", 0x" + DBPFUtil.toHex(pattern[1] & 0x7, 1) +
                                                ", 0x" + DBPFUtil.toHex(pattern[2] & 0x7, 1));
                }

                DBPFLongProperty zonesProp = (DBPFLongProperty) ex.getProperty(ZONES);
                if(zonesProp != null) {
                    int count = zonesProp.getCount();

                    for(int x=0; x<count; x++) {
                        int zone = zonesProp.getValue(x).intValue();
                        if(zone >= 0 && zone <= 15)
                            zonesCheck[zone].setSelected(true);
                    }
                }

                DBPFLongProperty wealthProp = (DBPFLongProperty) ex.getProperty(WEALTHS);
                if(wealthProp != null) {
                    int count = wealthProp.getCount();

                    for(int x=0; x<count; x++) {
                        long wealth = wealthProp.getValue(x);

                        switch((int)wealth) {
                            case 0:
                                wealthNone.setSelected(true);
                                break;
                            case 1:
                                wealthLow.setSelected(true);
                                break;
                            case 2:
                                wealthMed.setSelected(true);
                                break;
                            case 3:
                                wealthHigh.setSelected(true);
                                break;
                        }
                    }
                }

                DBPFLongProperty flipsProp = (DBPFLongProperty) ex.getProperty(FLIPS);
                if(flipsProp != null) {
                    long flips = flipsProp.getValue();
                    flipsCombo.setSelectedIndex((int)flips);
                }

                DBPFLongProperty rotsProp = (DBPFLongProperty) ex.getProperty(ROTS);
                if(rotsProp != null) {
                    long rots = rotsProp.getValue();

                    rotsNorth.setSelected((rots & 1) != 0);
                    rotsEast.setSelected((rots & 2) != 0);
                    rotsSouth.setSelected((rots & 4) != 0);
                    rotsWest.setSelected((rots & 8) != 0);
                }

                long objID = OBJECTS;
                DBPFLongProperty objProp;
                for(PropFloraEncap pfe : propFloraList) {
                    if(pfe.propListener != null)
                        TGIManager.removeTGIListener(pfe.propListener);
                }
                propFloraList.clear();
                while((objProp = (DBPFLongProperty) ex.getProperty(objID)) != null) {
                    PropFloraEncap pfe = new PropFloraEncap();
                    pfe.type = typeEnum.byCode(objProp.getValue(0));
                    if(pfe.type == null) {
                        objID++;
                        continue;
                    }
                    pfe.lod = lodEnum.byCode(objProp.getValue(1) & 0xF0);
                    pfe.lod = (pfe.lod == null) ? lodEnum.ALL : pfe.lod;
                    pfe.flag = (objProp.getValue(1) & 0x0FL) != 0;
                    pfe.rot = objProp.getValue(2);
                    pfe.rot = (pfe.rot > 3 || pfe.rot < 0) ? 0 : pfe.rot;
                    pfe.xPos = objProp.getValue(3);
                    pfe.yPos = objProp.getValue(4);
                    pfe.zPos = objProp.getValue(5);
                    pfe.xMin = objProp.getValue(6);
                    pfe.zMin = objProp.getValue(7);
                    pfe.xMax = objProp.getValue(8);
                    pfe.zMax = objProp.getValue(9);
                    pfe.objID = objProp.getValue(11);
                    for(int x=12; x<objProp.getCount(); x++) {
                        pfe.IIDs.add(objProp.getValue(x));
                    }
                    pfe.propListener = new PropListener(pfe.IIDs.get(0), pfe);
                    TGIManager.addTGIListener(pfe.propListener);
                    propFloraList.add(pfe);
                    objID++;
                }
            }
        }
        if(propTable.getModel() instanceof AbstractTableModel) {
            ((AbstractTableModel)propTable.getModel()).fireTableDataChanged();
        }
        if(propTable.getCellEditor() != null)
            propTable.getCellEditor().cancelCellEditing();
        ArrayList<DBPFTGI> tgis = new ArrayList<>(4);
        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile));
        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile + 4));
        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile + 14));
        tgiListener = new ImageListener(tgis);
        TGIManager.addTGIListener(tgiListener);
        propPanel.repaint();
        setModified(false);
    }
    
    public boolean triggerSave() {
        boolean success = saveData();
        if(!success) {
            JOptionPane.showInternalMessageDialog(this, "Could not save data, please check for bad IIDs or slopes.", "Bad Data", JOptionPane.ERROR_MESSAGE);
        } else {
            editParent.updateExemplar(ex, false);
            setModified(false);
        }
        return success;
    }
    
    private boolean saveData() {
        Matcher m = hexPattern.matcher(iidText.getText());
        if(m.matches()) {
            StringBuilder sb = new StringBuilder("0x");
            for(int x=m.group(2).length();x<8;x++)
                sb.append("0");
            sb.append(m.group(2));
            iidText.setText(sb.toString());
        } else {
            return false;
        }
        
        m = hexPattern.matcher(tileText.getText());
        if(m.matches()) {
            StringBuilder sb = new StringBuilder("0x");
            for(int x=m.group(2).length();x<8;x++)
                sb.append("0");
            sb.append(m.group(2));
            tileText.setText(sb.toString());
        } else {
            return false;
        }
        
        try {
            Float.parseFloat(minSlopeText.getText());
            Float.parseFloat(maxSlopeText.getText());
        } catch (NumberFormatException e) {
            return false;
        }
        
        ex.clearProperties();
        DBPFLongProperty typeProp = new DBPFLongProperty(DBPFPropertyType.UINT32, Long.valueOf(0x21L), EX_TYPE);
        ex.addProperty(typeProp);
        
        DBPFStringProperty nameProp = new DBPFStringProperty(this.name, EX_NAME);
        ex.addProperty(nameProp);

        DBPFLongProperty iidProp = new DBPFLongProperty(DBPFPropertyType.UINT32, Long.valueOf(iidText.getText().substring(2), 16), EX_IID);
        ex.addProperty(iidProp);
        ex.setTGI(ex.getTGI().modifyTGI(-1, 0x89AC5643L, Long.parseLong(iidText.getText().substring(2), 16)));

        DBPFLongProperty verProp = new DBPFLongProperty(DBPFPropertyType.UINT8, Long.valueOf(2), VERSION);
        ex.addProperty(verProp);

        if(!cancelCheck.isSelected()) {
            DBPFLongProperty tileProp = new DBPFLongProperty(DBPFPropertyType.UINT32, Long.valueOf(tileText.getText().substring(2), 16), TILE_IID);
            ex.addProperty(tileProp);

            DBPFFloatProperty minSlopeProp = new DBPFFloatProperty(Float.parseFloat(minSlopeText.getText()), MIN_SLOPE);
            ex.addProperty(minSlopeProp);

            DBPFFloatProperty maxSlopeProp = new DBPFFloatProperty(Float.parseFloat(maxSlopeText.getText()), MAX_SLOPE);
            ex.addProperty(maxSlopeProp);

            int patternSize = 4;
            if(patternSize3.isSelected())
                patternSize = 3;
            DBPFLongProperty patternSizeProp = new DBPFLongProperty(DBPFPropertyType.UINT8, Long.valueOf(patternSize), PATTERN_SIZE);
            ex.addProperty(patternSizeProp);

            Long[] pattern = {0L, 0L, 0L, 0L};
            for(int x=0;x<16;x++)
                pattern[x/4] += patternButton[x].isSelected() ? (1 << (x % 4)) : 0;
            DBPFLongProperty patternProp = new DBPFLongProperty(DBPFPropertyType.UINT8, pattern, PATTERN);
            ex.addProperty(patternProp);

            ArrayList<Long> zones = new ArrayList<>(16);
            for(int x=0;x<16;x++)
                if(zonesCheck[x].isSelected())
                    zones.add(Long.valueOf(x));
            if(zones.isEmpty()) {
                zones.add(Long.valueOf(0));
                zonesCheck[0].setSelected(true);
            }
            DBPFLongProperty zonesProp = new DBPFLongProperty(DBPFPropertyType.UINT8, zones.toArray(new Long[0]), ZONES);
            ex.addProperty(zonesProp);

            ArrayList<Long> wealths = new ArrayList<>(4);
            if(wealthNone.isSelected()) wealths.add(Long.valueOf(0));
            if(wealthLow.isSelected()) wealths.add(Long.valueOf(1));
            if(wealthMed.isSelected()) wealths.add(Long.valueOf(2));
            if(wealthHigh.isSelected()) wealths.add(Long.valueOf(3));
            if(wealths.isEmpty()) {
                wealths.add(Long.valueOf(0));
                wealthNone.setSelected(true);
            }
            DBPFLongProperty wealthsProp = new DBPFLongProperty(DBPFPropertyType.UINT8, wealths.toArray(new Long[0]), WEALTHS);
            ex.addProperty(wealthsProp);

            DBPFLongProperty flipsProp = new DBPFLongProperty(DBPFPropertyType.UINT8, Long.valueOf(flipsCombo.getSelectedIndex()), FLIPS);
            ex.addProperty(flipsProp);

            int rots = 0;
            rots += rotsNorth.isSelected() ? 1 : 0;
            rots += rotsEast.isSelected() ? 2 : 0;
            rots += rotsSouth.isSelected() ? 4 : 0;
            rots += rotsWest.isSelected() ? 8 : 0;
            DBPFLongProperty rotsProp = new DBPFLongProperty(DBPFPropertyType.UINT8, Long.valueOf(rots), ROTS);
            ex.addProperty(rotsProp);

            long objID = OBJECTS;
            DBPFLongProperty objProp;
            ArrayList<Long> values = new ArrayList<>(20);
            
            for(PropFloraEncap pfe : propFloraList) {
                values.clear();
                values.add(pfe.type.code);
                values.add(pfe.lod.code + (pfe.flag ? 1 : 0));
                values.add(pfe.rot);
                values.add(pfe.xPos);
                values.add(pfe.yPos);
                values.add(pfe.zPos);
                values.add(pfe.xMin);
                values.add(pfe.zMin);
                values.add(pfe.xMax);
                values.add(pfe.zMax);
                values.add(Long.valueOf(0));
                values.add(pfe.objID);
                if(pfe.type == typeEnum.PROP)
                    for(Long propIID : pfe.IIDs)
                        values.add(propIID);
                else
                    values.add(pfe.IIDs.get(0));
                
                objProp = new DBPFLongProperty(DBPFPropertyType.UINT32, values.toArray(new Long[0]), objID);
                ex.addProperty(objProp);
                objID++;
            }
        }
        setModified(false);
        return true;
    }

    void setModified(boolean modified) {
        this.modified = modified;
        this.setTitle(this.name + (this.modified ? " *" : ""));
    }

    public boolean isModified() {
        return modified;
    }
    
    private void updatePropPos() {
        if(currentPFE != null) {
            boolean isModified = this.modified;
            if(formatCombo.getSelectedIndex() == 0) {
                xPosText.setText(Float.toString((float)currentPFE.xPos / 0x10000));
                yPosText.setText(Float.toString((float)((int) currentPFE.yPos) / 0x10000));  // signed 32-bit integers
                zPosText.setText(Float.toString((float)currentPFE.zPos / 0x10000));
                xMinText.setText(Float.toString((float)currentPFE.xMin / 0x10000));
                xMaxText.setText(Float.toString((float)currentPFE.xMax / 0x10000));
                zMinText.setText(Float.toString((float)currentPFE.zMin / 0x10000));
                zMaxText.setText(Float.toString((float)currentPFE.zMax / 0x10000));
            } else {
                xPosText.setText("0x" + DBPFUtil.toHex(currentPFE.xPos, 8));
                yPosText.setText("0x" + DBPFUtil.toHex(currentPFE.yPos, 8));
                zPosText.setText("0x" + DBPFUtil.toHex(currentPFE.zPos, 8));
                xMinText.setText("0x" + DBPFUtil.toHex(currentPFE.xMin, 8));
                xMaxText.setText("0x" + DBPFUtil.toHex(currentPFE.xMax, 8));
                zMinText.setText("0x" + DBPFUtil.toHex(currentPFE.zMin, 8));
                zMaxText.setText("0x" + DBPFUtil.toHex(currentPFE.zMax, 8));
            }
            rotCombo.setSelectedIndex((int)currentPFE.rot);
            if(currentPFE.ex != null) {
                DBPFProperty prop = currentPFE.ex.getProperty(EX_NAME);
                if(prop instanceof DBPFStringProperty) {
                    DBPFStringProperty stringProp = (DBPFStringProperty)prop;
                    propNameLabel.setText(stringProp.getValue());
                    propNameLabel.setToolTipText(stringProp.getValue());
                }
            }
            this.setModified(isModified);
        } else {
            xPosText.setText("");
            yPosText.setText("");
            zPosText.setText("");
            xMinText.setText("");
            xMaxText.setText("");
            zMinText.setText("");
            zMaxText.setText("");
            rotCombo.setSelectedIndex(0);
            propNameLabel.setText("");
            propNameLabel.setToolTipText("");
        }
        propPanel.repaint();
    }
    
    private void updateXPos(int index) {
        if(currentPFE == null) {
            xPosText.setText("");
            xMinText.setText("");
            xMaxText.setText("");
            return;
        }
        long newX;
        String text;
        switch (index) {
            case 0:
                long deltaMin = currentPFE.xPos - currentPFE.xMin;
                long deltaMax = currentPFE.xMax - currentPFE.xPos;
                newX = currentPFE.xPos;
                text = xPosText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newX = (long) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newX = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newX = Math.min(0xFFFFF, Math.max(newX, 0x0));
                currentPFE.xPos = newX;
                currentPFE.xMin = Math.min(0xFFFFF, Math.max(newX - deltaMin, 0x0));
                currentPFE.xMax = Math.min(0xFFFFF, Math.max(newX + deltaMax, 0x0));
                updatePropPos();
                return;
            case -1:
                newX = currentPFE.xMin;
                text = xMinText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newX = (long) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newX = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newX = Math.min(0xFFFFF, Math.max(newX, 0x0));
                currentPFE.xMin = newX;
                updatePropPos();
                return;
            case 1:
                newX = currentPFE.xMax;
                text = xMaxText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newX = (int) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newX = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newX = Math.min(0xFFFFF, Math.max(newX, 0x0));
                currentPFE.xMax = newX;
                updatePropPos();
                return;
            default:
                return;
        }
    }
    
    private void updateYPos() {
        if(currentPFE == null) {
            yPosText.setText("");
            return;
        }
        long newY = currentPFE.yPos;
        String text = yPosText.getText();
        try {
            if(formatCombo.getSelectedIndex() == 0) {
                newY = (long) (Float.parseFloat(text) * 0x10000);
            } else {
                Matcher m = hexPattern.matcher(text);
                if(m.matches()) {
                    newY = Long.parseLong(m.group(2), 16);
                }
            }
            setModified(true);
        } catch(NumberFormatException e) {
            //Do Something?
        }
        // newY = Math.min(0xFFFFF, Math.max(newY, 0x0));
        newY = (long)((int) newY);  // this bounds the value to signed 32-bit integers
        currentPFE.yPos = newY;
        updatePropPos();
    }
    
    private void updateZPos(int index) {
        if(currentPFE == null) {
            zPosText.setText("");
            zMinText.setText("");
            zMaxText.setText("");
            return;
        }
        long newZ;
        String text;
        switch (index) {
            case 0:
                long deltaMin = currentPFE.zPos - currentPFE.zMin;
                long deltaMax = currentPFE.zMax - currentPFE.zPos;
                newZ = currentPFE.zPos;
                text = zPosText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newZ = (long) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newZ = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newZ = Math.min(0xFFFFF, Math.max(newZ, 0x0));
                currentPFE.zPos = newZ;
                currentPFE.zMin = Math.min(0xFFFFF, Math.max(newZ - deltaMin, 0x0));
                currentPFE.zMax = Math.min(0xFFFFF, Math.max(newZ + deltaMax, 0x0));
                updatePropPos();
                return;
            case -1:
                newZ = currentPFE.zMin;
                text = zMinText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newZ = (long) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newZ = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newZ = Math.min(0xFFFFF, Math.max(newZ, 0x0));
                currentPFE.zMin = newZ;
                updatePropPos();
                return;
            case 1:
                newZ = currentPFE.zMax;
                text = zMaxText.getText();
                try {
                    if(formatCombo.getSelectedIndex() == 0) {
                        newZ = (long) (Float.parseFloat(text) * 0x10000);
                    } else {
                        Matcher m = hexPattern.matcher(text);
                        if(m.matches()) {
                            newZ = Long.parseLong(m.group(2), 16);
                        }
                    }
                    setModified(true);
                } catch(NumberFormatException e) {
                    //Do Something?
                }
                newZ = Math.min(0xFFFFF, Math.max(newZ, 0x0));
                currentPFE.zMax = newZ;
                updatePropPos();
                return;
            default:
                return;
        }
    }
    
    private void retrieveTileImage() {

        long fshIID = tile + 4;
        DBPFFile file = null;
        DBPFTGI tgi = DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, fshIID);
        DBPFFSH fsh = null;
        Image image = null;
        if((file = TGIManager.getFile(tgi)) != null) {
            fsh = file.converter.createFSH(tgi);
            if(fsh != null) {
                image = fsh.getImage();
                if(image != null)
                    if(image.getWidth(this) != 128 || image.getHeight(this) != 128)
                        image = null;
            }
        }
        if(fshIID != tileImageIID) {
            if(file == null || fsh == null || image == null) {
                fshIID += 10; //tile + 14
                tgi = tgi.modifyTGI(-1, -1, fshIID);
                if((file = TGIManager.getFile(tgi)) != null) {
                    fsh = file.converter.createFSH(tgi);
                    if(fsh != null) {
                        image = fsh.getImage();
                        if(image != null)
                            if(image.getWidth(this) != 128 || image.getHeight(this) != 128)
                                image = null;
                    }
                }
            }
            if(fshIID != tileImageIID) {
                if(file == null || fsh == null || image == null) {
                    fshIID -= 14; // tile + 0
                    tgi = tgi.modifyTGI(-1, -1, fshIID);
                    if((file = TGIManager.getFile(tgi)) != null) {
                        fsh = file.converter.createFSH(tgi);
                        if(fsh != null) {
                            image = fsh.getImage();
                            if(image != null)
                                if(image.getWidth(this) != 128 || image.getHeight(this) != 128)
                                    image = null;
                        }
                    }
                }
            }
        }
        if(image != null) {
            tileImage = image;
            tileImageIID = fshIID;
            propPanel.repaint();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patternSizeButtonGroup = new javax.swing.ButtonGroup();
        typeColEditor = new javax.swing.JComboBox();
        lodColEditor = new javax.swing.JComboBox();
        propIIDEditor = new javax.swing.JPanel();
        propIIDEditorText = new javax.swing.JTextField();
        propIIDEditorButton = new javax.swing.JButton();
        t21Tabs = new javax.swing.JTabbedPane();
        t21LayoutPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        iidLabel = new javax.swing.JLabel();
        iidText = new javax.swing.JTextField();
        cancelCheck = new javax.swing.JCheckBox();
        tileLabel = new javax.swing.JLabel();
        tileText = new javax.swing.JTextField();
        minSlopeLabel = new javax.swing.JLabel();
        minSlopeText = new javax.swing.JTextField();
        maxSlopeLabel = new javax.swing.JLabel();
        maxSlopeText = new javax.swing.JTextField();
        patternPanel = new javax.swing.JPanel();
        patternSizeLabel = new javax.swing.JLabel();
        patternSize3 = new javax.swing.JRadioButton();
        patternSize4 = new javax.swing.JRadioButton();
        javax.swing.JToggleButton patternButton0 = new javax.swing.JToggleButton();
        patternButton[0] = patternButton0;
        javax.swing.JToggleButton patternButton1 = new javax.swing.JToggleButton();
        patternButton[1] = patternButton1;
        javax.swing.JToggleButton patternButton2 = new javax.swing.JToggleButton();
        patternButton[2] = patternButton2;
        javax.swing.JToggleButton patternButton3 = new javax.swing.JToggleButton();
        patternButton[3] = patternButton3;
        javax.swing.JToggleButton patternButton4 = new javax.swing.JToggleButton();
        patternButton[4] = patternButton4;
        javax.swing.JToggleButton patternButton5 = new javax.swing.JToggleButton();
        patternButton[5] = patternButton5;
        javax.swing.JToggleButton patternButton6 = new javax.swing.JToggleButton();
        patternButton[6] = patternButton6;
        javax.swing.JToggleButton patternButton7 = new javax.swing.JToggleButton();
        patternButton[7] = patternButton7;
        javax.swing.JToggleButton patternButton8 = new javax.swing.JToggleButton();
        patternButton[8] = patternButton8;
        javax.swing.JToggleButton patternButton9 = new javax.swing.JToggleButton();
        patternButton[9] = patternButton9;
        javax.swing.JToggleButton patternButton10 = new javax.swing.JToggleButton();
        patternButton[10] = patternButton10;
        javax.swing.JToggleButton patternButton11 = new javax.swing.JToggleButton();
        patternButton[11] = patternButton11;
        javax.swing.JToggleButton patternButton12 = new javax.swing.JToggleButton();
        patternButton[12] = patternButton12;
        javax.swing.JToggleButton patternButton13 = new javax.swing.JToggleButton();
        patternButton[13] = patternButton13;
        javax.swing.JToggleButton patternButton14 = new javax.swing.JToggleButton();
        patternButton[14] = patternButton14;
        javax.swing.JToggleButton patternButton15 = new javax.swing.JToggleButton();
        patternButton[15] = patternButton15;
        patternHexLabel = new javax.swing.JLabel();
        zonesPanel = new javax.swing.JPanel();
        zonesLabel = new javax.swing.JLabel();
        javax.swing.JCheckBox zonesNone = new javax.swing.JCheckBox();
        zonesCheck[0] = zonesNone;
        javax.swing.JCheckBox zonesRLow = new javax.swing.JCheckBox();
        zonesCheck[1] = zonesRLow;
        javax.swing.JCheckBox zonesRMed = new javax.swing.JCheckBox();
        zonesCheck[2] = zonesRMed;
        javax.swing.JCheckBox zonesRHigh = new javax.swing.JCheckBox();
        zonesCheck[3] = zonesRHigh;
        javax.swing.JCheckBox zonesCLow = new javax.swing.JCheckBox();
        zonesCheck[4] = zonesCLow;
        javax.swing.JCheckBox zonesCMed = new javax.swing.JCheckBox();
        zonesCheck[5] = zonesCMed;
        javax.swing.JCheckBox zonesCHigh = new javax.swing.JCheckBox();
        zonesCheck[6] = zonesCHigh;
        javax.swing.JCheckBox zonesILow = new javax.swing.JCheckBox();
        zonesCheck[7] = zonesILow;
        javax.swing.JCheckBox zonesIMed = new javax.swing.JCheckBox();
        zonesCheck[8] = zonesIMed;
        javax.swing.JCheckBox zonesIHigh = new javax.swing.JCheckBox();
        zonesCheck[9] = zonesIHigh;
        javax.swing.JCheckBox zonesMilitary = new javax.swing.JCheckBox();
        zonesCheck[10] = zonesMilitary;
        javax.swing.JCheckBox zonesSeaport = new javax.swing.JCheckBox();
        zonesCheck[12] = zonesSeaport;
        javax.swing.JCheckBox zonesAirport = new javax.swing.JCheckBox();
        zonesCheck[11] = zonesAirport;
        javax.swing.JCheckBox zonesSpaceport = new javax.swing.JCheckBox();
        zonesCheck[13] = zonesSpaceport;
        javax.swing.JCheckBox zonesLandfill = new javax.swing.JCheckBox();
        zonesCheck[14] = zonesLandfill;
        javax.swing.JCheckBox zonesPlopped = new javax.swing.JCheckBox();
        zonesCheck[15] = zonesPlopped;
        wealthLabel = new javax.swing.JLabel();
        wealthNone = new javax.swing.JCheckBox();
        wealthLow = new javax.swing.JCheckBox();
        wealthMed = new javax.swing.JCheckBox();
        wealthHigh = new javax.swing.JCheckBox();
        flipsLabel = new javax.swing.JLabel();
        flipsCombo = new javax.swing.JComboBox();
        rotsLabel = new javax.swing.JLabel();
        rotsNorth = new javax.swing.JCheckBox();
        rotsEast = new javax.swing.JCheckBox();
        rotsSouth = new javax.swing.JCheckBox();
        rotsWest = new javax.swing.JCheckBox();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 159), new java.awt.Dimension(0, 159), new java.awt.Dimension(0, 159));
        t21PropPanel = new javax.swing.JPanel();
        propTableScroller = new javax.swing.JScrollPane();
        propTable = new javax.swing.JTable();
        upButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        delButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        formatCombo = new javax.swing.JComboBox();
        propNameLabel = new javax.swing.JLabel();
        xPosLabel = new javax.swing.JLabel();
        xPosText = new javax.swing.JTextField();
        yPosLabel = new javax.swing.JLabel();
        yPosText = new javax.swing.JTextField();
        zPosLabel = new javax.swing.JLabel();
        zPosText = new javax.swing.JTextField();
        xMinLabel = new javax.swing.JLabel();
        xMinText = new javax.swing.JTextField();
        xMaxLabel = new javax.swing.JLabel();
        xMaxText = new javax.swing.JTextField();
        zMinLabel = new javax.swing.JLabel();
        zMinText = new javax.swing.JTextField();
        zMaxLabel = new javax.swing.JLabel();
        zMaxText = new javax.swing.JTextField();
        rotLabel = new javax.swing.JLabel();
        rotCombo = new javax.swing.JComboBox();
        propBoundsButton = new javax.swing.JButton();
        propPanel = new PropPanel();
        boundingCheck = new javax.swing.JCheckBox();
        vertexCheck = new javax.swing.JCheckBox();
        buttonBar = new javax.swing.JPanel();
        saveCloseButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        revertButton = new javax.swing.JButton();

        typeColEditor.setModel(new javax.swing.DefaultComboBoxModel(typeEnum.values()));
        typeColEditor.setBorder(null);

        lodColEditor.setModel(new javax.swing.DefaultComboBoxModel(lodEnum.values()));
        lodColEditor.setBorder(null);

        propIIDEditor.setLayout(new java.awt.BorderLayout());

        propIIDEditorText.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        propIIDEditor.add(propIIDEditorText, java.awt.BorderLayout.CENTER);

        propIIDEditorButton.setText("...");
        propIIDEditorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        propIIDEditor.add(propIIDEditorButton, java.awt.BorderLayout.LINE_END);

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        t21Tabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        nameLabel.setText("Name:");
        nameLabel.setToolTipText("T21 Exemplar Name");

        nameText.setToolTipText("T21 Exemplar Name");
        nameText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextActionPerformed(evt);
            }
        });
        nameText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameTextFocusLost(evt);
            }
        });

        iidLabel.setText("IID:");
        iidLabel.setToolTipText("T21 Exemplar IID");

        iidText.setToolTipText("T21 Exemplar IID");
        iidText.setInputVerifier(hexVer);
        iidText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iidTextActionPerformed(evt);
            }
        });

        cancelCheck.setText("Cancellation Entry");
        cancelCheck.setToolTipText("Creates a T21 Exemplar with a Name and IID only for overriding a T21 in another file.");

        tileLabel.setText("Network Tile:");
        tileLabel.setToolTipText("Network Tile IID this T21 affects.");

        tileText.setToolTipText("Network Tile IID this T21 affects.");
        tileText.setInputVerifier(hexVer);
        tileText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tileTextActionPerformed(evt);
            }
        });

        minSlopeLabel.setText("Min. Slope:");
        minSlopeLabel.setToolTipText("");

        minSlopeText.setInputVerifier(floatVer);
        minSlopeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minSlopeTextActionPerformed(evt);
            }
        });

        maxSlopeLabel.setText("Max. Slope:");

        maxSlopeText.setInputVerifier(floatVer);
        maxSlopeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxSlopeTextActionPerformed(evt);
            }
        });

        patternPanel.setPreferredSize(new java.awt.Dimension(148, 179));

        patternSizeLabel.setText("Pattern Size:");

        patternSizeButtonGroup.add(patternSize3);
        patternSize3.setText("3");
        patternSize3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patternSize3ActionPerformed(evt);
            }
        });

        patternSizeButtonGroup.add(patternSize4);
        patternSize4.setSelected(true);
        patternSize4.setText("4");
        patternSize4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patternSize4ActionPerformed(evt);
            }
        });

        patternHexLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        patternHexLabel.setText("Hex:");

        javax.swing.GroupLayout patternPanelLayout = new javax.swing.GroupLayout(patternPanel);
        patternPanel.setLayout(patternPanelLayout);
        patternPanelLayout.setHorizontalGroup(
            patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patternPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patternHexLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addGroup(patternPanelLayout.createSequentialGroup()
                        .addComponent(patternSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(patternSize3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(patternSize4))
                    .addGroup(patternPanelLayout.createSequentialGroup()
                        .addComponent(patternButton0, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(patternPanelLayout.createSequentialGroup()
                        .addComponent(patternButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(patternPanelLayout.createSequentialGroup()
                        .addComponent(patternButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(patternPanelLayout.createSequentialGroup()
                        .addComponent(patternButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(patternButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        patternPanelLayout.setVerticalGroup(
            patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patternPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(patternSizeLabel)
                    .addComponent(patternSize3)
                    .addComponent(patternSize4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(patternButton0, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(patternButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(patternButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(patternButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patternButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patternButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patternButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(patternHexLabel)
                .addContainerGap())
        );

        zonesLabel.setText("Zones:");

        zonesNone.setText("None");

        zonesRLow.setText("R-Low");

        zonesRMed.setText("R-Med");

        zonesRHigh.setText("R-High");

        zonesCLow.setText("C-Low");

        zonesCMed.setText("C-Med");

        zonesCHigh.setText("C-High");

        zonesILow.setText("I-Ag");

        zonesIMed.setText("I-Med");

        zonesIHigh.setText("I-High");

        zonesMilitary.setText("Military");

        zonesSeaport.setText("Seaport");

        zonesAirport.setText("Airport");

        zonesSpaceport.setText("Spaceport");

        zonesLandfill.setText("Landfill");

        zonesPlopped.setText("Plopped");

        javax.swing.GroupLayout zonesPanelLayout = new javax.swing.GroupLayout(zonesPanel);
        zonesPanel.setLayout(zonesPanelLayout);
        zonesPanelLayout.setHorizontalGroup(
            zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zonesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zonesLabel)
                    .addGroup(zonesPanelLayout.createSequentialGroup()
                        .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zonesSeaport)
                            .addComponent(zonesIMed)
                            .addComponent(zonesNone)
                            .addComponent(zonesCLow))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zonesSpaceport)
                            .addComponent(zonesIHigh)
                            .addComponent(zonesCMed)
                            .addComponent(zonesRLow))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zonesRMed)
                            .addComponent(zonesCHigh)
                            .addComponent(zonesMilitary)
                            .addComponent(zonesLandfill))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zonesILow)
                            .addComponent(zonesRHigh)
                            .addComponent(zonesAirport)
                            .addComponent(zonesPlopped))))
                .addContainerGap())
        );
        zonesPanelLayout.setVerticalGroup(
            zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zonesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(zonesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zonesNone)
                    .addComponent(zonesRLow)
                    .addComponent(zonesRMed)
                    .addComponent(zonesRHigh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zonesCLow)
                    .addComponent(zonesCMed)
                    .addComponent(zonesCHigh)
                    .addComponent(zonesILow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zonesIMed)
                    .addComponent(zonesIHigh)
                    .addComponent(zonesMilitary)
                    .addComponent(zonesAirport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(zonesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zonesSeaport, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zonesSpaceport, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zonesLandfill, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zonesPlopped, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        wealthLabel.setText("Wealths:");

        wealthNone.setText("None");

        wealthLow.setText("$");

        wealthMed.setText("$$");

        wealthHigh.setText("$$$");

        flipsLabel.setText("Flips:");

        flipsCombo.setMaximumRowCount(3);
        flipsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Both", "Flipped Only", "Non-flipped Only" }));

        rotsLabel.setText("Rotations:");
        rotsLabel.setToolTipText("Rotation of the tile itself (camera direction)");

        rotsNorth.setText("0-North");
        rotsNorth.setToolTipText("Rotation of the tile itself (camera direction)");

        rotsEast.setText("1-East");
        rotsEast.setToolTipText("Rotation of the tile itself (camera direction)");

        rotsSouth.setText("2-South");
        rotsSouth.setToolTipText("Rotation of the tile itself (camera direction)");

        rotsWest.setText("3-West");
        rotsWest.setToolTipText("Rotation of the tile itself (camera direction)");

        javax.swing.GroupLayout t21LayoutPanelLayout = new javax.swing.GroupLayout(t21LayoutPanel);
        t21LayoutPanel.setLayout(t21LayoutPanelLayout);
        t21LayoutPanelLayout.setHorizontalGroup(
            t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                        .addComponent(tileLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tileText, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(minSlopeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minSlopeText, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(maxSlopeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxSlopeText, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cancelCheck)
                    .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameText, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(iidLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iidText, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                        .addComponent(patternPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(zonesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                        .addComponent(wealthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wealthNone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wealthLow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wealthMed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wealthHigh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(flipsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flipsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                        .addComponent(rotsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotsNorth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotsEast)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotsSouth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotsWest)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        t21LayoutPanelLayout.setVerticalGroup(
            t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(t21LayoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iidLabel)
                    .addComponent(iidText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tileLabel)
                    .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(minSlopeLabel)
                        .addComponent(maxSlopeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(minSlopeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(maxSlopeLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zonesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wealthLabel)
                    .addComponent(wealthNone)
                    .addComponent(wealthLow)
                    .addComponent(wealthMed)
                    .addComponent(wealthHigh)
                    .addComponent(flipsLabel)
                    .addComponent(flipsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(t21LayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotsLabel)
                    .addComponent(rotsNorth)
                    .addComponent(rotsEast)
                    .addComponent(rotsSouth)
                    .addComponent(rotsWest))
                .addContainerGap(157, Short.MAX_VALUE))
        );

        t21Tabs.addTab("Layout  Properties", t21LayoutPanel);

        propTable.setModel(pfEditModel);
        propTable.setFillsViewportHeight(true);
        propTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        propTable.getTableHeader().setReorderingAllowed(false);
        propTableScroller.setViewportView(propTable);
        propTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        upButton.setText("^");
        upButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        addButton.setText("+");
        addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        delButton.setText("-");
        delButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        delButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delButtonActionPerformed(evt);
            }
        });

        downButton.setText("v");
        downButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        formatCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Decimal", "Hex" }));
        formatCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatComboActionPerformed(evt);
            }
        });

        propNameLabel.setMaximumSize(new java.awt.Dimension(190, 14));
        propNameLabel.setMinimumSize(new java.awt.Dimension(190, 14));
        propNameLabel.setPreferredSize(new java.awt.Dimension(190, 14));

        xPosLabel.setText("X Pos:");

        xPosText.setInputVerifier(posVer);
        xPosText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xPosTextActionPerformed(evt);
            }
        });

        yPosLabel.setText("Y Pos:");

        yPosText.setInputVerifier(posVer);
        yPosText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yPosTextActionPerformed(evt);
            }
        });

        zPosLabel.setText("Z Pos:");

        zPosText.setInputVerifier(posVer);
        zPosText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zPosTextActionPerformed(evt);
            }
        });

        xMinLabel.setText("X Min:");

        xMinText.setInputVerifier(posVer);
        xMinText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xMinTextActionPerformed(evt);
            }
        });

        xMaxLabel.setText("X Max:");

        xMaxText.setInputVerifier(posVer);
        xMaxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xMaxTextActionPerformed(evt);
            }
        });

        zMinLabel.setText("Z Min:");

        zMinText.setInputVerifier(posVer);
        zMinText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zMinTextActionPerformed(evt);
            }
        });

        zMaxLabel.setText("Z Max:");

        zMaxText.setInputVerifier(posVer);
        zMaxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zMaxTextActionPerformed(evt);
            }
        });

        rotLabel.setText("Rot:");
        rotLabel.setToolTipText("Rotation of the prop face (stage direction)");

        rotCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "South (0)", "West (1)", "North (2)", "East (3)" }));
        rotCombo.setToolTipText("Rotation of the prop face (stage direction)");
        rotCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotComboActionPerformed(evt);
            }
        });

        propBoundsButton.setText("Get Prop Bounds");
        propBoundsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propBoundsButtonActionPerformed(evt);
            }
        });

        propPanel.setBackground(new java.awt.Color(0, 0, 0));
        propPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        propPanel.setFocusable(false);
        propPanel.setPreferredSize(new java.awt.Dimension(260, 260));

        javax.swing.GroupLayout propPanelLayout = new javax.swing.GroupLayout(propPanel);
        propPanel.setLayout(propPanelLayout);
        propPanelLayout.setHorizontalGroup(
            propPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );
        propPanelLayout.setVerticalGroup(
            propPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );

        boundingCheck.setSelected(true);
        boundingCheck.setText("Bounding Boxes");

        vertexCheck.setText("Model Vertices");

        javax.swing.GroupLayout t21PropPanelLayout = new javax.swing.GroupLayout(t21PropPanel);
        t21PropPanel.setLayout(t21PropPanelLayout);
        t21PropPanelLayout.setHorizontalGroup(
            t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(t21PropPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propTableScroller, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(t21PropPanelLayout.createSequentialGroup()
                                .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                                        .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(delButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(propBoundsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(xMaxLabel)
                                            .addComponent(xMinLabel)
                                            .addComponent(xPosLabel)
                                            .addComponent(yPosLabel)
                                            .addComponent(zPosLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(zPosText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                                            .addComponent(xMinText, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                                            .addComponent(xMaxText, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                                            .addComponent(yPosText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                                            .addComponent(xPosText, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)))
                                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(zMaxLabel)
                                            .addComponent(zMinLabel)
                                            .addComponent(rotLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rotCombo, 0, 85, Short.MAX_VALUE)
                                            .addComponent(zMinText, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                                            .addComponent(zMaxText, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(formatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(propNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                        .addGap(9, 9, 9)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(t21PropPanelLayout.createSequentialGroup()
                                .addComponent(boundingCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(vertexCheck))
                            .addComponent(propPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        t21PropPanelLayout.setVerticalGroup(
            t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(t21PropPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(propTableScroller, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                        .addComponent(propPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(boundingCheck)
                            .addComponent(vertexCheck)))
                    .addGroup(t21PropPanelLayout.createSequentialGroup()
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(upButton)
                            .addComponent(addButton)
                            .addComponent(delButton)
                            .addComponent(downButton)
                            .addComponent(formatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(propNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xPosLabel)
                            .addComponent(xPosText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yPosText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yPosLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zPosText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(zPosLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xMinText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(xMinLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xMaxText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(xMaxLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zMinText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(zMinLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zMaxText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(zMaxLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(t21PropPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rotCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rotLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(propBoundsButton)))
                .addContainerGap())
        );

        t21Tabs.addTab("Props and Flora", t21PropPanel);

        getContentPane().add(t21Tabs, java.awt.BorderLayout.CENTER);

        buttonBar.setPreferredSize(new java.awt.Dimension(479, 45));

        saveCloseButton.setText("Save & Close");
        saveCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCloseButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        revertButton.setText("Revert");
        revertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonBarLayout = new javax.swing.GroupLayout(buttonBar);
        buttonBar.setLayout(buttonBarLayout);
        buttonBarLayout.setHorizontalGroup(
            buttonBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonBarLayout.createSequentialGroup()
                .addContainerGap(172, Short.MAX_VALUE)
                .addComponent(revertButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveCloseButton)
                .addContainerGap())
        );
        buttonBarLayout.setVerticalGroup(
            buttonBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveCloseButton)
                    .addComponent(saveButton)
                    .addComponent(closeButton)
                    .addComponent(revertButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(buttonBar, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void patternSize3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patternSize3ActionPerformed
        patternButton[3].setVisible(false);
        patternButton[7].setVisible(false);
        patternButton[11].setVisible(false);
        patternButton[12].setVisible(false);
        patternButton[13].setVisible(false);
        patternButton[14].setVisible(false);
        patternButton[15].setVisible(false);
        setModified(true);
    }//GEN-LAST:event_patternSize3ActionPerformed

    private void patternSize4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patternSize4ActionPerformed
        patternButton[3].setVisible(true);
        patternButton[7].setVisible(true);
        patternButton[11].setVisible(true);
        patternButton[12].setVisible(true);
        patternButton[13].setVisible(true);
        patternButton[14].setVisible(true);
        patternButton[15].setVisible(true);
        setModified(true);
    }//GEN-LAST:event_patternSize4ActionPerformed

    private void xPosTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xPosTextActionPerformed
        updateXPos(0);
    }//GEN-LAST:event_xPosTextActionPerformed

    private void formatComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatComboActionPerformed
        updatePropPos();
    }//GEN-LAST:event_formatComboActionPerformed

    private void yPosTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yPosTextActionPerformed
        updateYPos();
    }//GEN-LAST:event_yPosTextActionPerformed

    private void zPosTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zPosTextActionPerformed
        updateZPos(0);
    }//GEN-LAST:event_zPosTextActionPerformed

    private void xMinTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xMinTextActionPerformed
        updateXPos(-1);
    }//GEN-LAST:event_xMinTextActionPerformed

    private void xMaxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xMaxTextActionPerformed
        updateXPos(1);
    }//GEN-LAST:event_xMaxTextActionPerformed

    private void zMinTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zMinTextActionPerformed
        updateZPos(-1);
    }//GEN-LAST:event_zMinTextActionPerformed

    private void zMaxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zMaxTextActionPerformed
        updateZPos(1);
    }//GEN-LAST:event_zMaxTextActionPerformed

    private void rotComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotComboActionPerformed
        if(currentPFE == null) {
            rotCombo.setSelectedIndex(0);
            return;
        }
        if(currentPFE.rot != rotCombo.getSelectedIndex()) {
            currentPFE.rot = rotCombo.getSelectedIndex();
            setModified(true);
            propPanel.repaint();
        }
    }//GEN-LAST:event_rotComboActionPerformed

    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertButtonActionPerformed
        int choice = JOptionPane.showInternalConfirmDialog(this, "Discard all changes?", "Revert Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(choice == JOptionPane.YES_OPTION) {
            initData();
            setModified(false);
        }
    }//GEN-LAST:event_revertButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int selectedRow = propTable.getSelectedRow();
        if(selectedRow < 1 || selectedRow > propFloraList.size() - 1) {
            return;
        }
        PropFloraEncap pfe = propFloraList.remove(selectedRow);
        propFloraList.add(selectedRow - 1, pfe);
        if(propTable.getModel() instanceof AbstractTableModel) {
            ((AbstractTableModel)propTable.getModel()).fireTableDataChanged();
        }
        if(propTable.getCellEditor() != null)
            propTable.getCellEditor().cancelCellEditing();
        propPanel.repaint();
        propTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        setModified(true);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int selectedRow = propTable.getSelectedRow();
        if(selectedRow < 0 || selectedRow > propFloraList.size() - 2) {
            return;
        }
        PropFloraEncap pfe = propFloraList.remove(selectedRow);
        propFloraList.add(selectedRow + 1, pfe);
        if(propTable.getModel() instanceof AbstractTableModel) {
            ((AbstractTableModel)propTable.getModel()).fireTableDataChanged();
        }
        if(propTable.getCellEditor() != null)
            propTable.getCellEditor().cancelCellEditing();
        propPanel.repaint();
        propTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        setModified(true);
    }//GEN-LAST:event_downButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        int selectedRow = propFloraList.size();
        if((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
            selectedRow = propTable.getSelectedRow() + 1;
        } else if((evt.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
            selectedRow = propTable.getSelectedRow();
        }
        if(selectedRow < 0 || selectedRow > propFloraList.size()) {
            selectedRow = propFloraList.size();
        }
        PropFloraEncap pfe = new PropFloraEncap();
        pfe.type = typeEnum.PROP;
        pfe.lod = lodEnum.ALL;
        pfe.flag = false;
        pfe.objID = (long)(Math.random() * 0xFFFFFFFFL);
        pfe.IIDs.add(Long.valueOf(0L));
        pfe.rot = 0;
        pfe.xPos = 0x80000;
        pfe.xMin = 0x78000;
        pfe.xMax = 0x88000;
        pfe.yPos = 0x0;
        pfe.zPos = pfe.xPos;
        pfe.zMin = pfe.xMin;
        pfe.zMax = pfe.xMax;
        
        propFloraList.add(selectedRow, pfe);
        if(propTable.getModel() instanceof AbstractTableModel) {
            ((AbstractTableModel)propTable.getModel()).fireTableDataChanged();
        }
        if(propTable.getCellEditor() != null)
            propTable.getCellEditor().cancelCellEditing();
        propPanel.repaint();
        propTable.setRowSelectionInterval(selectedRow, selectedRow);
        setModified(true);
    }//GEN-LAST:event_addButtonActionPerformed

    private void delButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delButtonActionPerformed
        int selectedRow = propTable.getSelectedRow();
        if(selectedRow < 0 || selectedRow > propFloraList.size() - 1) {
            return;
        }
        propFloraList.remove(selectedRow);
        if(propTable.getModel() instanceof AbstractTableModel) {
            ((AbstractTableModel)propTable.getModel()).fireTableDataChanged();
        }
        if(propTable.getCellEditor() != null)
            propTable.getCellEditor().cancelCellEditing();
        propPanel.repaint();
        if(propFloraList.isEmpty())
            return;
        if(selectedRow == propFloraList.size())
            selectedRow--;
        propTable.setRowSelectionInterval(selectedRow, selectedRow);
        setModified(true);
    }//GEN-LAST:event_delButtonActionPerformed

    private void nameTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextActionPerformed
        if(!this.name.equals(nameText.getText())) {
            this.name = nameText.getText();
            setModified(true);
        }
    }//GEN-LAST:event_nameTextActionPerformed

    private void nameTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFocusLost
        if(!this.name.equals(nameText.getText())) {
            this.name = nameText.getText();
            setModified(true);
        }
    }//GEN-LAST:event_nameTextFocusLost

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        if(modified) {
            int choice = JOptionPane.showInternalConfirmDialog(this, "Discard all changes and close this exemplar?", "Close Exemplar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice == JOptionPane.YES_OPTION)
            {
                removeListeners();
                editParent.closeEditor(ex);
            }
        } else {
            removeListeners();
            editParent.closeEditor(ex);
        }
    }//GEN-LAST:event_closeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        boolean success = saveData();
        if(!success) {
            JOptionPane.showInternalMessageDialog(this, "Could not save data, please check for bad IIDs or slopes.", "Bad Data", JOptionPane.ERROR_MESSAGE);
        } else {
            editParent.updateExemplar(ex, false);
            setModified(false);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void saveCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCloseButtonActionPerformed
        boolean success = saveData();
        if(!success) {
            JOptionPane.showInternalMessageDialog(this, "Could not save data, please check for bad IIDs or slopes.", "Bad Data", JOptionPane.ERROR_MESSAGE);
        } else {
            removeListeners();
            editParent.updateExemplar(ex, true);
        }
    }//GEN-LAST:event_saveCloseButtonActionPerformed

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        if(modified) {
            int choice = JOptionPane.showInternalConfirmDialog(this, "Save changes before closing this exemplar?", "Close Exemplar", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice == JOptionPane.YES_OPTION)
            {
                boolean success = saveData();
                if(!success) {
                    JOptionPane.showInternalMessageDialog(this, "Could not save data, please check for bad IIDs or slopes.", "Bad Data", JOptionPane.ERROR_MESSAGE);
                } else {
                    removeListeners();
                    editParent.updateExemplar(ex, true);
                }
            } else if(choice == JOptionPane.NO_OPTION) {
                removeListeners();
                editParent.closeEditor(ex);
            }
        } else {
            removeListeners();
            editParent.closeEditor(ex);
        }
    }//GEN-LAST:event_formInternalFrameClosing

    private void removeListeners() {
        if(tgiListener != null)
            TGIManager.removeTGIListener(tgiListener);
        for(PropFloraEncap pfe : propFloraList) {
            if(pfe.propListener != null)
                TGIManager.removeTGIListener(pfe.propListener);
            if(pfe.modelListener != null)
                TGIManager.removeTGIListener(pfe.modelListener);
        }
    }
    
    private void iidTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iidTextActionPerformed
        Matcher m = hexPattern.matcher(iidText.getText());
        if(m.matches()) {
            long num = Long.parseLong(m.group(2), 16);
            if(iid != num)
                this.setModified(true);
            iid = num;
        }
        iidText.setText("0x" + DBPFUtil.toHex(iid, 8));
    }//GEN-LAST:event_iidTextActionPerformed

    private void tileTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tileTextActionPerformed
        Matcher m = hexPattern.matcher(tileText.getText());
        if(m.matches()) {
            long num = Long.parseLong(m.group(2), 16);
            if(tile != num)
                this.setModified(true);
            tile = num;
        }
        tileText.setText("0x" + DBPFUtil.toHex(tile, 8));
    }//GEN-LAST:event_tileTextActionPerformed

    private void minSlopeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minSlopeTextActionPerformed
        try {
            float num = Float.parseFloat(minSlopeText.getText());
            if(minSlope != num)
                T21EditWindow.this.setModified(true);
            minSlope = num;
            minSlopeText.setText(Float.toString(num));
        } catch(NumberFormatException e) {
            minSlopeText.setText(Float.toString(minSlope));
        }
    }//GEN-LAST:event_minSlopeTextActionPerformed

    private void maxSlopeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxSlopeTextActionPerformed
        try {
            float num = Float.parseFloat(maxSlopeText.getText());
            if(maxSlope != num)
                T21EditWindow.this.setModified(true);
            maxSlope = num;
            maxSlopeText.setText(Float.toString(num));
        } catch(NumberFormatException e) {
            maxSlopeText.setText(Float.toString(maxSlope));
        }
    }//GEN-LAST:event_maxSlopeTextActionPerformed

    private void propBoundsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propBoundsButtonActionPerformed
        if(currentPFE != null && currentPFE.ex != null) {
            DBPFProperty prop = currentPFE.ex.getProperty(OCCUPANT_SIZE);
            if(prop instanceof DBPFFloatProperty) {
                DBPFFloatProperty floatProp = (DBPFFloatProperty)prop;
                currentPFE.xMin = Math.max(currentPFE.xPos - (long)(floatProp.getValue(0) / 2 * 0x10000L), 0x0L);
                currentPFE.xMax = Math.min(currentPFE.xPos + (long)(floatProp.getValue(0) / 2 * 0x10000L), 0xFFFFFL);
                currentPFE.zMin = Math.max(currentPFE.zPos - (long)(floatProp.getValue(2) / 2 * 0x10000L), 0x0L);
                currentPFE.zMax = Math.min(currentPFE.zPos + (long)(floatProp.getValue(2) / 2 * 0x10000L), 0xFFFFFL);
                this.setModified(true);
                updatePropPos();
            }
        }
    }//GEN-LAST:event_propBoundsButtonActionPerformed

    private class T21PropFloraEditModel extends javax.swing.table.AbstractTableModel{

        @Override
        public Object getValueAt(int row, int col) {
            PropFloraEncap pfe = propFloraList.get(row);
            switch(col) {
                case 0:
                    return pfe.type;
                case 1:
                    return pfe.lod;
                case 2:
                    return pfe.flag;
                case 3:
                    return "0x" + DBPFUtil.toHex(pfe.objID, 8);
                case 4:
                    return "0x" + DBPFUtil.toHex(pfe.IIDs.get(0), 8);
                default:
                    return "---";
            }
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int col) {
            switch(col) {
                case 0:
                    return "Type";
                case 1:
                    return "LOD";
                case 2:
                    return "Flag";
                case 3:
                    return "Object#";
                case 4:
                    return "IID";
                default:
                    return "---";
            }
        }

        @Override
        public Class getColumnClass(int col) {
            switch(col) {
                case 0:
                    return typeEnum.class;
                case 1:
                    return lodEnum.class;
                case 2:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public int getRowCount() {
            return propFloraList.size();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object o, int row, int col) {
            PropFloraEncap pfe = propFloraList.get(row);
            String s = o.toString();
            boolean modified = false;
            long newL;
            Matcher m;
            switch(col) {
                case 0:
                    modified = !o.equals(pfe.type);
                    pfe.type = (typeEnum)o;
                    break;
                case 1:
                    modified = !o.equals(pfe.lod);
                    pfe.lod = (lodEnum)o;
                    break;
                case 2:
                    modified = !o.equals(pfe.flag);
                    pfe.flag = (Boolean)o;
                    break;
                case 3:
                    m = hexPattern.matcher(s);
                    if(m.matches()) {
                        newL = Math.min(Long.parseLong(m.group(2), 16), 0xFFFFFFFFL);
                        modified = (pfe.objID != newL);
                        pfe.objID = newL;
                    }
                    break;
                case 4:
                    m = hexPattern.matcher(s);
                    if(m.matches()) {
                        newL = Math.min(Long.parseLong(m.group(2), 16), 0xFFFFFFFFL);
                        modified = (pfe.IIDs.get(0).longValue() != newL);
                        pfe.IIDs.set(0, Long.valueOf(newL));
                        if(pfe.propListener != null)
                            TGIManager.removeTGIListener(pfe.propListener);
                        pfe.propListener = new PropListener(pfe.IIDs.get(0), pfe);
                        TGIManager.addTGIListener(pfe.propListener);
                    }
                    break;
            }
            if(modified)
                setModified(true);
            this.fireTableCellUpdated(row, col);
        }
    }
    
    class PropFloraEncap {
        public typeEnum type;
        public lodEnum lod;
        public boolean flag;
        public long rot;
        public long xPos, yPos, zPos;
        public long xMin, xMax, zMin, zMax;
        public long objID;
        public ArrayList<Long> IIDs = new ArrayList<>();
        public PropListener propListener = null;
        public DBPFExemplar ex;
        public ModelListener modelListener = null;
        public Path2D.Float vertices;
    }
    
    enum typeEnum {
        PROP ("Prop", 1),
        FLORA ("Flora", 4);
        
        String name;
        long code;
        
        private typeEnum(String name, long code) {
            this.name = name;
            this.code = code;
        }
        
        public String toString() {
            return this.name;
        }
        
        static typeEnum byCode(long code) {
            for(typeEnum type : typeEnum.values()) {
                if(type.code == code)
                    return type;
            }
            return null;
        }
    }
    
    enum lodEnum {
        ALL ("All", 0x0L),
        MEDHIGH ("Med or High", 0x10L),
        HIGH ("High Only", 0x20L);
        
        String name;
        long code;
        
        private lodEnum(String name, long code) {
            this.name = name;
            this.code = code;
        }
        
        public String toString() {
            return this.name;
        }
        
        static lodEnum byCode(long code) {
            for(lodEnum lod : lodEnum.values()) {
                if(lod.code == code)
                    return lod;
            }
            return null;
        }
    }
    
    private class PropIIDEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor, java.awt.event.ActionListener {
        
        PropFloraEncap pfe;
        JDialog diag;

        @Override
        public boolean isCellEditable(EventObject e) {
            if (e instanceof MouseEvent) { 
                return ((MouseEvent)e).getClickCount() >= 2;
            }
            return true;
        }
        
        @Override
        public Object getCellEditorValue() {
            return propIIDEditorText.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            propIIDEditorText.setText(value.toString());
            pfe = propFloraList.get(row);
            if(pfe.type == typeEnum.FLORA)
                propIIDEditorButton.setVisible(false);
            else
                propIIDEditorButton.setVisible(true);
            return propIIDEditor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case propIIDEditorTextAction:
                    this.stopCellEditing();
                    break;
                case propIIDEditorButtonAction:
                    diag = new IIDEditorDialog(T21EditWindow.this, pfe, T21Editor.app);
                    diag.setLocation(propIIDEditor.getLocationOnScreen());
                    diag.setVisible(true);
                    this.stopCellEditing();
                    break;
            }
        }
    }
    
    private class PropPanel extends javax.swing.JPanel {
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            if(tileImage != null)
                g2.drawImage(tileImage, 2, 2, 258, 258, this);

            AffineTransform save = g2.getTransform();
            g2.translate(256, 256);
            g2.rotate(Math.PI);
            g2.scale(16, 16);
            int selectedRow = propTable.getSelectedRow();
            PropFloraEncap savePFE = null;
            for(int x=0;x<=propFloraList.size();x++) {
                g2.setStroke(new BasicStroke(5.0f/64));
                PropFloraEncap pfe;
                if(x != propFloraList.size()) {
                    pfe = propFloraList.get(x);
                    if(x == selectedRow) {
                        savePFE = pfe;
                        continue;
                    }
                } else if(savePFE == null) {
                    break;
                } else {
                    pfe = savePFE;
                }
                if(x != propFloraList.size())
                    g2.setColor(Color.CYAN);
                else
                    g2.setColor(Color.RED);
                if(boundingCheck.isSelected()) {
                    float xMin = ((float)pfe.xMin) / 0x10000;
                    float zMin = ((float)pfe.zMin) / 0x10000;
                    float xMax = ((float)pfe.xMax) / 0x10000;
                    float zMax = ((float)pfe.zMax) / 0x10000;
                    float xMed = (xMax - xMin) / 2 + xMin;
                    float zMed = (zMax - zMin) / 2 + zMin;
                    g2.draw(new Rectangle2D.Float(xMin, zMin, xMax - xMin, zMax - zMin));

                    Path2D.Float triangle = new Path2D.Float(Path2D.WIND_NON_ZERO, 3);
                    switch((int)pfe.rot) {
                        case 0:
                            triangle.moveTo(xMed, zMin);
                            triangle.lineTo(xMin, zMax);
                            triangle.lineTo(xMax, zMax);
                            triangle.closePath();
                            break;
                        case 1:
                            triangle.moveTo(xMax, zMed);
                            triangle.lineTo(xMin, zMin);
                            triangle.lineTo(xMin, zMax);
                            triangle.closePath();
                            break;
                        case 2:
                            triangle.moveTo(xMed, zMax);
                            triangle.lineTo(xMax, zMin);
                            triangle.lineTo(xMin, zMin);
                            triangle.closePath();
                            break;
                        case 3:
                            triangle.moveTo(xMin, zMed);
                            triangle.lineTo(xMax, zMax);
                            triangle.lineTo(xMax, zMin);
                            triangle.closePath();
                            break;
                    }
                    g2.fill(triangle);
                    if(x != propFloraList.size())
                        g2.setColor(Color.BLUE);
                    else
                        g2.setColor(Color.ORANGE);
                }
                float xPos = ((float)pfe.xPos) / 0x10000;
                float zPos = ((float)pfe.zPos) / 0x10000;
                Ellipse2D.Float point = new Ellipse2D.Float(xPos - 1.0f / 8, zPos - 1.0f / 8, 1.0f / 4, 1.0f / 4);
                g2.fill(point);
                if(vertexCheck.isSelected() && pfe.vertices != null) {
                    g2.setStroke(new BasicStroke(2.0f/64));
                    if(x != propFloraList.size())
                        g2.setColor(Color.GREEN);
                    else
                        g2.setColor(Color.MAGENTA);
                    AffineTransform vertexShift = AffineTransform.getTranslateInstance(xPos, zPos);
                    vertexShift.concatenate(AffineTransform.getQuadrantRotateInstance((int)pfe.rot - 2));
                    Path2D.Float vertices = new Path2D.Float(pfe.vertices, vertexShift);
                    g2.draw(vertices);
                }
            }
            g2.setTransform(save);
       }
    }
    
    private class HexVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent jc) {
            return true;
        }

        @Override
        public boolean shouldYieldFocus(JComponent jc) {
            if(jc instanceof JTextField) {
                JTextField tf = (JTextField)jc;
                Matcher m = hexPattern.matcher(tf.getText());
                if(m.matches()) {
                    long num = Long.parseLong(m.group(2), 16);
                    if(tf.equals(iidText)) {
                        if(iid != num)
                            T21EditWindow.this.setModified(true);
                        iid = num;
                    } else if(tf.equals(tileText)) {
                        if(tile != num)
                            T21EditWindow.this.setModified(true);
                        tile = num;
                        if(tgiListener != null) {
                            TGIManager.removeTGIListener(tgiListener);
                        }
                        ArrayList<DBPFTGI> tgis = new ArrayList<>(4);
                        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile));
                        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile + 4));
                        tgis.add(DBPFTGI.FSH.modifyTGI(-1, 0x1ABE787DL, tile + 14));
                        tgiListener = new ImageListener(tgis);
                        TGIManager.addTGIListener(tgiListener);
                    }
                    tf.setText("0x" + DBPFUtil.toHex(num, 8));
                } else {
                    if(tf.equals(iidText))
                        iidText.setText("0x" + DBPFUtil.toHex(iid, 8));
                    else if(tf.equals(tileText))
                        tileText.setText("0x" + DBPFUtil.toHex(tile, 8));
                }
            }
            return true;
        }
    }
    
    private class FloatVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent jc) {
            return true;
        }

        @Override
        public boolean shouldYieldFocus(JComponent jc) {
            if(jc instanceof JTextField) {
                JTextField tf = (JTextField)jc;
                try {
                    float num = Float.parseFloat(tf.getText());
                    if(tf.equals(minSlopeText)) {
                        if(minSlope != num)
                            T21EditWindow.this.setModified(true);
                        minSlope = num;
                    } else if(tf.equals(maxSlopeText)) {
                        if(maxSlope != num)
                            T21EditWindow.this.setModified(true);
                        maxSlope = num;
                    }
                    tf.setText(Float.toString(num));
                } catch(NumberFormatException e) {
                    if(tf.equals(minSlopeText))
                        minSlopeText.setText(Float.toString(minSlope));
                    else if(tf.equals(maxSlopeText))
                        maxSlopeText.setText(Float.toString(maxSlope));
                }
            }
            return true;
        }
    }
    
    private class PositionVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent jc) {
            return true;
        }

        @Override
        public boolean shouldYieldFocus(JComponent jc) {
            if(currentPFE == null)
                return true;
            boolean isModified = T21EditWindow.this.modified;
            long num = 0;
            if(jc instanceof JTextField) {
                JTextField tf = (JTextField)jc;
                if(formatCombo.getSelectedIndex() == 0) {
                    try {
                        num = (long) (Float.parseFloat(tf.getText()) * 0x10000);
                    } catch(NumberFormatException e) {
                        if(tf.equals(xPosText))
                            tf.setText(Float.toString((float)currentPFE.xPos / 0x10000));
                        else if(tf.equals(xMinText))
                            tf.setText(Float.toString((float)currentPFE.xMin / 0x10000));
                        else if(tf.equals(xMaxText))
                            tf.setText(Float.toString((float)currentPFE.xMax / 0x10000));
                        else if(tf.equals(yPosText))
                            tf.setText(Float.toString((float)currentPFE.yPos / 0x10000));
                        else if(tf.equals(zPosText))
                            tf.setText(Float.toString((float)currentPFE.zPos / 0x10000));
                        else if(tf.equals(zMinText))
                            tf.setText(Float.toString((float)currentPFE.zMin / 0x10000));
                        else if(tf.equals(zMaxText))
                            tf.setText(Float.toString((float)currentPFE.zMax / 0x10000));
                        return true;
                    }
                } else {
                    Matcher m = hexPattern.matcher(tf.getText());
                    if(m.matches()) {
                        num = Long.parseLong(m.group(2), 16);
                    } else {
                        if(tf.equals(xPosText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.xPos, 8));
                        else if(tf.equals(xMinText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.xMin, 8));
                        else if(tf.equals(xMaxText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.xMax, 8));
                        else if(tf.equals(yPosText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.yPos, 8));
                        else if(tf.equals(zPosText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.zPos, 8));
                        else if(tf.equals(zMinText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.zMin, 8));
                        else if(tf.equals(zMaxText))
                            tf.setText("0x" + DBPFUtil.toHex(currentPFE.zMax, 8));
                        return true;
                    }
                }
                if(tf.equals(xPosText)) {
                    if(currentPFE.xPos != num)
                        T21EditWindow.this.setModified(true);
                    updateXPos(0);
                } else if(tf.equals(xMinText)) {
                    if(currentPFE.xMin != num)
                        T21EditWindow.this.setModified(true);
                    updateXPos(-1);
                } else if(tf.equals(xMaxText)) {
                    if(currentPFE.xMax != num)
                        T21EditWindow.this.setModified(true);
                    updateXPos(1);
                } else if(tf.equals(yPosText)) {
                    if(currentPFE.yPos != num)
                        T21EditWindow.this.setModified(true);
                    updateYPos();
                } else if(tf.equals(zPosText)) {
                    if(currentPFE.zPos != num)
                        T21EditWindow.this.setModified(true);
                    updateZPos(0);
                } else if(tf.equals(zMinText)) {
                    if(currentPFE.zMin != num)
                        T21EditWindow.this.setModified(true);
                    updateZPos(-1);
                } else if(tf.equals(zMaxText)) {
                    if(currentPFE.zMax != num)
                        T21EditWindow.this.setModified(true);
                    updateZPos(1);
                }
            }
            T21EditWindow.this.setModified(isModified);
            return true;
        }
    }
    
    private class ModifiedActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            T21EditWindow.this.setModified(true);
        }
    }
        
    private class PatternActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            int[] pattern = {0, 0, 0, 0};
            for(int x=0;x<4;x++)
                for(int y=0;y<4;y++)
                    pattern[x] |= patternButton[4*x + y].isSelected() ? (1 << y) : 0;
            if(patternSize4.isSelected())
                patternHexLabel.setText("Hex: 0x" + DBPFUtil.toHex(pattern[0], 1) +
                                        ", 0x" + DBPFUtil.toHex(pattern[1], 1) +
                                        ", 0x" + DBPFUtil.toHex(pattern[2], 1) +
                                        ", 0x" + DBPFUtil.toHex(pattern[3], 1));
            else
                patternHexLabel.setText("Hex: 0x" + DBPFUtil.toHex(pattern[0] & 0x7, 1) +
                                        ", 0x" + DBPFUtil.toHex(pattern[1] & 0x7, 1) +
                                        ", 0x" + DBPFUtil.toHex(pattern[2] & 0x7, 1));
        }
    }
    
    private class DrawingActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            propPanel.repaint();
        }
    }
    
    private class ImageListener extends TGIManager.TGIListener {

        public ImageListener(Collection<DBPFTGI> tgis) {
            super(tgis);
        }

        @Override
        public void tgiAdded(DBPFTGI tgi, DBPFFile file) {
            retrieveTileImage();
        }
    }
    
    private class PropListener extends TGIManager.TGIListener {
        
        private PropFloraEncap pfe;

        public PropListener(long instance, PropFloraEncap pfe) {
            super(DBPFTGI.EXEMPLAR.modifyTGI(-1, -1, instance));
            this.pfe = pfe;
        }

        @Override
        public void tgiAdded(DBPFTGI tgi, DBPFFile file) {
            DBPFExemplar ex = file.converter.createExemplar(tgi);
            if(ex != null) {
                DBPFProperty prop = ex.getProperty(EX_TYPE);
                if(prop instanceof DBPFLongProperty) {
                    DBPFLongProperty longProp = (DBPFLongProperty)prop;
                    if(pfe.type == typeEnum.PROP && longProp.getValue() == 0x1EL) {
                        if(pfe.ex == null || pfe.ex.getGID() != 0xC977C536L) {
                            pfe.ex = ex;
                        }
                    } else if(pfe.type == typeEnum.FLORA && longProp.getValue() == 0x0FL) {
                        if(pfe.ex == null || pfe.ex.getGID() != 0xE83E0437L) {
                            pfe.ex = ex;
                        }
                    }
                    if(pfe.ex != null) {
                        int rkt = -1;
                        for(int x=0;x<6;x++) {
                            prop = ex.getProperty(0x27812820L + x);
                            if(prop instanceof DBPFLongProperty) {
                                longProp = (DBPFLongProperty)prop;
                                rkt = x;
                                break;
                            }
                        }
                        DBPFTGI subTGI = null;
                        try {
                            switch(rkt) {
                                case 0:
                                    if(longProp.getValue(0) == 0x5AD0E817L)
                                        subTGI = new DBPFTGI(longProp.getValue(0), longProp.getValue(1), longProp.getValue(2));
                                    break;
                                case 1:
                                    subTGI = new DBPFTGI(longProp.getValue(0), longProp.getValue(1), longProp.getValue(2) + 0x400L);
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    subTGI = new DBPFTGI(longProp.getValue(0), longProp.getValue(1), longProp.getValue(6));
                                    break;
                                case 4:
                                    if(longProp.getValue(4) == 0x27812820L) {
                                        if(longProp.getValue(5) == 0x5AD0E817L)
                                            subTGI = new DBPFTGI(longProp.getValue(5), longProp.getValue(6), longProp.getValue(7));
                                    } else if(longProp.getValue(4) == 0x27812821L) {
                                        subTGI = new DBPFTGI(longProp.getValue(5), longProp.getValue(6), longProp.getValue(7) + 0x400L);
                                    }
                                    break;
                                case 5:
                                    subTGI = new DBPFTGI(longProp.getValue(0), longProp.getValue(1), longProp.getValue(2) + 0x400L);
                                    break;
                                default:
                            }
                        } catch(IndexOutOfBoundsException e) {}
                        if(pfe.modelListener != null)
                            TGIManager.removeTGIListener(pfe.modelListener);
                        if(subTGI != null) {
                            pfe.modelListener = new ModelListener(subTGI, pfe, rkt);
                            TGIManager.addTGIListener(pfe.modelListener);
                        } else {
                            pfe.modelListener = null;
                        }
                    }
                }
            }
        }
    }
    
    private class ModelListener extends TGIManager.TGIListener {
        
        private PropFloraEncap pfe;
        private int rkt;

        public ModelListener(DBPFTGI tgi, PropFloraEncap pfe, int rkt) {
            super(tgi);
            this.pfe = pfe;
            this.rkt = rkt;
        }

        @Override
        public void tgiAdded(DBPFTGI tgi, DBPFFile file) {
            Path2D.Float outline = new Path2D.Float();
            DBPFS3D s3d = file.converter.createS3D(tgi);
            List<DBPFS3D.Vert> verts = s3d.getPolys(0);
            if(verts != null) {
                int x=0;
                for(DBPFS3D.Vert vert : verts) {
                    if(x % 3 == 0)
                        outline.moveTo(vert.x, vert.z);
                    else
                        outline.lineTo(vert.x, vert.z);
                    x++;
                }
                if(pfe.vertices == null)
                    pfe.vertices = outline;
                else
                    pfe.vertices.append(outline, false);
                if(vertexCheck.isSelected())
                    propPanel.repaint();
            } else {
                System.err.println("S3D borked! " + tgi);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox boundingCheck;
    private javax.swing.JPanel buttonBar;
    private javax.swing.JCheckBox cancelCheck;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton delButton;
    private javax.swing.JButton downButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JComboBox flipsCombo;
    private javax.swing.JLabel flipsLabel;
    private javax.swing.JComboBox formatCombo;
    private javax.swing.JLabel iidLabel;
    private javax.swing.JTextField iidText;
    private javax.swing.JComboBox lodColEditor;
    private javax.swing.JLabel maxSlopeLabel;
    private javax.swing.JTextField maxSlopeText;
    private javax.swing.JLabel minSlopeLabel;
    private javax.swing.JTextField minSlopeText;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JLabel patternHexLabel;
    private javax.swing.JPanel patternPanel;
    private javax.swing.JRadioButton patternSize3;
    private javax.swing.JRadioButton patternSize4;
    private javax.swing.ButtonGroup patternSizeButtonGroup;
    private javax.swing.JLabel patternSizeLabel;
    private javax.swing.JButton propBoundsButton;
    private javax.swing.JPanel propIIDEditor;
    private javax.swing.JButton propIIDEditorButton;
    javax.swing.JTextField propIIDEditorText;
    private javax.swing.JLabel propNameLabel;
    private javax.swing.JPanel propPanel;
    private javax.swing.JTable propTable;
    private javax.swing.JScrollPane propTableScroller;
    private javax.swing.JButton revertButton;
    private javax.swing.JComboBox rotCombo;
    private javax.swing.JLabel rotLabel;
    private javax.swing.JCheckBox rotsEast;
    private javax.swing.JLabel rotsLabel;
    private javax.swing.JCheckBox rotsNorth;
    private javax.swing.JCheckBox rotsSouth;
    private javax.swing.JCheckBox rotsWest;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton saveCloseButton;
    private javax.swing.JPanel t21LayoutPanel;
    private javax.swing.JPanel t21PropPanel;
    private javax.swing.JTabbedPane t21Tabs;
    private javax.swing.JLabel tileLabel;
    private javax.swing.JTextField tileText;
    private javax.swing.JComboBox typeColEditor;
    private javax.swing.JButton upButton;
    private javax.swing.JCheckBox vertexCheck;
    private javax.swing.JCheckBox wealthHigh;
    private javax.swing.JLabel wealthLabel;
    private javax.swing.JCheckBox wealthLow;
    private javax.swing.JCheckBox wealthMed;
    private javax.swing.JCheckBox wealthNone;
    private javax.swing.JLabel xMaxLabel;
    private javax.swing.JTextField xMaxText;
    private javax.swing.JLabel xMinLabel;
    private javax.swing.JTextField xMinText;
    private javax.swing.JLabel xPosLabel;
    private javax.swing.JTextField xPosText;
    private javax.swing.JLabel yPosLabel;
    private javax.swing.JTextField yPosText;
    private javax.swing.JLabel zMaxLabel;
    private javax.swing.JTextField zMaxText;
    private javax.swing.JLabel zMinLabel;
    private javax.swing.JTextField zMinText;
    private javax.swing.JLabel zPosLabel;
    private javax.swing.JTextField zPosText;
    private javax.swing.JLabel zonesLabel;
    private javax.swing.JPanel zonesPanel;
    // End of variables declaration//GEN-END:variables
    
    public static final long EX_TYPE = 0x00000010L;
    public static final long EX_NAME = 0x00000020L;
    public static final long EX_IID = 0x00000021L;
    public static final long VERSION = 0x88EDC789L;
    public static final long TILE_IID = 0xC9A5A1BEL;
    public static final long MIN_SLOPE = 0xAA120972L;
    public static final long MAX_SLOPE = 0xAA120973L;
    public static final long PATTERN_SIZE = 0xCA81B8D4L;
    public static final long PATTERN = 0x49D55951L;
    public static final long ZONES = 0x88EDC793L;
    public static final long WEALTHS = 0x88EDC795L;
    public static final long FLIPS = 0xCC3E4755L;
    public static final long ROTS = 0xEC3BD470L;
    public static final long OBJECTS = 0x88EDC900L;
    public static final long OCCUPANT_SIZE = 0x27812810L;
    
    ArrayList<PropFloraEncap> propFloraList = new ArrayList<>();
    Pattern hexPattern = Pattern.compile("(0x)?(\\p{XDigit}{1,8})");
    
    private T21PropFloraEditModel pfEditModel;
    private PropFloraEncap currentPFE;
    private DBPFExemplar ex;
    private boolean modified = false;
    private String name = "";
    private long iid = 0, tile = 0;
    private float minSlope = 0, maxSlope = 0;
    private HexVerifier hexVer = new HexVerifier();
    private FloatVerifier floatVer = new FloatVerifier();
    private PositionVerifier posVer = new PositionVerifier();
    private JToggleButton[] patternButton = new JToggleButton[16];
    private JCheckBox[] zonesCheck = new JCheckBox[16];
    private T21ExplorerWindow editParent;
    private Image tileImage = null;
    private long tileImageIID;
    private TGIManager.TGIListener tgiListener;
    
    private final String propIIDEditorTextAction = "propIIDEditorTextAction";
    private final String propIIDEditorButtonAction = "propIIDEditorButtonAction";
}
