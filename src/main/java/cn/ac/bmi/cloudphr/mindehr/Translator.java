package cn.ac.bmi.cloudphr.mindehr;

import cn.ac.bmi.cloudphr.mindehr.helper.Constant;
import cn.ac.bmi.cloudphr.mindehr.helper.TemplateHelper;
import cn.ac.bmi.cloudphr.mindehr.model.Aom14Node;
import cn.ac.bmi.cloudphr.mindehr.model.Repo;
import cn.ac.bmi.cloudphr.mindehr.model.Tom14Node;
import cn.ac.bmi.cloudphr.mindmap.model.Mindmap;
import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import cn.ac.bmi.cloudphr.mindmap.parser.XmindParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Translator {

  public static boolean convert(String input, String dstDir) {
    try {
      return convert(new FileInputStream(input), dstDir);
    } catch (FileNotFoundException fnfe) {
      return false;
    }
  }

  public static boolean convert(File input, String dstDir) {
    try {
      return convert(new FileInputStream(input), dstDir);
    } catch (FileNotFoundException fnfe) {
      return false;
    }
  }

  public static boolean convert(InputStream input, String dstDir) {
    XmindParser parser = new XmindParser();
    Sheet[] sheets = parser.parse(input);
    sheets = Arrays.stream(sheets).filter(sheet -> sheet != null).collect(Collectors.toList()).toArray(new Sheet[0]);
    Mindmap mindmap = new Mindmap(sheets);
    Repo repo = new Repo(mindmap, dstDir);
    repo.generateArchetypes().loadArchetypes();

    Map<String, Aom14Node> templates = repo.getTemplates();
    for (String sheetTitle : templates.keySet()) {
      Aom14Node aom = templates.get(sheetTitle);
      Tom14Node tom14 = new Tom14Node(aom, "", repo);
      String templateId = UUID.randomUUID().toString();
      repo.getTemplateNameToIdMap().put(sheetTitle, templateId);
      if (!TemplateHelper.serialize(templateId, sheetTitle, tom14, repo.getRepoPath())) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please set the xmind path as parameter");
      System.exit(-1);
    } else {
      String xmind = args[0];
      if (args.length == 2) {
        Constant.LOCAL_CKM_SERVER = args[1];
      }
      Translator.convert(xmind, "tmp/openehr");
    }
  }
}
