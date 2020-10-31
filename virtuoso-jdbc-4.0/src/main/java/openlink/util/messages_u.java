package openlink.util;
import java.util.*;
public class messages_u extends ListResourceBundle {
  public messages_u() {
  }
  static final Object[][] contents = new String[][]{
   { "jdbcu.err.1", "Stream is closed"},
   { "jdbcu.err.2", "Invalid start position."},
   { "jdbcu.err.3", "Invalid length."},
   { "jdbcu.err.4", "Blob is freed."},
   };
  protected Object[][] getContents() {
    return contents;
  }
}
