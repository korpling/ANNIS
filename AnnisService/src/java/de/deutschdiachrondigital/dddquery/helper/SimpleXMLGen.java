package de.deutschdiachrondigital.dddquery.helper;
import java.io.IOException;
import java.io.Writer;


/**
 * A really simple XML generator class.
 * 
 * SimpleXMLGen offers creation of an XML file with very little overhead.
 * The generated XML is written to a {@link java.io.Writer} 
 * which has to be passed as an argument to the {@link #SimpleXMLGen(Writer) 
 * constructor}. 
 * 
 * The class does not implement a stack to keep track of open elements, you
 * have to do that yourself. It does, however, know whether we are inside a 
 * start tag.
 * 
 * @author Thorsten Vitt
 *	
 */


public class SimpleXMLGen {

    /** 
     * The Writer to receive the XML Stream. 
     */
    protected Writer out;

    /** True while an open tag has been started but not closed, i.e. there
     * is something like <code>&lt;foo bar="baz" </code> in the output
     * stream.
     */
    protected boolean inTag;
    
    /**
     * Creates the simple XML Generator.
     * @param writer the writer receiving the generated XML.
     */
    public SimpleXMLGen(Writer writer) {
        out = writer;
        inTag = false;
    }

    /**
     * Access the @see Writer the generated XML is written to. Be careful if you write
     * anything there ...
     * @return the generator's Writer object.
     * 
     * @uml.property name="out"
     */
    public Writer getWriter() {
        return out;
    }

    
    /**
     * If we are inside a start tag, close it. We are never in an end tag, so there is no
     * equivalent function. If you plan to write something directly to this class using the
     * Writer,  you should call this function first. 
     * 
     * @param linebreak is a boolean which controls whether this function may
     * 			insert a line break after the tag-closing <code>&gt;</code>.
     * @throws IOException
     */
    public void tryCloseStartTag(boolean linebreak) throws IOException {
        if (inTag) {
            if (linebreak)
                out.write(">\n");
            else
                out.write(">");
            inTag = false;
        }
    }
    
    /**
     * If we are inside a start tag, close it. We are never in an end tag, so there is no
     * equivalent function. If you plan to write something directly to this class using the
     * Writer,  you should call this function first. 
     * 
     * @see #tryCloseStartTag(boolean) 
     * @throws IOException
     */
    public void tryCloseStartTag() throws IOException {
        tryCloseStartTag(true);
    }

    
    /**
     * Begins to write the start tag of an XML element. This method does not close the start 
     * tag (i.e. it does not write any '&gt;'), so that attributes can be added.
     * @param element the name of the element to be added 
     * @throws IOException
     */
    public void startElement(String element) throws IOException {
        tryCloseStartTag();
        out.write("<" + element);
        inTag = true;
    }
    
    /**
     * Begins to write the start tag of an XML element. This method does not close the start 
     * tag (i.e. it does not write any '&gt;'), so that attributes can be added.
     * @param element	 the name of the element to be added
     * @param attributeString	a string with attributes (or anything else) to be added to the start tag.
     * @throws IOException
     */
    public void startElement(String element, String attributeString) throws IOException {
        startElement(element);
        out.write(" ");
        out.write(attributeString);
    }
    
    /**
     * Adds an attribute to the currently open start tag. 
     * If any argument is null, nothing is added. 
     * @param attribute the attribute's name
     * @param value	the attribute's value. May not contain ". 
     * @throws IOException	if writing fails or if there is currently no open start tag.
     */
    public void addAttribute(String attribute, String value) throws IOException {
        if (inTag) {
            if ((attribute != null) && (value != null))
                out.write(" " + attribute + "=\"" + value  + "\"");
        }
        else 
            throw new IOException("try to add XML attribute outside of tag"); 
    }
    
    /**
     * Ends an element (i.e. write <code>&lt;/element&gt;</code>).
     * @param element		the name of the element to end. We don't have a stack, so you must pass the correct name.
     * @throws IOException
     */
    public void endElement(String element) throws IOException {
        if (inTag)
            out.write("/>\n");
        else
            out.write("</" + element + ">\n");
        inTag = false;
    }

    /**
     * Adds a text node. This will close any open tag if neccessary.
     * @param string
     * @throws IOException
     */
    public void addText(String string) throws IOException {
        tryCloseStartTag(false);
        out.write(string);
    }
}
