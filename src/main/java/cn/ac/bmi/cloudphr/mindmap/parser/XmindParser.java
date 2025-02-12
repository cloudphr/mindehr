package cn.ac.bmi.cloudphr.mindmap.parser;

import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import cn.ac.bmi.cloudphr.mindmap.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class XmindParser implements MindmapParser {
  private static final String SHEET_TITLE = "title";
  private static final String SHEET_TOPIC = "rootTopic";
  private static final String CONTENT_FILE_NAME = "content.json";

  private static final String TOPIC_TITLE = "title";
  private static final String TOPIC_ID = "id";
  private static final String TOPIC_CHILDREN = "children";
  private static final String TOPIC_LABELS = "labels";
  private static final String TOPIC_NOTE = "notes";
  private static final String TOPIC_LINK = "href";

  @Override
  public Sheet[] parse(InputStream mindmapInputStream) {
    List<Sheet> sheets = new ArrayList<>();

    List<Map<String, Object>> sheetNodes = extractContentFromXmind(mindmapInputStream);
    if (sheetNodes == null) {
      return null;
    }

    for (Map<String, Object> sheetNode : sheetNodes) {
      Sheet sheet = parseSheetNode(sheetNode);
      sheet.init();
      sheets.add(sheet);
    }

    return sheets.isEmpty() ? null : sheets.toArray(new Sheet[0]);
  }

  private List<Map<String, Object>> extractContentFromXmind(InputStream xmindInputStream) {
    if (xmindInputStream == null) {
      return null;
    }

    try {
      InputStream contentInputStream = null;

      ZipInputStream zipInputStream = new ZipInputStream(xmindInputStream);
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (!entry.isDirectory() && XmindParser.CONTENT_FILE_NAME.equalsIgnoreCase(entry.getName().trim())) {
          contentInputStream = zipInputStream;
          break;
        }
      }

      if (contentInputStream == null) {
        return null;
      }

      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(contentInputStream, List.class);
    } catch (IOException ioe) {
      return null;
    }
  }

  private Sheet parseSheetNode(Map<String, Object> sheetNode) {
    String title = sheetNode.get(SHEET_TITLE).toString();
    Topic topic = parseTopicNode((Map<String, Object>) sheetNode.get(SHEET_TOPIC));
    return new Sheet(title, topic);
  }

  private Topic parseTopicNode(Map<String, Object> topicNode) {
    String id = topicNode.get(TOPIC_ID).toString();
    String title = topicNode.get(TOPIC_TITLE).toString();
    String note = parseTopicNote(topicNode);
    String link = parsetTopicLink(topicNode);
    List<Topic> children = parseTopicChildren(topicNode);
    Set<String> labels = parseTopicLabels(topicNode);

    Topic[] topicArray = children == null ? null : children.toArray(new Topic[0]);
    return new Topic(id, title, topicArray, labels, note, "".equals(link) ? null : link);
  }

  private String parseTopicNote(Map<String, Object> topicNode) {
    Map<String, Object> notesObject = (Map<String, Object>) topicNode.get(TOPIC_NOTE);
    Map<String, Object> plainObject = notesObject == null ? null : (Map<String, Object>) notesObject.get("plain");
    return plainObject == null ? null : plainObject.get("content").toString();
  }

  private String parsetTopicLink(Map<String, Object> topicNode) {
    Object linkObject = topicNode.get(TOPIC_LINK);
    return linkObject == null ? null : linkObject.toString();
  }

  private Set<String> parseTopicLabels(Map<String, Object> topicNode) {
    List<String> labelNodes = (List<String>) topicNode.get(TOPIC_LABELS);
    if (labelNodes == null) {
      return null;
    }

    Set<String> labels = new HashSet<>();
    for (String labelNode : labelNodes) {
      labels.add(labelNode);
    }

    return labels;
  }

  private List<Topic> parseTopicChildren(Map<String, Object> topicNode) {
    List<Topic> topics = new ArrayList<>();

    Map<String, Object> childrenNodes = (Map<String, Object>) topicNode.get(TOPIC_CHILDREN);
    if (childrenNodes != null) {
      List<Map<String, Object>> topicNodes = (List<Map<String, Object>>) childrenNodes.get("attached");
      if (topicNodes != null) {
        for (Map<String, Object> topic : topicNodes) {
          topics.add(parseTopicNode(topic));
        }
      }
    }

    return topics;
  }
}
