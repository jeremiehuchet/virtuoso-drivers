package virtuoso.jdbc4;
import java.io.*;
class VirtuosoAsciiInputStream extends FilterInputStream
{
   VirtuosoAsciiInputStream(byte[] data)
   {
      super(new ByteArrayInputStream(data));
   }
}
