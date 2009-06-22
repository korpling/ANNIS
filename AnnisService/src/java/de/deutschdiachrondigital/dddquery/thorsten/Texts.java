/*
 * $Id: Texts.java,v 1.2 2005/10/25 19:43:28 vitt Exp $
 *
 */
package de.deutschdiachrondigital.dddquery.thorsten;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * The texts class manages all texts that occur during the reconstruction
 * process.
 * 
 * @author Thorsten Vitt
 */

@SuppressWarnings("unchecked")
public class Texts {

    public Texts() {
        super();
        intervalMap = new HashMap();
    }

    /**
     * 
     * @uml.property name="maxInterval"
     * @uml.associationEnd multiplicity="(0 1)" aggregation="composite"
     *                     qualifier="textID:java.lang.Integer
     *                     de.deutschdiachrondigital.dddquery.gensql.reconstruct.MaxInterval"
     */
    private Map intervalMap;

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public java.util.Map getMaxInterval() {
        return intervalMap;
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public void setMaxInterval(java.util.Map value) {
        intervalMap = value;
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public Set maxIntervalKeySet() {
        return intervalMap.keySet();
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public Collection maxIntervalValues() {
        return intervalMap.values();
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public boolean maxIntervalContainsKey(Integer key) {
        return intervalMap.containsKey(key);
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public boolean maxIntervalContainsValue(MaxInterval value) {
        return intervalMap.containsValue(value);
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public MaxInterval getMaxInterval(Integer key) {
        return (MaxInterval) intervalMap.get(key);
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public MaxInterval putMaxInterval(Integer key, MaxInterval value) {
        return (MaxInterval) intervalMap.put(key, value);
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public MaxInterval removeMaxInterval(Integer key) {
        return (MaxInterval) intervalMap.remove(key);
    }

    /**
     * 
     * @uml.property name="maxInterval"
     */
    public void clearMaxInterval() {
        intervalMap.clear();
    }

    /**
     * Add a span to the list of maximum intervals.
     */
    public void addSpan(int textID, int left, int right) {
        MaxInterval interval;
        Integer tid = new Integer(textID);
        if (maxIntervalContainsKey(tid))
            interval = getMaxInterval(tid);
        else {
            interval = new MaxInterval(textID);
            putMaxInterval(tid, interval);
        }
        interval.addSpan(left, right);
    }

    /**
     * Returns an SQL statement which selects all texts contained in this
     * Textsset. You should also get the corresponding values and return the
     * CLOB's substrings accordingly (or we will add this functionality to this
     * class FIXME)
     * 
     * The result set will have the columns TEXT_ID and CONTENT, the latter
     * being a clob, the former an integer.
     *  
     */
    public String getTextSelectStatements() {
        // or is it better to simply get substrings?

        Iterator iter = maxIntervalKeySet().iterator();
        if (!iter.hasNext())
            return null; // no texts to select;

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT id AS id, text\nFROM  text\nWHERE");

        while (iter.hasNext()) {
            sql.append(" id = " + iter.next());
            if (iter.hasNext()) {
                sql.append("\n     OR ");
            }
        }
        return sql.toString();
    }

}