Issue acknowledged by Alfresco and fixed:   https://issues.alfresco.com/jira/browse/MNT-19773


# Alfresco VersionService - issue with marker aspects

## Description of the problem
Marker aspects are not persisted in frozen versions of nodes.

Example scenario where this is an issue: revert a node to a previous version, which had a marker aspect, the marker 
aspect is gone. 

## Reproduction
Alfresco with a custom model installed. This custom model contains a marker aspect: '{example.mode}marker'.

1. Create a first version of a node. This version has the marker aspect '{example.model}marker'.
2. Create a second version of that node. Don't touch the marker aspect. 
3. Revert the node to it's first version.
4. The marker aspect has now disappeared.

Reproduction of this issue in a Java test is available 
[here](src/test/java/eu/xenit/alfresco/VersionMarkerAspectIssueIT.java).
