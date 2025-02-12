package cn.ac.bmi.cloudphr.mindehr.model;

import cn.ac.bmi.cloudphr.mindehr.helper.ArchetypeHelper;
import cn.ac.bmi.cloudphr.mindehr.helper.Constant;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.assertion.Assertion;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.ontology.ArchetypeOntology;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.rm.common.generic.RevisionHistory;
import org.openehr.rm.common.resource.ResourceDescription;
import org.openehr.rm.common.resource.TranslationDetails;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.support.identification.ArchetypeID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.terminology.SimpleTerminologyService;

public class Aom14Node {
  @Getter private String cnConcept;
  @Getter private String enConcept;
  @Getter private String alias;
  @Getter @Setter private String type;
  @Getter @Setter private List<Aom14Node> children;

  @Getter private boolean fromTemplate;
  @Getter private String archetypeId;
  @Getter private String namespace;
  @Getter private String rmEntity;

  @Getter private Archetype blank;
  // @Getter private final Map<String, Aom14Node> nodeMapForTerms;
  @Getter private final Map<String, Map<String, ArchetypeTerm>> termsMap;
  @Getter private final NodeId nodeId;

  @Getter private MindNode node;

  @Getter private String note;
  @Getter private List<String> codes;

  public Aom14Node toSlotNode() {
    Aom14Node slot = new Aom14Node(this.nodeId, this.termsMap);

    slot.type = "SLOT";
    slot.cnConcept = this.cnConcept;
    slot.enConcept = this.enConcept;
    slot.alias = this.alias;
    slot.namespace = this.namespace;
    slot.archetypeId = this.archetypeId;
    slot.node = this.node;
    slot.rmEntity = new ArchetypeID(this.archetypeId).rmEntity();
    // @Getter private String rmEntity;
    slot.fromTemplate = this.fromTemplate;
    slot.blank = this.blank;

    slot.children = this.children;

    slot.note = this.note;
    slot.codes = this.codes;

    return slot;
  }

  // isSpecified: isArchetype && isExternal
  //              - children contains in the definition: limit
  //              - some of the children not in definition: specified
  public Aom14Node(MindNode node, NodeId startId, Map<String, Map<String, ArchetypeTerm>> termsMap) throws Exception {
    this.node = node;
    if (startId == null) {
      startId = new NodeId(0);
    }
    //    if (nodeMapForTerms == null) {
    //      nodeMapForTerms = new TreeMap<>();
    //    }
    if (termsMap == null) {
      termsMap = new TreeMap<>();
    }
    this.nodeId = startId;
    //    this.nodeMapForTerms = nodeMapForTerms;
    this.termsMap = termsMap;
    this.cnConcept = node.getCnName();
    this.enConcept = node.getEnName();
    this.alias = node.getAlias();
    this.fromTemplate = node.isFromTemplate();

    this.processType();
    this.processChildren();

    this.note = StringUtils.trim(node.getTopic().getNote());
    this.processNote();
  }

  public void split(Map<String, Aom14Node> nodes, Set<String> ckmConceptIds) {
    if (this.isArchetype() && !this.isExternal() && this.type.startsWith(ArchetypeHelper.NAMESPACE)) {
      nodes.put(this.type, this);
    }
    if (this.isArchetype() && !this.type.startsWith(ArchetypeHelper.NAMESPACE)) {
      this.archetypeId = this.type;
      ckmConceptIds.add(this.type);
    }

    if (this.children != null) {
      for (int i = 0; i < this.children.size(); i++) {
        Aom14Node child = this.children.get(i);
        if (child.isArchetype() || "cluster".equals(child.type.toLowerCase())) {
          child.split(nodes, ckmConceptIds);
        }
        if (child.isArchetype()) {
          this.children.set(i, child.toSlotNode());
        }
      }
    }
  }


  public Map<String, Aom14Node> flattenAom14Node(Repo repo, String pathBase, boolean withReference) {
    Map<String, Aom14Node> pathToNode = new LinkedHashMap<>();

    List<Aom14Node> children = this.getChildren();
    if (children != null && children.size() > 0) {
      for (Aom14Node child : children) {
        String path = pathBase + Constant.TOPIC_NAME_SEPARATOR + child.getEnConcept();
        if (child.isArchetype()) {
          pathToNode.put(path, child);
          if (withReference) {
            if (child.getChildren() != null && child.getChildren().size() > 0) {
              pathToNode.putAll(child.flattenAom14Node(repo, path, withReference));
            } else { // FIXME: set template reference
              Aom14Node reference = null;
              for (Aom14Node template : repo.getTemplates().values()) {
                // template.getNamespace().equals(child.getNamespace())
                if (template.getArchetypeId().equals(child.getArchetypeId())) {
                  reference = template;
                  break;
                }
              }
              if (reference == null) {
                String id = child.getNamespace() + "::" + child.getArchetypeId();
                reference = repo.getIdToNodeMap().get(id);
              }
              pathToNode.putAll(reference.flattenAom14Node(repo, path, withReference));
            }
          }
        } else if ("CLUSTER".equals(child.getType())) {
          pathToNode.put(path, child);
          pathToNode.putAll(child.flattenAom14Node(repo, path, withReference));
        } else {
          pathToNode.put(path, child);
        }
      }
    } else {
      // FIXME:
      System.out.println(this.getNode());
    }

    return pathToNode;
  }

