package pl.net.lkd.TagTools;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import pl.net.lkd.TagTools.Stats.*;
import pl.net.lkd.TagTools.Struct.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * 
 * @author lukasz
 */
public class TagTools
{
  public static void main(String[] args)
  {
    // java -jar TagUtils.jar --xml|--html --tags attrs|vals|stats path
    // [file_filter]"

    try
    {
      // create the command line parser
      CommandLineParser parser = new PosixParser();
      // parse the command line arguments
      CommandLine line = parser.parse(getOptions(), args);
      processCL(line);
    } catch (ParseException exp)
    {
      usage(exp.getMessage(), getOptions());
    }

  }

  private static Options getOptions()
  {
    // create the Options
    Options options = new Options();
    OptionGroup input = new OptionGroup();
    Option html = new Option("H", "html", false, "the input file is an HTML");
    Option xml = new Option("X", "xml", false, "the input file is an XML");
    input.addOption(html);
    input.addOption(xml);
    input.setRequired(true);
    OptionGroup mode = new OptionGroup();
    Option flat = new Option("F", "flat", false, "produce flat output");
    Option struct = new Option("S", "struct", false, "reproduce document structure");
    mode.addOption(flat);
    mode.addOption(struct);
    mode.setRequired(true);
    Option attrs = new Option("a", "attrs", false, "process tag attributes");
    Option vals = new Option("v", "vals", false, "process tag attribute values (slow)");
    Option stats = new Option("s", "stats", false, "count tag usage");
    options.addOptionGroup(input);
    options.addOptionGroup(mode);
    options.addOption(attrs);
    options.addOption(vals);
    options.addOption(stats);
    return options;
  }

  private static void processCL(CommandLine cl)
  {
    Set<String> level = new HashSet<String>();
    if (cl.hasOption("a"))
    {
      level.add("attrs");
    }
    if (cl.hasOption("v"))
    {
      level.add("vals");
    }
    if (cl.hasOption("s"))
    {
      level.add("stats");
    }

    String type = new String();
    if (cl.hasOption("H"))
    {
      type = "html";
    } else if (cl.hasOption("X"))
    {
      type = "xml";
    }

    String mode = new String();
    if (cl.hasOption("F"))
    {
      mode = "flat";
    } else if (cl.hasOption("S"))
    {
      mode = "struct";
    }

    String[] args = cl.getArgs();
    // Testing

    if ((args.length < 1) || (args.length > 3))
    {
      usage("Wrong number of arguments!", getOptions());
    } else
    {
      dispatch(type, mode, level, args);
    }
  }

  private static void usage(String message, Options options)
  {
    System.out.println(message + "\n");
    // automatically generate the help statement
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("XML Mode: TagTools -X -F|-S [-a -v -s] FILE|DIR [FILENAME_FILTER]\n"
        + "HTML Mode: TagTools -H -F|-S [-a -v -s] PARSER FILE|DIR [FILENAME_FILTER]\n",
        "TagTools release 1.0, codename 'Buggy Bastard'\n", options,
        "\nThe optional FILENAME_FILTER parameter instructs the parser to only process certain files.\n"
            + "\nIn HTML mode, you need to choose one of the following parsers: "
            + "tagsoup, neko, htmlparser, validator, mozilla, htmlcleaner, cobra "
            + "(some of them may be broken or not available on your system).");
  }

  private static void dispatch(String type, String mode, Set<String> level, String[] args)
  {
    String file = new String();
    String parser = new String();
    String filter = null;
    if (type.equals("html") && (args.length == 2 || args.length == 3))
    {
      file = args[1];
      parser = args[0];
      if (args.length == 3)
      {
        filter = args[2];
      }
      if (mode.equals("flat"))
      {
        try
        {
          new HTMLStats(level, file, parser, filter);
        } catch (IOException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        }
      } else if (mode.equals("struct"))
      {
        try
        {
          new HTMLStruct(level, file, parser, filter);
        } catch (Exception ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    } else if (type.equals("xml") && (args.length == 1 || args.length == 2))
    {
      file = args[0];
      if (args.length == 2)
      {
        filter = args[1];
      }
      if (mode.equals("flat"))
      {
        try
        {
          new XMLStats(level, file, filter);
        } catch (IOException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        }
      } else if (mode.equals("struct"))
      {
        try
        {
          new XMLStruct(level, file, filter);
        } catch (IOException ex)
        {
          Logger.getLogger(TagTools.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
}
