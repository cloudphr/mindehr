package cn.ac.bmi.cloudphr.mindmap.parser;

import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface MindmapParser {
  default Sheet[] parse(String mindmapPath) {
    if (mindmapPath == null) {
      return new Sheet[0];
    }

    try {
      InputStream mindmapInputStream = new FileInputStream(mindmapPath.trim());
      return parse(mindmapInputStream);
    } catch (IOException ioe) {
      return new Sheet[0];
    }
  }

  Sheet[] parse(InputStream mindmapInputStream);
}