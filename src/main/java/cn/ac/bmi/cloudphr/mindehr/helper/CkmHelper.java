package cn.ac.bmi.cloudphr.mindehr.helper;

import cn.ac.bmi.cloudphr.mindehr.model.CkmArchetype;
import cn.ac.bmi.cloudphr.mindehr.model.CkmTemplate;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CkmHelper {
  public static void main(String[] args) {
    parseCkmRepository();
  }

  public static void parseCkmRepository() {
    try {
      Document document = Jsoup.connect(Constant.CKM_RESOURCES_URL).get();
      Elements tbodies = document.getElementsByTag("tbody");
      assert tbodies.size() == 2;

      Element archetypeInfos = tbodies.get(0);
      Elements trs = archetypeInfos.getElementsByTag("tr");
      for (int i = 1; i < trs.size(); i++) {
        CkmArchetype archetype = new CkmArchetype(trs.get(i));
        System.out.println(archetype.getAdlPath());
      }

      Element templateInfos = tbodies.get(1);
      trs = templateInfos.getElementsByTag("tr");
      for (int i = 1; i < trs.size(); i++) {
        CkmTemplate template = new CkmTemplate(trs.get(i));
        System.out.println(template.getAdlPath());
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static String parseHrefFromElement(Element element) {
    String url = null;

    if (element != null) {
      if ("a".equalsIgnoreCase(element.tagName())) {
        url = element.attr("href");
      } else {
        Elements elements = element.getElementsByTag("a");
        if (elements != null && elements.size() == 1) {
          url = elements.get(0).attr("href");
        }
      }
    }

    return url;
  }


  public static final String FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzzz";
}