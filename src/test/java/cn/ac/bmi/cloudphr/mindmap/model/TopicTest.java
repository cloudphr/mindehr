package cn.ac.bmi.cloudphr.mindmap.model;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TopicTest {

  @Test
  public void topicCouldHaveNoBelongTo1() {
    Topic topic = new Topic("For Test", null);
    assertNotNull(topic);
    assertNull(topic.getBelongTo());
  }

  @Test
  public void topicCouldHaveNoBelongTo2() {
    Topic topic = new Topic("For Test",  "real", null);
    assertNotNull(topic);
    assertNull(topic.getBelongTo());
  }

  @Test
  public void topicToString() {
    Topic topic = new Topic("For Test",  "real", null);
    assertNotNull(topic);
    assertTrue(topic.toString().contains("For Test"));
  }
}
