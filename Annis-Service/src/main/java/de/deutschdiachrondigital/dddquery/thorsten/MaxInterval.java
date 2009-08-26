/*
 * $Id: MaxInterval.java,v 1.2 2005/11/01 16:20:16 vitt Exp $
 *
 */
package de.deutschdiachrondigital.dddquery.thorsten;

/**
 * @author Thorsten Vitt
 */

public class MaxInterval {

    /**
     * 
     */
    public MaxInterval(int textID) {
        super();
        this.textID = textID;
    }
    
    public MaxInterval(int textID, int left, int right) {
        this(textID);
        addSpan(left, right);
    }

    /**
     * The ID of the text this is the maximum interval of.	 
     *  
     * @uml.property name="textID" 
     */
    private int textID;

    /**
     *  
     * @uml.property name="textID"
     */
    public int getTextID() {
        return textID;
    }

    /**
     *  
     * @uml.property name="textID"
     */
    public void setTextID(int textID) {
        this.textID = textID;
    }

    /**
     * The left border 
     *  
     * @uml.property name="left" 
     */
    private int left = -1;

    /**
     *  
     * @uml.property name="left"
     */
    public int getLeft() {
        return (left == -1? 0 : left);
    }

    /**
     * The right border 
     *  
     * @uml.property name="right" 
     */
    private int right = 0;

    /**
     *  
     * @uml.property name="right"
     */
    public int getRight() {
        return right;
    }

    /**
     * Registers the given sub-interval and recomputesthe interval's borders accordingly
     */
    public void addSpan(int left, int right) {
        if (this.left > left || this.left == -1) {
            this.left = left;
        }
        if (this.right < right) {
            this.right = right;
        }
    }

    /**
     * @return
     */
    public int getLength() {
        // TODO Auto-generated method stub
        return (right - left);
    }

}
