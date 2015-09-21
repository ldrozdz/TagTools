package pl.net.lkd.TagTools.Utils;

import javax.xml.parsers.*;       // JAXP classes for obtaining a SAX Parser
import org.xml.sax.*;             // The main SAX package
import java.io.*;                 // For reading the input file
import java.util.*;               // Hashtable, lists, and so on

/**
 * Parse a web.xml file using the SAX2 API.
 * This class extends DefaultHandler so that instances can serve as SAX2
 * event handlers, and can be notified by the parser of parsing events.
 * We simply override the methods that receive events we're interested in
 **/
public class TagList extends org.xml.sax.helpers.DefaultHandler
{

  /** The main method sets things up for parsing
   * @param arg - the name of the file to parse
   * @throws java.io.IOException
   * @throws org.xml.sax.SAXException
   * @throws javax.xml.parsers.ParserConfigurationException 
   */
  public static void main(String arg) throws IOException, SAXException, ParserConfigurationException
  {
    // We use a SAXParserFactory to obtain a SAXParser, which
    // encapsulates a SAXReader.
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);     // We don't want validation
    factory.setNamespaceAware(false); // No namespaces please
    // Create a SAXParser object from the factory
    SAXParser parser = factory.newSAXParser();
    // Now parse the file specified on the command line using
    // an instance of this class to handle the parser callbacks
    parser.parse(new File(arg), new TagList());
  }
  
  Set elements = new HashSet();
  
  // Called at the beginning of parsing.  We use it as an init( ) method
  public void startDocument()
  {
  }

  public void characters(char[] buffer, int start, int length)
  {
  }

  // At the beginning of each new element, erase any accumulated text.
  public void startElement(String namespaceURL, String localName,
          String qname, Attributes attributes)
  {
    List temp = new ArrayList(2);
    temp.clear();
    temp.add(qname);
    for (int i=0 ; i < attributes.getLength(); i++)
    {
      temp.add(attributes.getQName(i));
      temp.add(attributes.getValue(i));
    }
    elements.add(temp);
  }

  // Take special action when we reach the end of selected elements.
  // Although we don't use a validating parser, this method does assume
  // that the web.xml file we're parsing is valid.
  public void endElement(String namespaceURL, String localName, String qname)
  {
  }

  // Called at the end of parsing.  Used here to print our results.
  public void endDocument()
  {
    System.out.println(elements.size());
    Iterator it = elements.iterator();
    while (it.hasNext())
    {
      System.out.println(it.next());
    }
  }

  // Issue a warning
  public void warning(SAXParseException exception)
  {
    System.err.println("WARNING: line " + exception.getLineNumber() + ": " +
            exception.getMessage());
  }

  // Report a parsing error
  public void error(SAXParseException exception)
  {
    System.err.println("ERROR: line " + exception.getLineNumber() + ": " +
            exception.getMessage());
  }

  // Report a non-recoverable error and exit
  public void fatalError(SAXParseException exception) throws SAXException
  {
    System.err.println("FATAL: line " + exception.getLineNumber() + ": " +
            exception.getMessage());
    throw (exception);
  }
}
