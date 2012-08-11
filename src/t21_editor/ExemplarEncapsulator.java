/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package t21_editor;

import jwb.jdbpf.DBPFFile;
import jwb.jdbpf.types.DBPFExemplar;

/**
 *
 * @author Jon
 */
public class ExemplarEncapsulator {

    public DBPFExemplar ex;
    public DBPFFile filename;

    public ExemplarEncapsulator(DBPFExemplar ex, DBPFFile filename) {
        this.ex = ex;
        this.filename = filename;
    }
}
