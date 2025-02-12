package cn.ac.bmi.cloudphr.mindmap.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MindmapParserTest {
  private MindmapParser parser;

  @BeforeEach
  public void setUp() throws Exception {
    this.parser = new MindmapParser() {
      @Override
      public Sheet[] parse(InputStream mindmapInputStream) {
        return new Sheet[0];
      }
    };
  }

  @Test
  public void testParseNotExistFile() {
    Sheet[] sheets = this.parser.parse("SOME_FILE_NOT_EXISTS");
    assertNotNull(sheets);
    assertEquals(sheets.length, 0);
  }
}