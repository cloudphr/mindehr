package cn.ac.bmi.cloudphr.mindehr.model;

import cn.ac.bmi.cloudphr.mindehr.helper.Constant;
import cn.ac.bmi.cloudphr.mindmap.model.Mindmap;
import cn.ac.bmi.cloudphr.mindmap.model.Topic;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

public class MindNode {
  @Getter private String cnName;
  @Getter private String enName;
  @Getter private String alias;
  @Getter @Setter private String type;
  @Getter @Setter private List<MindNode> children;
  @Getter private boolean repeat;
  @Getter private boolean fromTemplate;

  @Getter private final Topic topic;

  public static final String DEFAULT_SIMPLE_TYPE_NAME = "text";
  public static final String DEFAULT_OBJECT_TYPE_NAME = "inner";

  public MindNode(final Topic topic) throws Exception {
    if (topic == null) {
      StaticLog.error("No topic!");
      throw new Exception("No topic!");
    }

    this.topic = topic;
    this.processLabels();
    this.processTitle();
    this.processChildren();
    this.processLink();
  }

  public MindNode copyWithoutRepeat() {
    MindNode mindNode = null;
    try {
      mindNode = new MindNode(topic);
      mindNode.repeat = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mindNode;
  }

  public boolean isComplexPrimitive() {
    return PRIMITIVE_TYPES_COMPLEX.contains(this.type.toLowerCase());
  }

  public boolean isSimplePrimitive() {
    return PRIMITIVE_TYPES_SIMPLE.contains(this.type.toLowerCase());
  }

  public boolean isExternalReference() {
    // FIXME:
    return topic.getLink() != null || this.type.startsWith("openEHR-");
  }

  public boolean isObjectType() {
    return (this.children != null && this.children.size() > 0)
          || this.isComplexPrimitive()
          || this.isExternalReference();
  }

  @Override
  public String toString() {
    return "Node{" + topic + " of <" + this.getType() + ">}";
  }

  private void processLabels() throws Exception {
    Set<String> labels = topic.getLabels();
    if (labels == null) {
      labels = new HashSet<>();
    }
    if (labels.contains(Constant.REPEAT_LABEL)) {
      this.repeat = true;
      labels.remove(Constant.REPEAT_LABEL);
    }

    if (labels.size() > 1) {
      StaticLog.warn(topic.toString() + ":" + Constant.FORMAT_ERROR_LABELS.getMessage());
      StaticLog.warn(" Use the first one as the type!");
      this.type = labels.iterator().next();
    } else if (labels.size() == 1) {
      this.type = labels.iterator().next();
      if (PRIMITIVE_TYPES.contains(this.type.toLowerCase())) {
        this.type = this.type.toLowerCase();
      }
    } else {
      if (topic.getTopics() == null || topic.getTopics().length == 0) {
        this.type = DEFAULT_SIMPLE_TYPE_NAME;
      } else {
        this.type = DEFAULT_OBJECT_TYPE_NAME;
      }
    }
  }

  private void processTitle() throws Exception {
    String title = this.topic.getTitle();

    if (title == null || title.split(Constant.TOPIC_ALIAS_SEPARATOR + "").length > 2) {
      StaticLog.error("Error occurs in " + this.topic);
      throw Constant.FORMAT_ERROR_TITLE;
    } else {
      String[] conceptAndName = topic.getTitle().split(Constant.TOPIC_ALIAS_SEPARATOR + "");
      String[] concepts = conceptAndName[0].split(Constant.TOPIC_NAME_SEPARATOR + "");
      if (concepts.length != 2) {
        StaticLog.error("Error occurs in " + this.topic);
        throw Constant.FORMAT_ERROR_TITLE;
      } else {
        if ("".equals(concepts[0].trim()) || "".equals(concepts[1].trim())) {
          StaticLog.error("Error occurs in " + this.topic);
          throw Constant.FORMAT_ERROR_TITLE;
        } else {
          this.cnName = concepts[0].trim();
          this.enName = concepts[1].trim();
        }
      }
      if (conceptAndName.length == 2) {
        this.alias = conceptAndName[1].trim();
      } else {
        this.alias = StrUtil.toCamelCase(enName.replaceAll("\\s+", "_")) + (this.repeat ? "s" : "");
      }
      this.alias = this.alias.replaceAll("[/|&|\\||'|\"|;]", "_");
    }
  }

  private void processChildren() throws Exception {
    if (topic.getTopics() != null) {
      this.children = new ArrayList<>();
      for (Topic child : topic.getTopics()) {
        this.children.add(new MindNode(child));
      }
    }
  }

  private void processLink() {
    String sheetTitle = getReferenceTitle();
    if (sheetTitle != null && sheetTitle.matches("^T[0-9]+\\..*$")) {
      this.fromTemplate = true;
    }
  }

  public String getReferenceTitle() {
    String referenceTitle = null;
    String xmindIdPrefix = "xmind:#";
    if (topic.getLink() != null && topic.getLink().startsWith(xmindIdPrefix)) {
      String refId = topic.getLink().substring(xmindIdPrefix.length());
      Mindmap mindmap = topic.getBelongTo().getBelongTo();
      Topic reference = mindmap.getTopic(refId);
      if (reference != null) {
        referenceTitle = reference.getBelongTo().getTitle();
      }
    }
    return referenceTitle;
  }

  private static final Set<String> PRIMITIVE_TYPES_SIMPLE = new HashSet<>(Arrays.asList(
        "boolean", "count", "date", "datetime", "identifier", "multimedia",
        "ordinal", "real", "text", DEFAULT_SIMPLE_TYPE_NAME
  ));
  private static final Set<String> PRIMITIVE_TYPES_COMPLEX = new HashSet<String>(Arrays.asList(
        "codedtext", "duration", "quantity", "terminology"
  ));
  private static final Set<String> PRIMITIVE_TYPES =  Stream
        .concat(PRIMITIVE_TYPES_SIMPLE.stream(), PRIMITIVE_TYPES_COMPLEX.stream())
        .collect(Collectors.toSet());

  public static List<MindNode> CODED_TEXT_NODE_CHILDREN;
  // public static List<Node> IDENTIFIER_NODE_CHILDREN;
  public static List<MindNode> DURATION_NODE_CHILDREN;
  public static List<MindNode> QUANTITY_NODE_CHILDREN;
  // public static List<Node> MULTIMEDIA_NODE_CHILDREN;

  static {
    try {
      CODED_TEXT_NODE_CHILDREN = Arrays.asList(
            new MindNode(new Topic("编码/Code", null)),
            new MindNode(new Topic("名称/Name", null))
      );
      DURATION_NODE_CHILDREN = Arrays.asList(
            new MindNode(new Topic("值/Value", "real", null)),
            new MindNode(new Topic("周期/Period", null))
      );
      QUANTITY_NODE_CHILDREN = Arrays.asList(
            new MindNode(new Topic("值/Value", "real", null)),
            new MindNode(new Topic("单位/Units", null))
      );
      // IDENTIFIER_NODE_CHILDREN = Arrays.asList(
      //     // FIXME:
      // );
      // MULTIMEDIA_NODE_CHILDREN = Arrays.asList(
      //     new Node(new Topic("Base64编码内容/data", null)),
      //     new Node(new Topic("图片大小/size", null))
      // );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}