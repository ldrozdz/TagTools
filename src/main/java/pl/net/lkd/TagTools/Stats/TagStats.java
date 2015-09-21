package pl.net.lkd.TagTools.Stats;

import org.xml.sax.*;             // The main SAX package

import java.util.*;               // Hashtable, lists, and so on

import java.util.logging.*;

public class TagStats extends org.xml.sax.helpers.DefaultHandler
{
  protected final static String LOG_FILE = "log.xml";
  protected static Logger logger = Logger.getLogger(TagStats.class.getName());  // elements={key:[tagname, attr1, attr1val, attr2, attr2val]; value:1}
  static Map elements = new HashMap();
  protected static Set<String> mode;
  protected XMLReader reader;

  public TagStats()
  {
  }
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
    //tags attrs|vals|stats
    List temp = new ArrayList(2);
    temp.clear();
    temp.add(qname);
    if (mode.contains("attrs"))
    {
      for (int i = 0; i < attributes.getLength(); i++)
      {
        // getQName stores attribute names
        temp.add(attributes.getQName(i));
        if (mode.contains("vals"))
        {
          // getValue stores attribute values
          temp.add(attributes.getValue(i));
        }
      }
    }
    if (mode.contains("stats"))
    {
      if (elements.containsKey(temp))
      {
        int s = (Integer) elements.get(temp);
        elements.put(temp, s + 1);
      } else
      {
        elements.put(temp, 1);
      }
    } else
    {
      elements.put(temp, null);
    }
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

  public static void printout(Map elements)
  {
    System.out.println(elements.size());
    Iterator it = elements.keySet().iterator();
    if (mode.contains("stats"))
    {
      while (it.hasNext())
      {
        ArrayList key = (ArrayList) it.next();
        System.out.println(key + ": " + elements.get(key));
      }
    } else
    {
      while (it.hasNext())
      {
        System.out.println(it.next());
      }
    }
  }
}
