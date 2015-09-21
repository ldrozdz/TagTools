/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.net.lkd.TagTools.Viewer;

import dom.traversal.NameNodeFilter;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ui.DOMTreeFull;

/** This class shows a DOM Document in a JTree, and presents controls
 *  which allow the user to create and view the progress of a TreeWalker
 *  in the DOM tree.
 */
public class TreeWalkerView
        extends JFrame
        implements ActionListener
{
  private static final long serialVersionUID = 3257566187583189559L;
  Document document;
  TreeNode lastSelected;
  DOMParser parser;
  JTextArea messageText;
  JScrollPane messageScroll;
  DOMTreeFull jtree;
  TreeWalker treeWalker;
  NameNodeFilter nameNodeFilter;
  JButton nextButton;
  JButton prevButton;
  JButton newIterator;
  JButton parentButton;
  JButton nextSiblingButton;
  JButton previousSiblingButton;
  JButton firstChildButton;
  JButton lastChildButton;

  /** main */
  public static void main(Document d)
  {

    try
    {
      TreeWalkerView frame = new TreeWalkerView(d);
      frame.addWindowListener(new java.awt.event.WindowAdapter()
      {
        public void windowClosing(java.awt.event.WindowEvent e)
        {
          System.exit(0);
        }
      });
      frame.setSize(640, 700);
      frame.setVisible(true);
    } catch (Exception e)
    {
      e.printStackTrace(System.err);
    }
  }
  Hashtable treeNodeMap = new Hashtable();

  /** Constructor */
  public TreeWalkerView(Document document)
  {
    super("TreeWalkerView");
    try
    {

      if (!document.isSupported("Traversal", "2.0"))
      {
        // This cannot happen with the DOMParser...
        throw new RuntimeException("This DOM Document does not support Traversal");
      }

      // jtree  UI setup
      jtree = new DOMTreeFull((Node) document);
      jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

      // Listen for when the selection changes, call nodeSelected(node)
      jtree.addTreeSelectionListener(
              new TreeSelectionListener()
              {
                public void valueChanged(TreeSelectionEvent e)
                {
                  TreePath path = (TreePath) e.getPath();
                  TreeNode treeNode = (TreeNode) path.getLastPathComponent();
                  if (jtree.getSelectionModel().isPathSelected(path))
                  {
                    nodeSelected(treeNode);
                  }
                }
              });

      //            
      // controls
      //

      BorderLayout borderLayout = new BorderLayout();

      //iterate panel
      JPanel iteratePanel = new JPanel();
      iteratePanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("Document Order Traversal"),
              BorderFactory.createEmptyBorder(4, 4, 4, 4)));

      prevButton = new JButton("Previous");
      iteratePanel.add(prevButton);
      prevButton.addActionListener(this);

      nextButton = new JButton("Next");
      iteratePanel.add(nextButton);
      nextButton.addActionListener(this);

      //walkerPanel
      JPanel walkerPanel = new JPanel();
      walkerPanel.setLayout(new BorderLayout());
      walkerPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("Walk"),
              BorderFactory.createEmptyBorder(4, 4, 4, 4)));

      parentButton = new JButton("Parent");
      walkerPanel.add(parentButton, BorderLayout.NORTH);
      parentButton.addActionListener(this);

      JPanel childPanel = new JPanel();
      firstChildButton = new JButton("First Child");
      childPanel.add(firstChildButton);
      firstChildButton.addActionListener(this);

      lastChildButton = new JButton("Last Child");
      childPanel.add(lastChildButton);
      lastChildButton.addActionListener(this);
      walkerPanel.add(childPanel, BorderLayout.SOUTH);

      nextSiblingButton = new JButton("Next Sibling");
      walkerPanel.add(nextSiblingButton, BorderLayout.EAST);
      nextSiblingButton.addActionListener(this);

      previousSiblingButton = new JButton("Previous Sibling");
      walkerPanel.add(previousSiblingButton, BorderLayout.WEST);
      previousSiblingButton.addActionListener(this);

      JPanel controlsPanel = new JPanel(new BorderLayout());
      controlsPanel.setFont(new Font("Dialog", Font.PLAIN, 8));
      JPanel buttonsPanel = new JPanel(new BorderLayout());
      buttonsPanel.add(iteratePanel, BorderLayout.NORTH);
      buttonsPanel.add(walkerPanel, BorderLayout.CENTER);
      controlsPanel.add(buttonsPanel, BorderLayout.NORTH);
      controlsPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("Controls"),
              BorderFactory.createEmptyBorder(4, 4, 4, 4)));

      // tree panel    
      JPanel treePanel = new JPanel(new BorderLayout());

      JScrollPane treeScroll = new JScrollPane(jtree);
      treeScroll.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("Tree View"),
              BorderFactory.createEmptyBorder(4, 4, 4, 4)));

      // message text UI setup
      messageText = new JTextArea(3, 5);

      JPanel messagePanel = new JPanel(new BorderLayout());
      messageScroll = new JScrollPane(messageText);
      messagePanel.add(messageScroll);
      messagePanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("Messages"),
              BorderFactory.createEmptyBorder(4, 4, 4, 4)));

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(controlsPanel, BorderLayout.EAST);
      mainPanel.add(treeScroll, BorderLayout.CENTER);
      mainPanel.add(messagePanel, BorderLayout.SOUTH);
      getContentPane().add(mainPanel);

      treeWalker = ((DocumentTraversal) document).createTreeWalker(
              document,
              NodeFilter.SHOW_ALL,
              new NameNodeFilter(),
              true);

    } catch (Exception e)
    {
      e.printStackTrace(System.err);
    }
  }

  public void actionPerformed(ActionEvent e)
  {

    if (e.getSource() == newIterator)
    {

      TreeNode treeNode = (TreeNode) jtree.getLastSelectedPathComponent();
      if (treeNode == null)
      {
        messageText.append("Must select a tree component.");
        return;
      }

      Node node = jtree.getNode(treeNode);
      if (node == null)
      {
        setMessage("No current Node in TreeNode: " + node);
      }

      treeWalker = ((DocumentTraversal) document).createTreeWalker(node,
              NodeFilter.SHOW_ALL,
              nameNodeFilter,
              true);
      setMessage("createTreeWalker:" +
              " root=" + node);
      return;

    }

    if (e.getSource() == previousSiblingButton)
    {
      Node node = treeWalker.previousSibling();
      handleButton(node, "previousSibling()");
      return;
    }

    if (e.getSource() == firstChildButton)
    {
      Node node = treeWalker.firstChild();
      handleButton(node, "firstChild()");
      return;
    }

    if (e.getSource() == lastChildButton)
    {
      Node node = treeWalker.lastChild();
      handleButton(node, "lastChild()");
      return;
    }

    if (e.getSource() == nextSiblingButton)
    {
      Node node = treeWalker.nextSibling();
      handleButton(node, "nextSibling()");
      return;
    }

    if (e.getSource() == parentButton)
    {
      Node node = treeWalker.parentNode();
      handleButton(node, "parentNode()");
      return;
    }

    if (e.getSource() == nextButton)
    {
      Node node = treeWalker.nextNode();
      handleButton(node, "nextNode()");
      return;
    }

    if (e.getSource() == prevButton)
    {
      Node node = treeWalker.previousNode();
      handleButton(node, "previousNode()");
      return;
    }

  }

  /** handle a button press: output messages and select node. */
  void handleButton(Node node, String function)
  {

    setMessage("treeWalker." + function + " == " + node);

    if (node == null)
    {
      return;
    }
    TreeNode treeNode = jtree.getTreeNode(node);
    if (treeNode == null)
    {
      setMessage("No JTree TreeNode for Node name:" + node.getNodeName());
      return;
    }

    TreePath path = new TreePath(
            ((DefaultTreeModel) jtree.getModel()).getPathToRoot(treeNode));
    jtree.requestFocus();
    jtree.setSelectionPath(path);
    jtree.scrollPathToVisible(path);
  }

  /** Helper function to set messages */
  void setMessage(String string)
  {
    messageText.selectAll();
    messageText.cut();
    messageText.append(string);
    messageText.setCaretPosition(0);
  }

  /** called when our JTree's nodes are selected.
   */
  void nodeSelected(TreeNode treeNode)
  {

    lastSelected = treeNode;
    Node node = jtree.getNode(treeNode);

    if (node == null)
    {
      return;
    }
    setMessage(DOMTreeFull.toString(node));
  }

  /** Utility function to expand the jtree */
  void expandTree()
  {
    for (int i = 0; i < jtree.getRowCount(); i++)
    {
      jtree.expandRow(i);
    }
  }

  class Errors implements ErrorHandler
  {
    Hashtable errorNodes = new Hashtable();

    public void warning(SAXParseException ex)
    {
      store(ex, "[Warning]");
    }

    public void error(SAXParseException ex)
    {
      store(ex, "[Error]");
    }

    public void fatalError(SAXParseException ex) throws SAXException
    {
      store(ex, "[Fatal Error]");
    }

    public Hashtable getErrorNodes()
    {
      return errorNodes;
    }

    public Object getError(Node node)
    {
      return errorNodes.get(node);
    }

    public void clearErrors()
    {
      errorNodes.clear();
    }

    void store(SAXParseException ex, String type)
    {

      // build error text
      String errorString = type + " at line number, " + ex.getLineNumber() + ": " + ex.getMessage() + "\n";

      Node currentNode = null;
      try
      {
        currentNode = (Node) parser.getProperty("http://apache.org/xml/properties/dom-node");
      } catch (SAXException se)
      {
        System.err.println(se.getMessage());
        return;
      }
      if (currentNode == null)
      {
        return;      // accumulate any multiple errors per node in the Hashtable.
      }
      String previous = (String) errorNodes.get(currentNode);
      if (previous != null)
      {
        errorNodes.put(currentNode, previous + errorString);
      } else
      {
        errorNodes.put(currentNode, errorString);
      }
    }
  }

}
