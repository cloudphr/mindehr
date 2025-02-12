package cn.ac.bmi.cloudphr.mindehr.helper;

import java.util.Map;

public class Constant {
  public static final String REPEAT_LABEL = "repeat".toLowerCase();

  public static final char TOPIC_NAME_SEPARATOR = '/';
  public static final char TOPIC_ALIAS_SEPARATOR = ':';

  public static final Map<String, String> SPECIFICATION = Map.of(
        "title", "cn " + TOPIC_NAME_SEPARATOR + " en" + TOPIC_ALIAS_SEPARATOR + " alias",
        "labels", "[repeat][,TYPE_NAME]"
  );

  public static String LOCAL_CKM_SERVER = null;
  public static final String CKM_URL_BASE = "https://raw.githubusercontent.com/openEHR/CKM-mirror/master";
  public static final String CKM_RESOURCES_URL = "https://www.openehr.org/ckm/retrieveResources?list=true";

  public static final Exception FORMAT_ERROR_TITLE = new Exception(
        "Topic title should be in the format [" + SPECIFICATION.get("title") + "]");
  public static final Exception FORMAT_ERROR_LABELS = new Exception("Only one type and 'repeat' can appear in labels!");
  public static final Exception DATATYPE_ERROR_IDENTIFIER = new Exception("FIXME please! Data type not supportted yet");
}