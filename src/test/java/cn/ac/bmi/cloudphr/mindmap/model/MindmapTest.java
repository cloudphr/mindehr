package cn.ac.bmi.cloudphr.mindmap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class MindmapTest {
  @Test
  public void testObjectCreation() {
    Mindmap mindmap = new Mindmap(new Sheet[0]);
    assertNotNull(mindmap);
    Sheet[] sheets = mindmap.getSheets();
    assertNotNull(sheets);
    assertEquals(sheets.length, 0);
  }

  @Test
  public void nullSheetInSheetsShouldNotFailMindmapCreation() {
    Sheet[] sheets = new Sheet[2];
    sheets[0] = null;
    sheets[1] = null;
    Mindmap mindmap = new Mindmap(sheets);
    assertNotNull(mindmap);
  }

  @Test
  public void temp() {
    Topic topic = new Topic("", null);
    Sheet sheet = new Sheet("NO", topic);
    topic.setBelongTo(sheet);
    Sheet[] sheets = {sheet};
    Mindmap mindmap = new Mindmap(sheets);
    assertNotNull(mindmap);
  }
}