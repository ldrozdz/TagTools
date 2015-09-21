/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.net.lkd.TagTools.Stats;

import pl.net.lkd.TagTools.Utils.FileTraversal;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * @author lukasz
 */
public class XMLStats extends TagStats
{
  public XMLStats(Set<String> m, String f, String filter) throws IOException, SAXException,
      ParserConfigurationException
  {
    // Set things up for logging
    FileHandler handler = new FileHandler(LOG_FILE, true);
    logger.addHandler(handler);
    // Set the global mode variable
    TagStats.mode = m;

    // Initialise the XML Reader
    reader = XMLReaderFactory.createXMLReader();
    TagStats xhandler = new TagStats();
    reader.setContentHandler(xhandler);
    reader.setErrorHandler(xhandler);

    // Walk through and parse the files
    XMLTraversal t = new XMLTraversal();
    t.traverse(new File(f), filter);

    printout(elements);
  }

  private class XMLTraversal extends FileTraversal
  {
    public void onDirectory(File d)
    {}

    public void onFile(final File f)
    {
      try
      {
        reader.parse(new InputSource(f.getAbsolutePath()));
      } catch (Exception ex)
      {
        Logger.getLogger(XMLStats.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
