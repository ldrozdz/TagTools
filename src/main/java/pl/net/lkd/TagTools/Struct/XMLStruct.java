package pl.net.lkd.TagTools.Struct;

import pl.net.lkd.TagTools.Utils.FileTraversal;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 
 * @author lukasz
 */
public class XMLStruct extends TagStruct
{

  private class XMLTraversal extends FileTraversal
  {
    public void onDirectory(File d)
    {}

    public void onFile(File f)
    {
      try
      {
        // For each file, start with the root of the out tree
        Element outRoot = outDoc.getRootElement();
        // Read in the in file and build a tree
        SAXBuilder inBuilder = new SAXBuilder();
        // Disable validation
        inBuilder.setValidation(false);
        inBuilder.setFeature("http://xml.org/sax/features/validation", false);
        inBuilder
            .setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        inBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
            false);
        Document inDoc = inBuilder.build(f);
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

  
  
  public XMLStruct(Set<String> m, String f, String filter) throws IOException
  {
    mode = m;
    // Set things up for logging
    FileHandler handler = new FileHandler(LOG_FILE, true);
    logger.addHandler(handler);

    // Create the out tree
    outDoc = new Document(new Element("document"));
    if (mode.contains("stats"))
    {
      Element count = new Element("__count__");
      count.setText("0");
      outDoc.getRootElement().addContent(count);
    }

    XMLTraversal t = new XMLTraversal();
    t.traverse(new File(f), filter);

    printout(outDoc.getRootElement(), "");
  }

}
