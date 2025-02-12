package cn.ac.bmi.cloudphr.mindehr.helper;

import cn.ac.bmi.cloudphr.mindehr.model.Tom14Node;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class TemplateHelper {
  public static boolean serialize(String id, String templateName, Tom14Node tom14, String repoDir) {
    boolean successful = true;
    String template = "";
    // template += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n";
    template += "<template xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
          + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
          + " xmlns=\"openEHR/v1/Template\">" + "\n";
    template += "  <id>" + id + "</id>" + "\n";
    template += "  <name>" + templateName + "</name>" + "\n";
    template += "  <description>\n"
          + "    <lifecycle_state>Initial</lifecycle_state>\n"
          + "  </description>" + "\n";
    template += tom14.toString() + "\n";
    template += "</template>";

    String dirname = repoDir + "/templates";
    String filename = dirname + "/" + templateName + ".oet";

    try {
      FileUtils.writeStringToFile(new File(filename), template);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      successful = false;
    }

    return successful;
  }
}
