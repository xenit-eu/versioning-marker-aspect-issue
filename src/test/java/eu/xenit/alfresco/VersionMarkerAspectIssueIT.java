package eu.xenit.alfresco;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(value = AlfrescoTestRunner.class)
public class VersionMarkerAspectIssueIT extends AbstractAlfrescoIT {

    private final static String TEST_NODE_NAME = "Node with multiple versions and a marker aspect.txt";

    private final static QName MARKER_ASPECT = QName.createQName("{example.model}marker");

    /**
     * Cleanup possible residual test node from previous runs
     */
    @Before
    public void cleanupTestNodeIfExists() {
        final NodeRef existingTestNode = getServiceRegistry().getNodeService()
                .getChildByName(getCompanyHomeNodeRef(), ContentModel.ASSOC_CONTAINS, TEST_NODE_NAME);

        if (existingTestNode != null && getServiceRegistry().getNodeService().exists(existingTestNode)) {
            getServiceRegistry().getNodeService().deleteNode(existingTestNode);
        }
    }

    @Test
    public void reproduce() {

        /* STEP 1: Create a node, make sure it has a marker aspect, make sure it has a first version */

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, TEST_NODE_NAME);

        final NodeRef workingNodeRef = getServiceRegistry().getNodeService().createNode(
                getCompanyHomeNodeRef(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_NODE_NAME),
                ContentModel.TYPE_CONTENT,
                properties
        ).getChildRef();

        getServiceRegistry().getNodeService().addAspect(
                workingNodeRef,
                MARKER_ASPECT,
                Collections.emptyMap()
        );

        final Version firstVersion =
                getServiceRegistry().getVersionService().createVersion(workingNodeRef, Collections.emptyMap());

        assertTrue(getServiceRegistry().getNodeService().hasAspect(workingNodeRef, MARKER_ASPECT));
        // The damage has already been done, the marker aspect has not been persisted into the frozen version:
//        assertTrue(
//                getServiceRegistry().getNodeService().hasAspect(firstVersion.getFrozenStateNodeRef(), MARKER_ASPECT));


        /* STEP 2: Create a new version for our node, this version also has our marker aspect */
        Map<String, Serializable> versionProps = new HashMap<>();
        versionProps.put(Version2Model.PROP_VERSION_TYPE, VersionType.MAJOR);
        final Version secondVersion =
                getServiceRegistry().getVersionService().createVersion(workingNodeRef, versionProps);

        assertTrue(getServiceRegistry().getNodeService().hasAspect(workingNodeRef, MARKER_ASPECT));
        // Again, the marker aspect will not be persisted into the second version frozen state
//        assertTrue(
//                getServiceRegistry().getNodeService().hasAspect(secondVersion.getFrozenStateNodeRef(), MARKER_ASPECT));


        /* STEP 3: revert node to first version */
        getServiceRegistry().getVersionService().revert(workingNodeRef, firstVersion);

        // And poof, the marker aspect is gone!
        assertTrue(getServiceRegistry().getNodeService().hasAspect(workingNodeRef, MARKER_ASPECT));

    }

    /**
     * ==================== Helper Methods ============================================================================
     */

    /**
     * Get the node reference for the /Company Home top folder in Alfresco. Use the standard node locator service.
     *
     * @return the node reference for /Company Home
     */
    private NodeRef getCompanyHomeNodeRef() {
        return getServiceRegistry().getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
    }

}
