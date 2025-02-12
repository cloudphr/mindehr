package cn.ac.bmi.cloudphr.mindmap.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class Sheet {
  @Getter private String title;
  @Getter private Topic topic;

  @Getter @Setter private Mindmap belongTo;
  @Getter private Map<String, Topic> topics = new HashMap<>();

  public Sheet(String title, Topic topic) {
    this.title = title;
    this.topic = topic;
  }

  public Sheet init() {
    title = title == null ? "" : title.trim();

    if (topic != null) {
      topics.put(topic.getId(), topic);
      topic.init(this);
    }
    return this;
  }
}