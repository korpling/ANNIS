/*
 * $Id: Reconstruction.java,v 1.6 2005/11/16 17:24:03 vitt Exp $
 *
 */
package de.deutschdiachrondigital.dddquery.thorsten;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import annis.dao.QueryExecution;


/**
 * Reconstructs the graph format (in the DDDq results XML format) from the
 * result of an SQL query.
 * 
 * The Reconstruction class needs a
 * {@link java.sql.Connection database connection}. You can either provide one
 * using an appriopriate constructor or provide the neccessary credentials,
 * defaults are
 * 
 * @author Thorsten Vitt
 */
@SuppressWarnings("all")
public class Reconstruction {

    /**
     * @author Thorsten Vitt
     */

    public class Node {

        int preorder;

        int postorder;

        String name;

        String attribute;

        String value;

        private int element;

        public String marker;
        
        public int left;
        
        public int right;

        public boolean isRoot = false;

        /**
         * Initializes a node from a resultSet
         * 
         * @param res
         * @throws SQLException
         */
        public Node(ResultSet res) throws SQLException {
            super();
            init(res);
        }

        public void init(ResultSet res) throws SQLException {
            preorder = res.getInt("PRE");
            postorder = res.getInt("POST");
            name = res.getString("NAME");
            element = res.getInt("STRUCT");
            attribute = res.getString("ATTRIBUTE");
            value = res.getString("VALUE");
            marker = res.getString("MARKER");

            int tid = res.getInt("TEXT_ID");
            if (res.wasNull())
                return;
            left = res.getInt("LEFT");
            if (res.wasNull())
                return;
            right = res.getInt("RIGHT");
            if (res.wasNull())
                return;
            texts.addSpan(tid, left, right);

        }

    }

//    private String dbUrl = "jdbc:oracle:thin:@sellerie:1521:wbi";
//    private String dbUser = "vitt";
//    private String dbPassword = "sellvitt";

    /**
     * Constructs the elements part of the result data set.
     * 
     * @param res
     *            the element query's results
     * @param xml
     *            receives the generated xml.
     * @throws SQLException
     * @throws IOException
     * @throws InconsistentDataException
     */
    public void reconstructElements(ResultSet res, SimpleXMLGen xml)
            throws SQLException, IOException, InconsistentDataException {
        Stack stack = new Stack(); // elements we're currently in
        Set knownElements = new HashSet(); // elements we know (for nref)
        boolean nodeAvailable = res.next(); // or end of data?
        Node node = null; // the current node

        xml.startElement("gXDF:elements");

        if (nodeAvailable)
            node = new Node(res);

        while (nodeAvailable) {

            if (stack.empty() || ((Node) stack.peek()).preorder < node.preorder) {
                // new element

                while (!stack.empty()
                        && node.preorder >= ((Node) stack.peek()).postorder) {
                    /*
                     * if the current node's (n) preorder is greater than the
                     * postorder of the stack's topmost element (s), then n
                     * cannot be inside s, so we have to close s and remove it
                     * from the stack.
                     */
                    xml.endElement((((Node) stack.peek())).name);
                    stack.pop();
                }
                xml.tryCloseStartTag();

                
                boolean isNref = !knownElements.add(new Integer(node.element));
                if (isNref) {
                    /* 
                     * if we already know the current node, we can skip the 
                     * entire subtree. Therefore we remember the end of the
                     * subtree, which is marked by the current node's postorder.
                     */
                    int skipEnd = node.postorder;

                    // first find the nid attribute of the duplicate node
//                    while (!node.attribute.equals("nid")) {
//                        if (nodeAvailable = res.next())
//                            node = new Node(res);
//                        else
//                            throw new InconsistentDataException(
//                                    "element without nid attribute (i.e. inconsistent database)");
//                    }
                    
                    // now replace the duplicate node with an nref alike node
                    xml.startElement(node.name);
                    if (node.marker != null && node.marker != "")
                        xml.addAttribute("dq:marker", node.marker);

                    xml.addAttribute("dq:nref", String.valueOf(node.element));
                    xml.endElement(null);

                    // finally skip subtree
                    while (node.preorder < skipEnd) {
                        nodeAvailable = res.next();
                        if (nodeAvailable)
                            node = new Node(res);
                        else
                            break;
                    }
                    
                    
                    
                } else { // ie. original node, not isNref
                    stack.push(node);
                    xml.startElement(node.name);
                    xml.addAttribute(node.attribute, node.value);
                    if (node.marker != null && node.marker != "")
                        xml.addAttribute("dq:marker", node.marker);
                    
                    xml.addAttribute("dq:nid", String.valueOf(node.element));
                    xml.addAttribute("dq:left", String.valueOf(node.left));
                    xml.addAttribute("dq:right", String.valueOf(node.right));

                    nodeAvailable = res.next();
                    if (nodeAvailable)
                        node = new Node(res);
                }

            } else if (((Node) stack.peek()).preorder == node.preorder) {
                // new attribute for current element
                xml.addAttribute(node.attribute, node.value);

                nodeAvailable = res.next();
                if (nodeAvailable)
                    node = new Node(res);
            }

        }
        // no more nodes -- empty stack, close open elements
        while (!stack.empty()) {
            node = (Node) stack.pop();
            xml.endElement(node.name);
        }

        xml.endElement("gXDF:elements");

    }

    public void reconstructTexts(SimpleXMLGen xml)
            throws SQLException, IOException, ClassNotFoundException {
        String textSelectStatements = texts.getTextSelectStatements();
        if (textSelectStatements == null || textSelectStatements == "")
            return;

        ResultSet res = new QueryExecution().executeQuery(textSelectStatements);

        xml.startElement("gXDF:texts");

        while (res.next()) {
            MaxInterval interval = texts.getMaxInterval(new Integer(res
                    .getInt("id")));
            xml.startElement("gXDF:text");
            xml.addAttribute("dq:id", Integer.toString(interval.getTextID()));
            xml
                    .addAttribute("gXDF:start", Integer.toString(interval
                            .getLeft()));
            xml.addAttribute("gXDF:end", Integer.toString(interval.getRight()));
            String clob = res.getString("text");
            try {
            xml.addText(clob.substring(interval.getLeft() - 1, interval.getRight() ));
            } catch (Exception e) {
            	// ok
            }
            xml.endElement("gXDF:text");
        }

        xml.endElement("gXDF:texts");

    }

    public void writeResult(ResultSet res, Writer writer)
            throws SQLException, IOException,
            InconsistentDataException, ClassNotFoundException {
        SimpleXMLGen xml = new SimpleXMLGen(writer);

        xml.startElement("dq:results");
        xml
                .addAttribute("xmlns:dq",
                        "http://www.deutschdiachrondigital.de/namespace/dddquery-result");
        xml.addAttribute("xmlns:gXDF",
                "http://www.deutschdiachrondigital.de/namespace/gXDF2");
        xml.addAttribute("xmlns",
                "http://www.deutschdiachrondigital.de/namespace/corpus");

        writer.flush();

        reconstructElements(res, xml);

        writer.flush();

        // TODO: Lonely spans ...
        reconstructTexts(xml);

        xml.endElement("dq:results");

        writer.flush();
    }

    public Reconstruction() {
        super();
        texts = new Texts();
    }

    /**
     * 
     * @uml.property name="texts"
     * @uml.associationEnd multiplicity="(1 1)" aggregation="composite"
     */
    private Texts texts;

    /**
     * 
     * @uml.property name="texts"
     */
    public Texts getTexts() {
        return texts;
    }

}