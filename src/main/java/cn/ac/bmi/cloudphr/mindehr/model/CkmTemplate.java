package cn.ac.bmi.cloudphr.mindehr.model;

import static cn.ac.bmi.cloudphr.mindehr.helper.CkmHelper.FORMAT;

import cn.ac.bmi.cloudphr.mindehr.helper.CkmHelper;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Data
public class CkmTemplate {
  private String template;
  private String templateId;
  private String status;
  private String createdAt;
  private String updatedAt;
  private String assetCid;
  private String ckmPath;
  private String adlPath;

  public CkmTemplate(Element element) {
    Elements tds = element.getElementsByTag("td");
    this.template = tds.get(0).text();
    this.templateId = tds.get(1).text();
    this.status = tds.get(2).text();
    this.createdAt = DateUtil.parse(tds.get(3).text(), FORMAT).toString();
    this.updatedAt = DateUtil.parse(tds.get(4).text(), FORMAT).toString();
    this.assetCid = tds.get(5).text();
    this.ckmPath = CkmHelper.parseHrefFromElement(tds.get(6));
    this.adlPath = CkmHelper.parseHrefFromElement(tds.get(7));
  }
}