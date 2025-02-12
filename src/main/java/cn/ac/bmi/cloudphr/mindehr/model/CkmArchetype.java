package cn.ac.bmi.cloudphr.mindehr.model;

import static cn.ac.bmi.cloudphr.mindehr.helper.CkmHelper.FORMAT;

import cn.ac.bmi.cloudphr.mindehr.helper.CkmHelper;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Data
public class CkmArchetype {
  private String concept;
  private String archetypeId;
  private String description;
  private String status;
  private String createdAt;
  private String updatedAt;
  private String assetCid;
  private String ckmPath;
  private String adlPath;
  private String xmlPath;

  public CkmArchetype(Element element) {
    Elements tds = element.getElementsByTag("td");
    this.concept = tds.get(0).text();
    this.archetypeId = tds.get(1).text();
    this.description = tds.get(2).text();
    this.status = tds.get(3).text();
    this.createdAt = DateUtil.parse(tds.get(4).text(), FORMAT).toString();
    this.updatedAt = DateUtil.parse(tds.get(5).text(), FORMAT).toString();
    this.assetCid = tds.get(6).text();
    this.ckmPath = CkmHelper.parseHrefFromElement(tds.get(7));
    this.adlPath = CkmHelper.parseHrefFromElement(tds.get(8));
    this.xmlPath = CkmHelper.parseHrefFromElement(tds.get(9));
  }
}