# Mindmap Specification of MindEHR

A mindmap file is always consist  by a set of sheets,
and each sheet has a root topic node which might have some child topic nodes.
We use the mindmap file to define multiple business, templates, and archetype models.

## 1. Sheet title
It is used to define the name of a Template or an Archetype.
 - `Txxx.TEMPLATE_NAME`: It represents that the content in this sheet is a common template,
    which would probably reused in the other business models or template models.
    One template file (oet) will be generated for each template sheet.
 - `Bxxx.BUSINESS_NAME`: It represents that the content in this sheet is a business model.
    It will be treated as a Template (oet) file.
    It always has a larger granularity than a common template.
 - `ARCHETYPE_NAME`: It represents that the content in this sheet is the definition of an Archetype.
    It is always used to define an archetype
    which is reused in the business models or template models more than once. 

## 2. Topic node

### 2.1 Topic title

The title of topic node is always used to define the concept name and property name.
It should be in the format of `EN_CONCEPT_NAME / OTHER_LANG_CONCEPT_NAME: varName`.
 - `EN_CONCEPT_NAME / OTHER_LANG_CONCEPT_NAME`: required.
    The `EN_CONCEPT_NAME` will be used as the concept name in English.
    The `OTHER_LANG_CONCEPT_NAME` will be used as the concept name in other languages.
    We only support English and Chinese for now.
    That means the `OTHER_LANG_CONCEPT_NAME` is only used for concept name in Chinese.
 - `varName`: optional. It is used to define the property name of an item.
    If the property name is not defined,
    the english concept name will be converted into the **camel** format
    and be used as the property name.
 - `:`: The colon is used to separate the concept name and the variable name.
    If the variable name is not defined, the colon is not required.

### 2.2 Label of topic
The labels primarily record type information.
 - No Label is Present
   - If the node is a leaf node, it is a property node which has the `Text` type.
   - If the node is a non-leaf node, it will be treated as an internal `CLUSTER`.
 - With labels
   - each node allows for a maximum of two types of information: 1) concept ID or type name, 2) repeat.
     Furthermore, when two types of information are present, one of them must be "repeat".
   - **`Concept ID`** for non-leaf node: It is the concept IDs (such as openEHR-EHR-OBSERVATION.blood_pressure.v2, etc.).
     It is used to indicate the type or concept name of the node.
   - **`Type name`** for leaf node: It is used to indicate the type of the node.
     It will be mapped to the basic openEHR RM type names with the following rules:
     - `Duration` -> `DV_DURATION`,
     - `Quantity`-> `DV_QUANTITY`,
     - `Text`-> `DV_TEXT`,
     - `CodedText`-> `DV_CODED_TEXT`,
     - `Date`-> `DV_DATE`,
     - `DateTime`-> `DV_DATE_TIME`,
     - `Boolean`-> `DV_BOOLEAN`,
     - `Multimedia`-> `DV_MULTIMEDIA`,
     - `Terminology`-> `DV_CODED_TEXT`

### 2.3 Link in topic
 - External Reference: If a topic node has a link and the label contains an archetype ID,
   it signifies an external reference.
   - If a node has some child nodes, the content of child nodes represents the result of applying external reference constraints.
   - If a node does not have child nodes, it implies the inclusion of all content from the external reference.
 - Template Reference: If the link points to a template with the name "Txxx.",
   it denotes a template reference.
