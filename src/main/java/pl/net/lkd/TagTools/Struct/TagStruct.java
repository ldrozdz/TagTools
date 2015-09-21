package pl.net.lkd.TagTools.Struct;

import pl.net.lkd.TagTools.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import java.io.*;                 // For reading the input file

import java.util.*;               // Hashtable, lists, and so on

import java.util.logging.*;

public class TagStruct
{
  protected final static String LOG_FILE = "log.xml";
  protected static Logger logger = Logger.getLogger(TagStruct.class.getName());  // elements={key:[tagname, attr1, attr1val, attr2, attr2val]; value:1}
  protected static Document outDoc = null;
  protected static Integer docCount = 0;
  protected static Set<String> mode;

   public TagStruct()
  {
  }

  protected void docWalk(Element inEl, Element outEl)
  {
    List inKids = inEl.getChildren();
    Iterator elIt = inKids.iterator();
    // Iterate over the children of the current in node
    while (elIt.hasNext())
    {
      Element il = (Element) elIt.next();
      List oList = outEl.getChildren(il.getName());
      Element ol = findElement(il, oList);
      if (ol == null) // If the element doesn't exist in the out tree
      {
        ol = createElement(il);
        outEl.addContent(ol);
      } else  // If the element exists in the out tree
      {
        if (mode.contains("stats"))
        {
          updateElementCount(ol);
        }
      }
      // Dive one level deeper
      if (il.getChildren() != null)
      {
        docWalk(il, ol);
      }
    }
  }

  protected Element createElement(Element inEl)
  {
    Element outEl = new Element(inEl.getName());
    if (mode.contains("attrs"))
    {
      Map<String, String> ilAttrs = new HashMap<String, String>();
      ilAttrs = getAttrs(inEl);
      Iterator atIt = ilAttrs.entrySet().iterator();
      while (atIt.hasNext())
      {
        Map.Entry at = (Map.Entry) atIt.next();
        String atName = at.getKey().toString();
        String atValue = "";
        if (mode.contains("vals"))
        {
          atValue = at.getValue().toString();
        }
        outEl.setAttribute(atName, atValue);
      }
    }
    if (mode.contains("stats"))
    {
      Element count = new Element("__count__");
      count.setText("1");
      outEl.addContent(count);
    }
    return outEl;
  }

  protected Map<String, String> getAttrs(Element el)
  {
    Map<String, String> attrs = new HashMap<String, String>();
    List atList = el.getAttributes();
    if (atList != null)
    {
      Iterator atIt = atList.iterator();
      while (atIt.hasNext())
      {
        Attribute at = ((Attribute) atIt.next());
        String attrName = at.getName();
        String attrVal = "";
        if (mode.contains("vals"))
        {
          attrVal = at.getValue();
        }
        attrs.put(attrName, attrVal);
      }
    }
    return attrs;
  }

  protected boolean compareElements(Element inEl, Element outEl)
  {
    String ilTag = inEl.getName();
    if (outEl == null) // If the element doesn't exist in the out tree
    {
      return false;
    } else
    {
      if (mode.contains("attrs"))
      {
        // Get the list of in doc attributes and convert it to a set
        Map<String, String> ilAttrs = getAttrs(inEl);
        // Get the list of out doc attributes and convert it to a set
        Map<String, String> olAttrs = getAttrs(outEl);
        if (ilAttrs.equals(olAttrs))  // The in and out elements are equal
        {
          return true;
        } else  // The in and out elements are different
        {
          return false;
        }
      }
      return true;
    }
  }

  protected Element findElement(Element inEl, List outList)
  {
    String ilTag = inEl.getName();
    Iterator olIt = outList.iterator();
    while (olIt.hasNext())
    {
      Element outEl = (Element) olIt.next();
      if (compareElements(inEl, outEl))
      {
        return outEl;
      }
    }
    return null;
  }

  protected void updateElementCount(Element el)
  {

    Element countEl = el.getChild("__count__");
    Integer count = Integer.parseInt(countEl.getText());
    countEl.setText(String.valueOf(++count));
  }

  protected void printout(Element el, String indent)
  {
    if (!el.getName().equals("__count__"))
    {
      String tag = el.getName();
      String line = indent + "[" + tag;
      if (mode.contains("attrs"))
      {
        List attrs = el.getAttributes();
        Iterator atIt = attrs.iterator();
        while (atIt.hasNext())
        {
          Attribute at = ((Attribute) atIt.next());
          line += " " + at.getName();


          if (mode.contains("vals"))
          {
            line += "=\"" + at.getValue() + "\"";
          }
        }
      }
      line += "]";
      if (mode.contains("stats"))
      {
        if (!el.getName().equals("__count__"))
        {
          String count = el.getChildText("__count__");
          line += ": " + count;
        }
      }
      System.out.println(line);
    }
    Iterator elIt = el.getChildren().iterator();
    while (elIt.hasNext())
    {
      printout((Element) elIt.next(), indent + "  ");
    }
  }
}
