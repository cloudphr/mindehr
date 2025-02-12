package cn.ac.bmi.cloudphr.mindehr.helper;

import cn.ac.bmi.cloudphr.mindehr.model.Aom14Node;
import cn.ac.bmi.cloudphr.mindehr.model.MindNode;
import cn.hutool.log.StaticLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.assertion.Assertion;
import org.openehr.am.archetype.assertion.ExpressionBinaryOperator;
import org.openehr.am.archetype.assertion.ExpressionItem;
import org.openehr.am.archetype.assertion.ExpressionLeaf;
import org.openehr.am.archetype.assertion.OperatorKind;
import org.openehr.am.archetype.constraintmodel.ArchetypeSlot;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CMultipleAttribute;
import org.openehr.am.archetype.constraintmodel.CObject;
import org.openehr.am.archetype.constraintmodel.CSingleAttribute;
import org.openehr.am.archetype.constraintmodel.Cardinality;
import org.openehr.am.archetype.constraintmodel.primitive.CString;
import org.openehr.am.archetype.ontology.ArchetypeOntology;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.archetype.ontology.OntologyDefinitions;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity;
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase;
import org.openehr.am.serialize.ADLSerializer;
import org.openehr.rm.support.basic.Interval;
import se.acode.openehr.parser.ADLParser;
import se.acode.openehr.parser.ParseException;

public class ArchetypeHelper {
  private static ADLSerializer serializer = new ADLSerializer();

  public static Map<String, String> DATA_TYPE_MAPPING = new HashMap<String, String>() {
    {
      put("boolean", "DV_BOOLEAN");
      put("codedtext", "DV_CODED_TEXT");
      put("count", "DV_COUNT");
      put("date", "DV_DATE");
      put("datetime", "DV_DATE_TIME");
      put("duration", "DV_DURATION");
      put("identifier", "DV_IDENTIFIER");
      put("multimedia", "DV_MULTIMEDIA");
      put("ordinal", "DV_ORDINAL");
      put("quantity", "C_DV_QUANTITY");
      put("terminology", "DV_CODED_TEXT");
      put(MindNode.DEFAULT_SIMPLE_TYPE_NAME, "DV_TEXT");
      put(MindNode.DEFAULT_OBJECT_TYPE_NAME, "CLUSTER");
    }
  };

  public static final Set<String> ENTRY_TYPES = new HashSet<>(
        Arrays.asList("admin_entry", "action", "instruction", "evaluation", "observation")
  );
  public static final String ADL_VERSION = "1.4";
  public static final String NAMESPACE = "cn.ac.bmi";
  public static final Pattern CONCEPT_ID_PATTERN = Pattern.compile("^(([^:]+)::)?([^:]+)$");
  public static final Pattern ARCHETYPE_ID_PATTERN = Pattern.compile("^([^.]+)\\.([^.]+)\\.([^.]+)$");
  public static final Pattern RM_NAMES_PATTERN = Pattern.compile("^([^-]+)-([^-]+)-([^-]+)$");
  public static final Pattern NODEID_PATTERN = Pattern.compile("^at(0)*(\\d)+$");

