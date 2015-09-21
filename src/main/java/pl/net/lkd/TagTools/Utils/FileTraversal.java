// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FileTraversal.java

package pl.net.lkd.TagTools.Utils;

import java.io.File;
import java.io.IOException;

public abstract class FileTraversal
{

  public final void traverse(File f, String filter) throws IOException
  {
    if (f.isDirectory())
    {
      onDirectory(f);
      File children[] = f.listFiles();
      File arr$[] = children;
      int len$ = arr$.length;
      for (int i$ = 0; i$ < len$; i$++)
      {
        File child = arr$[i$];
        traverse(child, filter);
      }

      return;
    } else
    {
      if (filter != null)
      {
        if (f.getName().equals(filter))
        {
          onFile(f);
          return;
        }
      } else
      {
        onFile(f);
        return;
      }
    }
  }

  public abstract void onDirectory(File file);

  public abstract void onFile(File file);
}
