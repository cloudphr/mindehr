package cn.ac.bmi.cloudphr.mindmap.parser;

import static org.junit.jupiter.api.Assertions.*;

import cn.ac.bmi.cloudphr.mindmap.model.Sheet;
import cn.ac.bmi.cloudphr.mindmap.model.Topic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XmindParserTest {

  private ClassLoader classLoader;
  private MindmapParser parser;

  @BeforeEach
  public void setUp() throws Exception {
    this.parser = new XmindParser();
    this.classLoader = this.getClass().getClassLoader();
    assertNotNull(this.parser);
    assertNotNull(this.classLoader);
  }

  @Test
  public void testFileNotFoundException() {
    String pathNotExist = "notExist.xmind";
    Sheet[] sheets = this.parser.parse(pathNotExist);
    assertNotNull(sheets);
    assertEquals(sheets.length, 0);
  }

  @Test
  public void testNullStringAsPath() {
    String nullStringAsPath = null;
    Sheet[] sheets = this.parser.parse(nullStringAsPath);
    assertNotNull(sheets);
    assertEquals(sheets.length, 0);
  }

  @Test
  public void testBadXmindContent() {
    File file = getFileInResources("mindmap/bad.content.xmind");
    Sheet[] sheets = this.parser.parse(file.getAbsolutePath());
    assertNull(sheets);
  }

  @Test
  public void testParseNotXmindFile() {
    File file = getFileInResources("mindmap/notxmind.xmind");
    Sheet[] sheets = this.parser.parse(file.getAbsolutePath());
    assertNull(sheets);
  }

  @Test
  public void testParseClosedInputStream() {
    File file = getFileInResources("mindmap/blank.xmind");
    try {
      InputStream inputStream = new FileInputStream(file);
      try {
        inputStream.close();
      } catch (IOException ioe) {
        assert false;
      }
      Sheet[] sheets = this.parser.parse(inputStream);
      assertNull(sheets);
    } catch (FileNotFoundException e) {
      assert false;
    }
  }

  @Test
  public void testParseNullFileInputStream() {
    InputStream inputStream = null;
    Sheet[] sheets = this.parser.parse(inputStream);
    assertNull(sheets);
  }

  @Test
  public void testParseBlank() {
    File file = getFileInResources("mindmap/blank.xmind");
    Sheet[] sheets = this.parser.parse(file.getAbsolutePath());
    assertNotNull(sheets);
    assertEquals(1, sheets.length);
    assertEquals("Sheet 1", sheets[0].getTitle());
    assertNotNull(sheets[0].getTopic());

    Topic root = sheets[0].getTopic();
    assertEquals("blank", root.getTitle());
    assertEquals(0, root.getTopics().length);
    assertEquals(sheets[0], root.getBelongTo());
    assertEquals("7d9a0bdc61892a4af9df3ba770", root.getId());
    assertNull(root.getLabels());
    assertNull(root.getLink());
    assertNull(root.getNote());
  }

  @Test
  public void testParseSimpleXmindFileUsingTestHelper() throws Exception {
    File file = getFileInResources("mindmap/simple-1.xmind");
    Sheet[] sheets = this.parser.parse(file.getAbsolutePath());
    /* Test 'sheets' is same as expected */
    assertNotNull(sheets);
    assertEquals(1, sheets.length);
    assertEquals("Only&*Sheet", sheets[0].getTitle());
    assertNotNull(sheets[0].getTopic());
    Topic firstTopic = sheets[0].getTopic();
    File jsonFile = new File(this.classLoader.getResource("mindmap/simple-1.json").getFile());
    Topic secondTopic = TestHelper.constructTopicFromJson(jsonFile.getAbsolutePath());
    assertTrue(TestHelper.topicsEquals(firstTopic, secondTopic));
  }

  @Test
  public void parserShouldNotReturnNullSheet() {
    File file = getFileInResources("mindmap/simple-1.xmind");
    Sheet[] sheets = this.parser.parse(file.getAbsolutePath());
    for (Sheet sheet : sheets) {
      assertNotNull(sheet);
    }
  }

  private File getFileInResources(String filename) {
    return new File(this.classLoader.getResource(filename).getFile());
  }
}
