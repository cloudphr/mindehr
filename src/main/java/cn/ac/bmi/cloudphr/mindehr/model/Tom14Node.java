package cn.ac.bmi.cloudphr.mindehr.model;

import cn.ac.bmi.cloudphr.mindehr.helper.ArchetypeHelper;
import cn.hutool.log.StaticLog;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CObject;
import org.openehr.rm.support.identification.ArchetypeID;

public class Tom14Node implements Comparable<Tom14Node> {
  private String type;
  @Getter private String archetypeId;
  private String conceptName;
  private String name;
  private String path;
  private String max;
  @Getter private boolean fromTemplate;
  private String fromTemplateId;
  private List<Rule> rules;
  private List<Tom14Node> childrens;

  private Aom14Node node;
  private Repo repo;

  public Tom14Node(Aom14Node aom14, String path, Repo repo) {
    if ("Outpatient diagnosis".equals(aom14.getEnConcept())) {
      System.out.println();
    }
    this.archetypeId = aom14.getArchetypeId();
    this.fromTemplate = aom14.isFromTemplate();
    if (this.fromTemplate) {
      String templateTitle = aom14.getNode().getReferenceTitle();
      this.fromTemplateId = repo.getTemplateNameToIdMap().get(templateTitle);
    }
    this.name = aom14.getAlias();
    this.type = new ArchetypeID(this.archetypeId).rmEntity();
    this.path = path == null ? "" : path;
    this.repo = repo;
    this.rules = new LinkedList<>();
    this.childrens = new LinkedList<>();
    if ("SLOT".equals(aom14.getType())) {
      if (aom14.getNamespace() != null
            && (!aom14.isExternal() || (aom14.getChildren() == null || aom14.getChildren().size() == 0))) {
        String id = aom14.getNamespace() + "::" + archetypeId;
        aom14 = repo.getIdToNodeMap().get(id);
      }
    }
    this.node = aom14;

    Archetype archetype = repo.getArchetypeById(this.archetypeId);
    // this.conceptName = aom14.getEnConcept();
    this.conceptName = archetype.getConceptName("en");

    if ("COMPOSITION".equals(this.type) || "SECTION".equals(this.type)) {
      for (CAttribute attribute : archetype.getDefinition().getAttributes()) {
        for (CObject child : attribute.getChildren()) {
          Rule rule = new Rule();
          rule.path = child.path();
          rule.max = "0";
          this.rules.add(rule);
        }
      }
      for (Aom14Node child : aom14.getChildren()) {
        // FIXME: check if the child is legal to added in content
        // FIXME: ... child should be the referenced one if it is a slot.
        child.setType("SLOT");
        String childPath = "/content";
        if (this.path != null && !"".equals(this.path.trim())) {
          childPath = "/items";
        }
        Tom14Node childTom = new Tom14Node(child, childPath, repo);
        childTom.max = "1";
        this.childrens.add(childTom);
      }
    } else {
      Map<String, String> nodeIdToConceptNameMap = ArchetypeHelper.getNodeIdToConceptNameMap(archetype);
      CAttribute attribute = ArchetypeHelper.locateAttribute(archetype.getDefinition(), "items");

      String parentPath = attribute.parentNodePath();
      CObject parent;
      if ("".equals(parentPath)) {
        parent = archetype.getDefinition();
      } else {
        parent = archetype.getPathNodeMap().get(parentPath);
      }
      if (this.node.getChildren() != null && this.node.getChildren().size() > 0) {
        Map<String, Aom14Node> aom14NodeMap = this.node.flattenAom14Node(repo, "", false);

        Map<String, CObject> excludes = new LinkedHashMap<>();
        Map<String, CObject> primitives = new LinkedHashMap<>();
        Map<String, CObject> references = new LinkedHashMap<>();

        ArchetypeHelper.flattenCobject(parent, "", excludes, primitives, references,
              aom14NodeMap, nodeIdToConceptNameMap);

        for (String exclude : excludes.keySet()) {
          Rule rule = new Rule();
          rule.path = excludes.get(exclude).path();
          rule.max = "0";
          this.rules.add(rule);
        }

        for (String primitive : primitives.keySet()) {
          Rule rule = new Rule();
          rule.path = primitives.get(primitive).path();
          Aom14Node node = aom14NodeMap.get(primitive);
          if (node != null) {
            rule.name = node.getAlias();
            this.rules.add(rule);
          } else {
            StaticLog.error("----------> " + primitive);
          }
        }

        for (String reference : references.keySet()) {
          // FIXME: bug exists here.
          //        it should contains the children entries only.
          Aom14Node node = aom14NodeMap.get(reference);    // <- should be the child node but not
          String childPath = references.get(reference).path();
          Tom14Node childTom = new Tom14Node(node, childPath, repo);
          if (!node.isRepeat()) {
            childTom.max = "1";
          }
          this.childrens.add(childTom);
        }
      }
    }
  }

  private String contentType = "definition";
  private int indent = 0;

  @Override
  public String toString() {
    Collections.sort(this.childrens);
    Collections.sort(this.rules);
    String item = StringUtils.repeat(" ", this.indent + 2) + "<" + this.contentType
          + " xsi:type=\"" + this.type + "\""
          + " archetype_id=\"" + this.archetypeId + "\""
          // FIXME: calculate the template id first
          + (this.fromTemplate ? " template_id=\"" + this.fromTemplateId + "\"" : "")
          + " concept_name=\"" + this.conceptName + "\""
          + ("definition".equals(this.contentType) ? "" : " path=\"" + this.path + "\"")
          + " name=\"" + this.name + "\">" + "\n";
    if (!this.fromTemplate) {
      for (Rule rule : this.rules) {
        item += StringUtils.repeat(" ", this.indent + 4) + rule.toString() + "\n";
      }
      for (Tom14Node child : this.childrens) {
        child.indent = this.indent + 2;
        switch (this.type) {
          case "COMPOSITION":
            child.contentType = "Content";
            break;
          case "SECTION":
            child.contentType = "Item";
            break;
          case "ACTION":
          case "ADMIN_ENTRY":
          case "EVALUATION":
          case "OBSERVATION":
          case "INSTRUCTION":
          case "CLUSTER":
            child.contentType = "Items";
            break;
          default:
            StaticLog.error("Unsupported type! " + this.type);
            break;
        }
        item += child.toString() + "\n";
      }
    }
    item += StringUtils.repeat(" ", this.indent + 2) + "</" + this.contentType + ">";
    return item;
  }


  @Override
  public int compareTo(Tom14Node that) {
    if (this.path == null || that.path == null) {
      return 0;
    }
    return this.path.compareTo(that.path);
  }

  class Rule implements Comparable<Rule> {
    protected String name;
    protected String path;
    protected String max;

    @Override
    public String toString() {
      String rule = "<Rule path=\"" + path + "\"";
      if (name != null && !"".equals(name.trim())) {
        rule += " name=\"" + name + "\"";
      }
      if (max != null && !"".equals(max.trim())) {
        rule += " max=\"" + max + "\"";
      }
      rule += "/>";
      return rule;
    }

    @Override
    public int compareTo(Rule that) {
      if (this.path == null || that.path == null) {
        return 0;
      }
      return this.path.compareTo(that.path);
    }
  }
}