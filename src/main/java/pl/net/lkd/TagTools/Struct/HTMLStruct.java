package pl.net.lkd.TagTools.Struct;

import pl.net.lkd.TagTools.Utils.FileTraversal;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Document;
import org.jdom.Element;
import com.dappit.Dapper.parser.MozillaParser;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.apache.commons.io.FileUtils;
import org.cyberneko.html.parsers.DOMParser;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.JDomSerializer;
import org.htmlcleaner.TagNode;
import org.jdom.input.SAXBuilder;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.test.*;
import org.lobobrowser.html.*;
//import org.w3c.dom.*;
/**
 *
 * @author lukasz
 */
public class HTMLStruct extends TagStruct
{
  private String parserChoice;

  public HTMLStruct(Set<String> m, String f, String p, String filter) throws IOException, Exception
  {
    parserChoice = p;
    mode = m;
    // Set things up for logging
    FileHandler handler = new FileHandler(LOG_FILE, true);
    logger.addHandler(handler);

    if (parserChoice.equals("mozilla"))
    {
      //Initialise the parser
      mozillaInit();
    }

    //Create the out tree
    outDoc = new Document(new Element("document"));
    if (mode.contains("stats"))
    {
      Element count = new Element("__count__");
      count.setText("0");
      outDoc.getRootElement().addContent(count);
    }

    HTMLTraversal t = new HTMLTraversal();
    t.traverse(new File(f), filter);

    printout(outDoc.getRootElement(), "");
  }

  private static void mozillaInit() throws Exception
  {
    File parserLibraryFile = new File(
            "/opt/MozillaHtmlParser/native/bin/MozillaParser.so");
    String parserLibrary = parserLibraryFile.getAbsolutePath();
    //System.out.println("Loading Parser Library :" + parserLibrary);
    //	mozilla.dist.bin directory :
    final File mozillaDistBinDirectory = new File(
            "/opt/MozillaHtmlParser/dist/bin");
    MozillaParser.init(parserLibrary, mozillaDistBinDirectory.getAbsolutePath());
  }

  private static Document mozillaParse(File f) throws Exception
  {
    MozillaParser parser = new MozillaParser();
    org.w3c.dom.Document DOMDoc = parser.parse(FileUtils.readFileToString(f));
    DOMBuilder builder = new DOMBuilder();
    Document inDoc = builder.build(DOMDoc);
    return inDoc;
  }

  private static Document nekoParse(File f) throws IOException, SAXException
  {
    org.cyberneko.html.parsers.DOMParser parser = new DOMParser();
    parser.parse(f.getAbsolutePath());
    org.w3c.dom.Document DOMDoc = parser.getDocument();
    DOMBuilder builder = new DOMBuilder();
    Document inDoc = builder.build(DOMDoc);
    return inDoc;
  }

  private static Document jtidyParse(File f) throws FileNotFoundException
  {
    Tidy parser = new Tidy();
    parser.setQuiet(true);
    parser.setShowWarnings(false);
    parser.setDocType("omit");
    org.w3c.dom.Document DOMDoc = parser.parseDOM(new FileInputStream(f), null);
    DOMBuilder builder = new DOMBuilder();
    Document inDoc = builder.build(DOMDoc);
    return inDoc;
  }

  private static Document htmlcleanerParse(File f) throws FileNotFoundException, IOException
  {
    HtmlCleaner parser = new HtmlCleaner();
    CleanerProperties props = parser.getProperties();
    props.setAdvancedXmlEscape(true);
    props.setAllowHtmlInsideAttributes(false);
    props.setIgnoreQuestAndExclam(true);
    props.setOmitDoctypeDeclaration(true);
    props.setOmitComments(true);
    props.setOmitXmlDeclaration(true);
    props.setTranslateSpecialEntities(true);
    props.setRecognizeUnicodeChars(true);
    props.setUseCdataForScriptAndStyle(false);
    props.setNamespacesAware(false);
    TagNode node = parser.clean(f);
    Document inDoc = new JDomSerializer(props, true).createJDom(node);
    return inDoc;
  }

  private static Document cobraParse(File f) throws SAXException, IOException, ParserConfigurationException
  {
    UserAgentContext uacontext = new SimpleUserAgentContext();
    //DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
    DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dbuilder = dbfactory.newDocumentBuilder();
    org.w3c.dom.Document DOMDoc = dbuilder.newDocument();
    HtmlParser parser = new HtmlParser(uacontext, DOMDoc);
    Reader reader = new FileReader(f);
    parser.parse(reader);
    DOMBuilder builder = new DOMBuilder();
    Document inDoc = builder.build(DOMDoc);
    return inDoc;
  }

  private static Document validatorParse(File f) throws SAXException, IOException
  {
    DocumentBuilder b = new HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
    org.w3c.dom.Document DOMDoc = b.parse(f);
    DOMBuilder builder = new DOMBuilder();
    Document inDoc = builder.build(DOMDoc);
    return inDoc;
  }

  private static Document tagsoupParse(File f) throws JDOMException, IOException
  {
    SAXBuilder builder = new SAXBuilder("org.ccil.cowan.tagsoup.Parser");
    Document inDoc = builder.build(f);
    return inDoc;
  }

  private class HTMLTraversal extends FileTraversal
  {
    public void onDirectory(File d)
    {
    }

    public void onFile(File f)
    {
      try
      {
        // For each file, start with the root of the out tree
        Element outRoot = outDoc.getRootElement();

        // Parse the document
        Document inDoc = new Document();
        if (parserChoice.equals("mozilla"))
        {
          inDoc = mozillaParse(f);
        } else if (parserChoice.equals("neko"))
        {
          inDoc = nekoParse(f);
        } else if (parserChoice.equals("jtidy"))
        {
          inDoc = jtidyParse(f);
        } else if (parserChoice.equals("htmlcleaner"))
        {
          inDoc = htmlcleanerParse(f);
        } else if (parserChoice.equals("cobra"))
        {
          inDoc = cobraParse(f);
        } else if (parserChoice.equals("validator"))
        {
          inDoc = validatorParse(f);
        } else if (parserChoice.equals("tagsoup"))
        {
          inDoc = tagsoupParse(f);
        }
        //XMLOutputter outputter = new XMLOutputter();
        //outputter.output(inDoc, System.out);
        Element inRoot = inDoc.getRootElement();
        // Increase document count
        if (mode.contains("stats"))
        {
          Element countEl = outRoot.getChild("__count__");
          docCount = Integer.parseInt(countEl.getText());
          countEl.setText(String.valueOf(++docCount));
        }
        // Let's take a walk through the tree
        docWalk(inRoot, outRoot);
      } catch (Exception ex)
      {
        Logger.getLogger(TagStruct.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

}