  public static Archetype loadBlank(String rmName, String rmEntity) {
    Archetype blank = null;
    String blankResource = "openehr/archetypes/openEHR-" + rmName + "-" + rmEntity + ".blank.v0.adl";
    ClassLoader classloader = ArchetypeHelper.class.getClassLoader();
    InputStream blankFileInputStream = classloader.getResourceAsStream(blankResource);
    ADLParser parser = null;
    try {
      parser = new ADLParser(blankFileInputStream, true, true);
      blank = parser.parse();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return blank;
  }

  public static CComplexObject toCcomplexObject(Aom14Node node) throws Exception {
    // TODO: hard work here!
    CComplexObject definition = (CComplexObject) node.getBlank().getDefinition().copy();
    CAttribute items = ArchetypeHelper.locateAttribute(definition, "items");

    if (items != null) {
      List<CAttribute> attributes = ArchetypeHelper.createItemsAttributes(node, items.parentNodePath());
      for (CObject child : attributes.get(0).getChildren()) {
        items.addChild(child);
      }
    }

    return definition;
  }

  private static List<CAttribute> createItemsAttributes(Aom14Node node, String parentPath) throws Exception {
    String rmAttributeName = "items";
    Cardinality cardinality = new Cardinality(false, true, new Interval<>(1, null));
    List<CObject> children = new ArrayList<>();
    CAttribute items = new CMultipleAttribute(parentPath + CAttribute.PATH_SEPARATOR + "items",
          rmAttributeName, CAttribute.Existence.REQUIRED, cardinality, children);

    for (Aom14Node child : node.getChildren()) {
      String nodeId = String.format("at%04d", child.getAndIncNodeId());

      String path = items.childNodePathBase() + "[" + nodeId + "]";
      Interval<Integer> occurrences = new Interval<>(0, child.isRepeat() ? null : 1);
      List<CAttribute> attributes = null;
      CObject childObject = null;
      switch (child.getType()) {
        case "SLOT":
          Set<Assertion> includes = ArchetypeHelper.createMatchesAssertion(
                new HashSet<>(Arrays.asList(child.getArchetypeId())), false
          );
          childObject = new ArchetypeSlot(path, child.getRmEntity(), occurrences, nodeId, items, includes, null);
          break;
        case "CLUSTER":
          attributes = ArchetypeHelper.createItemsAttributes(child, items.childNodePathBase());
          childObject = new CComplexObject(path, "CLUSTER", occurrences, nodeId, attributes, items);
          break;
        default:
          attributes = ArchetypeHelper.createPrimitiveAttributes(child, path);
          childObject = new CComplexObject(path, "ELEMENT", occurrences, nodeId, attributes, items);
          break;
      }

      items.getChildren().add(childObject);

      if (child != null) {
        String code = childObject.getNodeId();
        ArchetypeTerm enTerm = new ArchetypeTerm(code, child.getEnConcept(), "");
        ArchetypeTerm cnTerm = new ArchetypeTerm(code, child.getCnConcept(), "");
        Map<String, ArchetypeTerm> dummy = new HashMap<>();
        dummy.put("en", enTerm);
        dummy.put("cn", cnTerm);
        node.getTermsMap().put(code, dummy);
        //        node.getNodeMapForTerms().put(childObject.getNodeId(), child);
      }
    }

    return Arrays.asList(items);
  }

  public static void flattenCobject(
        CObject node,
        String pathBase,
        Map<String, CObject> excludes,
        Map<String, CObject> primitives,
        Map<String, CObject> references,
        Map<String, Aom14Node> aom14NodeMap,
        Map<String, String> nodeIdToConceptNameMap) {
    if (node instanceof CComplexObject) {
      for (CAttribute attribute : ((CComplexObject) node).getAttributes()) {
        for (CObject child : attribute.getChildren()) {
          String path = pathBase + Constant.TOPIC_NAME_SEPARATOR + nodeIdToConceptNameMap.get(child.getNodeId());
          if (aom14NodeMap.containsKey(path)) {
            primitives.put(path, child);
            // FIXME:
            if (!"ELEMENT".equals(child.getRmTypeName())) {
              flattenCobject(child, path, excludes, primitives, references, aom14NodeMap, nodeIdToConceptNameMap);
            }
          } else {
            excludes.put(path, child);
          }
        }
      }
    } else if (node instanceof ArchetypeSlot) {
      Set<Assertion> assertions = ((ArchetypeSlot) node).getIncludes();
      Aom14Node aom14 = aom14NodeMap.get(pathBase);
      if (aom14.isFromTemplate()) {
        System.out.println("----------------- From template");
      }


      // FIXME: if it is allowed to put
      //        if it is a template or an archetype?
      if (true) {
        references.put(pathBase, node);
      }
    } else if (node instanceof CCodePhrase) {
      StaticLog.warn("Going to remove " + node.getRmTypeName() + ", " + node.path());
    } else {
      StaticLog.warn("Not supported rmType " + node.getRmTypeName() + ", " + node.path());
    }

    return;
  }

  public static int findAndRemoveByRmType(Archetype blank, String rmType) {
    int nodeIdSeq = -1;
    CComplexObject definition = blank.getDefinition();
    CAttribute items = ArchetypeHelper.locateAttribute(definition, "items");
    if (items != null) {
      for (CObject object : items.getChildren()) {
        if (rmType.equals(object.getRmTypeName())) {
          String nodeId = object.getNodeId();
          Matcher m = ArchetypeHelper.NODEID_PATTERN.matcher(nodeId);
          if (m.find()) {
            nodeIdSeq = Integer.parseInt(m.group(2));
            ArchetypeHelper.removeNodeFromOntology(blank, nodeId);
            items.removeChild(object);
          } else {
            StaticLog.error("Some error exists in archetype template [" + blank.getArchetypeId().getValue() + "]!");
          }
          break;
        }
      }
      items.removeAllChildren();
    }
    return nodeIdSeq;
  }

  public static ArchetypeOntology toOntology(Aom14Node node) {
    Archetype blank = node.getBlank();
    List<ArchetypeTerm> enTerms = new ArrayList<>();
    List<ArchetypeTerm> cnTerms = new ArrayList<>();
    List<OntologyDefinitions> blankDefinitionsList = blank.getOntology().getTermDefinitionsList();
    for (OntologyDefinitions definitions : blankDefinitionsList) {
      switch (definitions.getLanguage()) {
        case "en":
          enTerms = definitions.getDefinitions();
          break;
        case "zh":
          cnTerms = definitions.getDefinitions();
          break;
        default:
          break;
      }
    }

    for (ArchetypeTerm term : enTerms) {
      if (term.getCode().equals(blank.getConcept())) {
        term.getItems().put("text", node.getEnConcept());
      }
    }

    for (ArchetypeTerm term : cnTerms) {
      if (term.getCode().equals(blank.getConcept())) {
        term.getItems().put("text", node.getCnConcept());
      }
    }

    Map<String, Map<String, ArchetypeTerm>> nodeMapForTerms = node.getTermsMap();
    for (String code : nodeMapForTerms.keySet()) {
      Map<String, ArchetypeTerm> termNode = nodeMapForTerms.get(code);
      //      ArchetypeTerm enTerm = new ArchetypeTerm(code, termNode.getEnConcept(), "");
      //      ArchetypeTerm cnTerm = new ArchetypeTerm(code, termNode.getCnConcept(), "");
      ArchetypeTerm enTerm = termNode.get("en");
      ArchetypeTerm cnTerm = termNode.get("cn");

      enTerms.add(enTerm);
      cnTerms.add(cnTerm);
    }

    List<OntologyDefinitions> termDefinitionsList = new ArrayList<>();
    termDefinitionsList.add(new OntologyDefinitions("en", enTerms));
    termDefinitionsList.add(new OntologyDefinitions("zh", cnTerms));

    return new ArchetypeOntology(
          null,
          null,
          termDefinitionsList,
          null,
          null,
          null
    );
  }

  public static int removeNodeFromOntology(Archetype archetype, String nodeId) {
    int count = 0;
    List<OntologyDefinitions> blankDefinitionsList = archetype.getOntology().getTermDefinitionsList();
    for (OntologyDefinitions definitions : blankDefinitionsList) {
      for (ArchetypeTerm term : definitions.getDefinitions()) {
        if (term.getCode().equals(nodeId)) {
          definitions.getDefinitions().remove(term);
          count++;
          break;
        }
      }
    }
    return count;
  }

  public static Set<Assertion> createMatchesAssertion(Set<String> conceptIds, boolean includeSpecializations) {
    String pattern = ".*";

    if (conceptIds != null && conceptIds.size() > 0) {
      Set<String> extended = new HashSet<>();
      for (String conceptId : conceptIds) {
        if (StringUtils.countMatches(conceptId, ".") != 2) {
          StaticLog.warn("concept id should have two '.'s");
        } else {
          if (includeSpecializations) {
            int index = conceptId.lastIndexOf('.');
            conceptId = conceptId.substring(0, index) + "(-[a-zA-Z0-9_]+)*" + conceptId.substring(index);
          }
          extended.add(conceptId);
        }
      }
      pattern = String.join("|", extended)
            .replace(".", "\\.")
            .replace(" ", "");
    }

    String type = "Boolean";
    OperatorKind operator = OperatorKind.OP_MATCHES;
    ExpressionItem left = new ExpressionLeaf(
          "String", "archetype_id/value", ExpressionLeaf.ReferenceType.ATTRIBUTE
    );
    CString item = new CString(pattern);
    ExpressionItem right = new ExpressionLeaf("C_STRING", item, ExpressionLeaf.ReferenceType.CONSTRAINT);
    ExpressionBinaryOperator expression = new ExpressionBinaryOperator(type, operator, false, left, right);

    Set<Assertion> assertions = new HashSet<>(Arrays.asList(new Assertion(expression, expression.toString())));
    return assertions;
  }

  public static List<CAttribute> createPrimitiveAttributes(Aom14Node node, String path) throws Exception {
    CAttribute value = new CSingleAttribute(path + CComplexObject.PATH_SEPARATOR + "value",
          "value", CAttribute.Existence.REQUIRED);
    CObject child = null;
    String childPath = path + CComplexObject.PATH_SEPARATOR + "value";
    if ("C_DV_QUANTITY".equals(node.getType())) {
      child = CDvQuantity.anyAllowed(childPath);
    } else if ("DV_IDENTIFIER".equals(node.getType())) {
      // FIXME:
      // child = new DvIdentifier();
      StaticLog.warn("FIXME: DV_IDENTIFIER not supported yet!");
      child = CComplexObject.createSingleRequired(childPath, "DV_TEXT");
    } else if ("DV_CODED_TEXT".equals(node.getType())) {
      child = CComplexObject.createSingleRequired(childPath, node.getType());

      String definingCodePath = childPath + "/" + "defining_code";
      CAttribute definingCode = CSingleAttribute.createRequired(definingCodePath, "defining_code");

      CCodePhrase ccp = null;
      List<String> codes = node.getCodes();
      if (codes != null && codes.size() > 0) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < codes.size(); i++) {
          String codeString = codes.get(i);
          String[] cnEnCodeStrings = codeString.split("/");
          String code = String.format("at%04d", node.getAndIncNodeId());
          ids.add(code);
          ArchetypeTerm cnTerm = new ArchetypeTerm(code, cnEnCodeStrings[0], "");
          String enCode = cnEnCodeStrings.length > 1 ? cnEnCodeStrings[1] : cnEnCodeStrings[0];
          ArchetypeTerm enTerm = new ArchetypeTerm(code, enCode, "");
          Map<String, ArchetypeTerm> dummy = new HashMap<>();
          dummy.put("en", enTerm);
          dummy.put("cn", cnTerm);
          node.getTermsMap().put(code, dummy);
        }
        ccp = CCodePhrase.singleRequired(definingCodePath, "local", ids);
      } else {
        ccp = new CCodePhrase(definingCodePath, "local", "");
      }

      definingCode.addChild(ccp);
      ((CComplexObject) child).addAttribute(definingCode);
    } else if ("DV_ORDINAL".equals(node.getType())) {
      Interval<Integer> occurrences = new Interval<>(0, node.isRepeat() ? null : 1);
      child = new CDvOrdinal(childPath, occurrences, null);
    } else {
      child = CComplexObject.createSingleRequired(childPath, node.getType());
    }
    value.addChild(child);
    return Arrays.asList(value);
  }

  public static CAttribute locateAttribute(CObject object, String attrName) {
    if (object instanceof CComplexObject) {
      for (CAttribute attribute : ((CComplexObject) object).getAttributes()) {
        if (attrName.equals(attribute.getRmAttributeName())) {
          return attribute;
        } else {
          for (CObject child : attribute.getChildren()) {
            CAttribute childAttr = locateAttribute(child, attrName);
            if (childAttr != null) {
              return childAttr;
            }
          }
        }
      }
    }

    return null;
  }

  public static String getRmEntity(String archetypeId) {
    Matcher matcher = ARCHETYPE_ID_PATTERN.matcher(archetypeId);
    matcher.matches();
    Matcher rmMatcher = RM_NAMES_PATTERN.matcher(matcher.group(1));
    rmMatcher.matches();
    return rmMatcher.group(3);
  }

  public static int serializeAdls(Collection<Aom14Node> nodes, String repoPath) {
    int count = 0;
    for (Aom14Node node : nodes) {
      Archetype archetype = node.toArchetype();
      ArchetypeHelper.serialize(archetype, repoPath);
      count++;
    }

    return count;
  }

  private static void serialize(Archetype archetype, String repoDir) {
    String rmEntity = archetype.getArchetypeId().rmEntity();
    String dirname = repoDir + "/archetypes";
    switch (rmEntity.toLowerCase()) {
      case "composition":
      case "section":
      case "cluster":
        dirname += "/" + rmEntity.toLowerCase();
        break;
      case "admin_entry":
      case "action":
      case "evaluation":
      case "instruction":
      case "observation":
        dirname += "/entry/" + rmEntity.toLowerCase();
        break;
      default:
        break;
    }
    String filename = dirname + "/" + archetype.getArchetypeId().getValue() + ".adl";
    File file = new File(filename);
    file.getParentFile().mkdirs();
    try {
      FileOutputStream output = new FileOutputStream(file);
      ArchetypeHelper.serializer.output(archetype, output);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Map<String, Archetype> loadArchetypesFromAdls(String repoPath) {
    Map<String, Archetype> archetypes = new TreeMap<>();
    Collection<File> files = FileUtils.listFiles(new File(repoPath), new String[] { "adl" }, true);
    for (File file : files) {
      try {
        Archetype archetype = new ADLParser(file).parse();
        if (archetype != null) {
          archetypes.put(archetype.getArchetypeId().toString(), archetype);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return archetypes;
  }

  public static int downloadFromCkm(Set<String> ckmConceptIds, String repoPath) {
    int count = 0;
    for (String conceptId : ckmConceptIds) {
      String url = ArchetypeHelper.getUrlById(conceptId);
      String filePath = repoPath + "/ckm/" + conceptId + ".adl";
      File file = new File(filePath);
      file.getParentFile().mkdirs();
      try {
        StaticLog.info("Going to download Archetype from " + url);
        FileUtils.copyURLToFile(new URL(url), file);
        count++;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return count;
  }

  public static Map<String, String> getNodeIdToConceptNameMap(Archetype archetype) {
    List<OntologyDefinitions> definitions = archetype.getOntology().getTermDefinitionsList();
    Map<String, String> nodeIdConceptNameMap = new LinkedHashMap<>();
    for (OntologyDefinitions definition : definitions) {
      if ("en".equals(definition.getLanguage())) {
        for (ArchetypeTerm term : definition.getDefinitions()) {
          nodeIdConceptNameMap.put(term.getCode(), term.getText());
        }
        break;
      }
    }
    return nodeIdConceptNameMap;
  }

  private static String getUrlById(String id) {
    String type = getRmEntity(id).toLowerCase();
    if (ArchetypeHelper.ENTRY_TYPES.contains(type)) {
      type = "entry/" + type;
    }
    return (Constant.LOCAL_CKM_SERVER == null ? Constant.CKM_URL_BASE :
          Constant.LOCAL_CKM_SERVER) +  "/local/archetypes/" + type + "/" + id + ".adl";
  }
}