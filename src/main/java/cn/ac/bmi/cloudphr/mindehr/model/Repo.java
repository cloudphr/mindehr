package cn.ac.bmi.cloudphr.mindehr.model;

import cn.ac.bmi.cloudphr.mindehr.helper.ArchetypeHelper;
import cn.ac.bmi.cloudphr.mindmap.model.Mindmap;
import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import cn.ac.bmi.cloudphr.mindmap.model.Topic;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Getter;
import org.openehr.am.archetype.Archetype;

public class Repo {
  @Getter private String repoPath;
  private Mindmap mindmap;

  Set<String> ckmConceptIds = new TreeSet<>();

  @Getter private Map<String, Aom14Node> idToNodeMap = new TreeMap<>();
  private Map<String, Aom14Node> businesses = new TreeMap<>();
  @Getter private Map<String, Aom14Node> templates = new LinkedHashMap<>();

  @Getter private Map<String, String> templateNameToIdMap = new TreeMap<>();

  private Map<String, Archetype> archetypes = new TreeMap<>();

  private static final String BUSINESS_PATTERN = "^B[0-9]+\\..*$";
  private static final String TEMPLATE_PATTERN = "^T[0-9]+\\..*$";


  public Repo(Mindmap mindmap, String repoPath) {
    this.repoPath = repoPath;
    this.mindmap = mindmap;
    if (this.mindmap.getSheets() != null) {
      for (Sheet sheet : this.mindmap.getSheets()) {
        Topic topic = sheet.getTopic();
        try {
          MindNode node = new MindNode(topic);
          Aom14Node aom = new Aom14Node(node, null, null);
          aom.split(this.idToNodeMap, this.ckmConceptIds);

          String title = sheet.getTitle();
          if (title.matches(Repo.BUSINESS_PATTERN) || title.matches(Repo.TEMPLATE_PATTERN)) {
            Aom14Node fullAom = new Aom14Node(node, null, null);
            this.templates.put(title, fullAom);
            if (title.matches(Repo.BUSINESS_PATTERN)) {
              this.businesses.put(title, fullAom);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public Repo generateArchetypes() {
    ArchetypeHelper.serializeAdls(this.idToNodeMap.values(), this.repoPath);
    ArchetypeHelper.downloadFromCkm(ckmConceptIds, this.repoPath);

    return this;
  }

  public Repo loadArchetypes() {
    this.archetypes = ArchetypeHelper.loadArchetypesFromAdls(this.repoPath);

    return this;
  }

  public Archetype getArchetypeById(String archetypeId) {
    return this.archetypes.get(archetypeId);
  }

}