  public Archetype toArchetype() {
    if (!this.isArchetype()) {
      return null;
    }

    this.nodeId.seq = -1;
    //    this.nodeMapForTerms.clear();
    this.termsMap.clear();

    Archetype archetype = null;
    try {
      ArchetypeID archetypeId = new ArchetypeID(this.archetypeId);

      this.blank = ArchetypeHelper.loadBlank(archetypeId.rmName(), archetypeId.rmEntity());

      this.nodeId.seq = ArchetypeHelper.findAndRemoveByRmType(this.blank, "ELEMENT");
      CComplexObject definition = ArchetypeHelper.toCcomplexObject(this);
      ArchetypeOntology ontology = ArchetypeHelper.toOntology(this);

      HierObjectID uid = this.blank.getUid();
      Set<Assertion> invariants = this.blank.getInvariants();
      String concept = this.blank.getConcept();

      CodePhrase originalLang = this.blank.getOriginalLanguage();
      Map<String, TranslationDetails> translations = this.blank.getTranslations();
      ResourceDescription description = this.blank.getDescription();
      RevisionHistory revisions = this.blank.getRevisionHistory();

      boolean isControlled = this.blank.isControlled();
      TerminologyService terminologyService = SimpleTerminologyService.getInstance();

      archetype = new Archetype(ArchetypeHelper.ADL_VERSION, archetypeId.getValue(), null, concept,
            originalLang, translations, description, revisions, isControlled,
            uid, definition, ontology, invariants, terminologyService);

    } catch (Exception e) {
      e.printStackTrace();
    }

    this.archetypeId = archetype.getArchetypeId().toString();
    return archetype;
  }

  public boolean isRepeat() {
    return this.node.isRepeat();
  }

  public boolean isArchetype() {
    return this.archetypeId != null;
  }

  public boolean isExternal() {
    return this.isArchetype() && node.isExternalReference();
  }

  public int getAndIncNodeId() {
    this.nodeId.seq += 1;
    return this.nodeId.seq - 1;
  }

  private Aom14Node(NodeId startId, Map<String, Map<String, ArchetypeTerm>> termsMap) {
    this.nodeId = startId;
    //    this.nodeMapForTerms = nodeMapForTerms;
    this.termsMap = termsMap;
  }

  // FIXME: Ugly code here
  private void processNote() throws Exception {
    Gson gson = new Gson();
    if (StringUtils.isNotEmpty(this.note)) {
      try {
        Map<String, Object> objectMap = gson.fromJson(this.note, Map.class);
        if (objectMap.containsKey("codes")) {
          if (objectMap.get("codes") instanceof List) {
            List<String> strings = (List<String>) (objectMap.get("codes"));

            if (strings.size() == 0) {
              this.codes = null;
            } else {
              this.codes = strings.stream()
                    .filter(code -> StringUtils.isNotEmpty(code))
                    .collect(Collectors.toList());
            }
          } else {
            System.err.println("Json format error in note/codes at " + this.node.getTopic().toString());
          }

          System.out.println(codes);
        }
      } catch (JsonSyntaxException jse) {
        System.err.println("Json format error in note at " + this.node.getTopic().toString());
      }
    }
  }

  private void processType() throws Exception {
    this.type = this.node.getType();
    if (ArchetypeHelper.DATA_TYPE_MAPPING.containsKey(this.type)) {
      this.type = ArchetypeHelper.DATA_TYPE_MAPPING.get(this.type);
    } else {
      Matcher matcher = ArchetypeHelper.CONCEPT_ID_PATTERN.matcher(this.type);
      if (matcher.matches()) {
        String namespace = matcher.group(2);
        String archetypeId = matcher.group(3);
        if (ArchetypeHelper.ARCHETYPE_ID_PATTERN.matcher(archetypeId).matches()) {
          this.namespace = namespace;
          this.archetypeId = archetypeId;
        }
      } else {
        throw Constant.FORMAT_ERROR_LABELS;
      }
    }
  }

  private void processChildren() throws Exception {
    if (this.node.getChildren() != null) {
      this.children = new ArrayList<>();
      for (MindNode subNode : node.getChildren()) {
        this.children.add(new Aom14Node(subNode, this.nodeId, this.termsMap));
      }
    }
  }

  protected class NodeId {
    @Getter private int seq;

    NodeId(int seq) {
      this.seq = seq;
    }
  }
}
