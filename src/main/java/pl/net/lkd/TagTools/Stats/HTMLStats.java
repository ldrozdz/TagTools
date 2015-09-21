package pl.net.lkd.TagTools.Stats;

import pl.net.lkd.TagTools.Utils.FileTraversal;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * @author lukasz
 */
public class HTMLStats extends TagStats
{

  public HTMLStats(Set<String> m, String f, String p, String filter) throws IOException, SAXException,
      ParserConfigurationException
  {
    // Set things up for logging
    FileHandler fhandler = new FileHandler(LOG_FILE, true);
    logger.addHandler(fhandler);
    // Set the global mode variable
    TagStats.mode = m;

    // Initialise the reader
    reader = chooseReader(p);
    TagStats xhandler = new TagStats();
    reader.setContentHandler(xhandler);
    reader.setErrorHandler(xhandler);

    // Walk through and parse the files
    HTMLTraversal t = new HTMLTraversal();
    t.traverse(new File(f), filter);

    printout(elements);
  }

  private XMLReader chooseReader(String parserChoice)
  {
    XMLReader r = null;
    if (parserChoice.equals("tagsoup"))
    {
      r = new org.ccil.cowan.tagsoup.Parser();
    } else if (parserChoice.equals("neko"))
    {
      r = new org.cyberneko.html.parsers.SAXParser();
    } else if (parserChoice.equals("htmlparser"))
    {
      r = new org.htmlparser.sax.XMLReader();
    } else if (parserChoice.equals("validator"))
    {
      r = new nu.validator.htmlparser.sax.HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
    }
    // else if (parserChoice.equals("validator"))
    // {
    // }
    return r;
  }

  private class HTMLTraversal extends FileTraversal
  {
    public void onDirectory(File d)
    {}

    public void onFile(final File f)
    {
      try
      {
        FileReader fr = new FileReader(f);
        // inputsource(f) doesn't work with htmlparser,
        // while f.getabsolutename doesn't work with validator
        reader.parse(new InputSource(fr));
      } catch (Exception ex)
      {
        Logger.getLogger(XMLStats.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

}
