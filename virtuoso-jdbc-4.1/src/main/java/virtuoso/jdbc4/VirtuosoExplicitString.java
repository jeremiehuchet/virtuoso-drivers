package virtuoso.jdbc4;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.math.*;
import openlink.util.*;
public class VirtuosoExplicitString
{
  private int dtp;
  private byte[] bytes;
  private String str;
  private VirtuosoConnection con;
  protected VirtuosoExplicitString (byte [] bytes, int dtp)
    {
      this.dtp = dtp;
      this.bytes = bytes;
      this.str = null;
      if (dtp == VirtuosoTypes.DV_STRING || dtp == VirtuosoTypes.DV_SHORT_STRING_SERIAL ||
            dtp == VirtuosoTypes.DV_STRICT_STRING || dtp == VirtuosoTypes.DV_C_STRING ||
            dtp == VirtuosoTypes.DV_BLOB || dtp == VirtuosoTypes.DV_ANY)
        {
            if (bytes.length < 256)
                this.dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
            else
                this.dtp = VirtuosoTypes.DV_STRING;
        }
    }
  protected VirtuosoExplicitString (String str, int dtp, VirtuosoConnection con) throws VirtuosoException
    {
      try
 {
   this.dtp = dtp;
   this.con = con;
   if (dtp == VirtuosoTypes.DV_WIDE ||
       dtp == VirtuosoTypes.DV_LONG_WIDE ||
       dtp == VirtuosoTypes.DV_BLOB_WIDE)
     {
       bytes = str.getBytes ("UTF8");
       if (bytes.length < 256)
  this.dtp = VirtuosoTypes.DV_WIDE;
       else
  this.dtp = VirtuosoTypes.DV_LONG_WIDE;
       this.str = str;
     }
   else if (dtp == VirtuosoTypes.DV_STRING ||
       dtp == VirtuosoTypes.DV_SHORT_STRING_SERIAL ||
       dtp == VirtuosoTypes.DV_STRICT_STRING ||
       dtp == VirtuosoTypes.DV_C_STRING ||
       dtp == VirtuosoTypes.DV_BLOB)
     {
        if (con != null && con.charset_utf8)
         bytes = str.getBytes ("UTF8");
       else if (con != null && con.charset != null)
  bytes = con.charsetBytes(str);
       else
  cli_wide_to_narrow (str, con != null ? con.client_charset_hash : null);
       this.dtp = (bytes.length < 256) ? VirtuosoTypes.DV_SHORT_STRING_SERIAL : VirtuosoTypes.DV_STRING;
     }
   else if (dtp == VirtuosoTypes.DV_ANY)
     {
       if (con != null && (con.charset != null || con.charset_utf8))
         {
    bytes = (con.charset_utf8) ? str.getBytes ("UTF8") : con.charsetBytes(str);
    this.dtp = (bytes.length < 256) ? VirtuosoTypes.DV_SHORT_STRING_SERIAL : VirtuosoTypes.DV_STRING;
         }
       else
  {
    boolean wide = false;
                  for (int i = 0; i < str.length(); i++)
                    if (str.charAt(i) > 127) {
                      wide = true;
                      break;
                    }
                  if (wide) {
      bytes = str.getBytes ("UTF8");
      this.dtp = (bytes.length < 256) ? VirtuosoTypes.DV_WIDE : VirtuosoTypes.DV_LONG_WIDE;
      this.str = str;
                  }
                  else {
      cli_wide_to_narrow (str, con != null ? con.client_charset_hash : null);
             this.dtp = (bytes.length < 256) ? VirtuosoTypes.DV_SHORT_STRING_SERIAL : VirtuosoTypes.DV_STRING;
                  }
  }
     }
   else if (dtp == VirtuosoTypes.DV_BLOB_BIN || dtp == VirtuosoTypes.DV_BIN)
     {
       bytes = str.getBytes ("8859_1");
       if (bytes.length < 256)
  this.dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
       else
  this.dtp = VirtuosoTypes.DV_STRING;
     }
   else
     {
       if (!cli_wide_to_narrow (str, con != null ? con.client_charset_hash : null))
  {
    bytes = str.getBytes ("UTF8");
    if (bytes.length < 256)
      this.dtp = VirtuosoTypes.DV_WIDE;
    else
      this.dtp = VirtuosoTypes.DV_LONG_WIDE;
    this.str = str;
  }
       else
  {
            if (con != null && con.charset_utf8)
             bytes = str.getBytes ("UTF8");
           else if (con != null && con.charset != null)
      bytes = con.charsetBytes(str);
    if (bytes.length < 256)
      this.dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
    else
      this.dtp = VirtuosoTypes.DV_STRING;
  }
     }
 }
      catch (java.io.UnsupportedEncodingException e)
 {
   if (con != null && con.charset != null)
     bytes = con.charsetBytes(str);
   else
     cli_wide_to_narrow (str, con != null ? con.client_charset_hash : null);
   if (bytes.length < 256)
     dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
   else
     dtp = VirtuosoTypes.DV_STRING;
 }
    }
  protected boolean cli_wide_to_narrow (String str, Hashtable charset_ht)
    {
      boolean ret = true;
      if (str == null)
        {
   bytes = new byte[0];
   return (true);
 }
      bytes = new byte[str.length()];
      if (charset_ht == null)
       {
        for (int i = 0; i < str.length(); i++)
   bytes[i] = (byte)str.charAt(i);
       }
      else
       {
        for (int i = 0; i < str.length(); i++)
   {
     Character ch = Character.valueOf (str.charAt(i));
     Byte b;
     b = (Byte)(charset_ht != null ? charset_ht.get (ch) : Byte.valueOf ((byte) (ch.charValue())));
     if (b == null)
       {
         bytes[i] = (byte) '?';
         ret = false;
       }
     else
       bytes[i] = (byte) b.intValue();
   }
       }
      return ret;
    }
  protected boolean cli_wide_to_escaped (String str, Hashtable ht)
    {
      boolean ret = true;
      if (str == null)
        {
   bytes = new byte[0];
   dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
   return (true);
 }
      StringBuffer strbuf = new StringBuffer ();
      for (int i = 0; i < str.length(); i++)
 {
   char curr = str.charAt(i);
   Byte b = null;
   if (ht != null)
     {
       Character ch = Character.valueOf (curr);
       b = (Byte)ht.get (ch);
       if (b == null)
  {
    strbuf.append ("\\x");
                  strbuf.append (String.format("%04x", (int) ch.charValue()));
  }
       else
  {
    strbuf.append ((char) b.intValue());
  }
     }
   else
     {
       if (((int)curr) > 255)
  {
    strbuf.append ("\\x");
    strbuf.append (String.format("%04x", (int) curr));
  }
       else
  strbuf.append (curr);
     }
 }
      bytes = new byte[strbuf.length() + 1];
      for (int i = 0; i < strbuf.length(); i++)
 bytes[i] = (byte) strbuf.charAt (i);
      strbuf = null;
      if (bytes.length < 256)
 this.dtp = VirtuosoTypes.DV_SHORT_STRING_SERIAL;
      else
 this.dtp = VirtuosoTypes.DV_STRING;
      return true;
    }
  protected int getDtp() { return this.dtp; };
  protected void write (VirtuosoOutputStream os) throws IOException
    {
      os.write (dtp);
      if (bytes.length < 256)
 os.write (bytes.length);
      else
 os.writelongint (bytes.length);
      os.write (bytes, 0, bytes.length);
    }
  public String toString ()
    {
      if (this.str != null)
 return this.str;
      try
 {
   if (dtp == VirtuosoTypes.DV_WIDE || dtp == VirtuosoTypes.DV_LONG_WIDE)
     return new String (bytes, "UTF8");
   else if (this.con != null && this.con.charset != null)
     return this.con.uncharsetBytes (new String (bytes, "8859_1"));
   else
     return new String (bytes, "8859_1");
 }
      catch (Exception e)
 {
   char [] chars = new char [bytes.length];
   for (int i =0 ; i < bytes.length; i++)
     chars[i] = (char)(bytes[i] & 0xff);
   return new String (chars);
 }
    }
  public String toParamString ()
    {
      return toString().trim();
    }
  public boolean equals (Object obj)
    {
      if (obj != null && (obj instanceof VirtuosoExplicitString))
 {
   VirtuosoExplicitString sobj = (VirtuosoExplicitString)obj;
   if (sobj.dtp != this.dtp)
     return false;
   else
     return sobj.bytes.equals (this.bytes);
 }
      else
 return false;
    }
}
