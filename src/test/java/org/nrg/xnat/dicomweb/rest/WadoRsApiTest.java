package org.nrg.xnat.dicomweb.rest;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.dicomweb.service.XnatDicomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for WadoRsApi, specifically the study metadata endpoint fix
 */
public class WadoRsApiTest {

    @Mock
    private XnatDicomService mockDicomService;

    @Mock
    private UserManagementServiceI mockUserManagementService;

    @Mock
    private RoleHolder mockRoleHolder;

    @Mock
    private UserI mockUser;

    private WadoRsApi wadoRsApi;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        wadoRsApi = new WadoRsApi(mockDicomService, mockUserManagementService, mockRoleHolder) {
            @Override
            protected UserI getSessionUser() {
                return mockUser;
            }
        };
    }

    @Test
    public void testRetrieveStudyMetadata_ReturnsArrayOfInstances() {
        // Arrange
        String projectId = "TestProject";
        String studyUID = "1.2.3.4.5";

        // Create mock instance metadata (simulating multiple instances in a study)
        List<Attributes> mockInstances = createMockInstances(3);

        when(mockDicomService.retrieveAllStudyInstanceMetadata(any(UserI.class), eq(projectId), eq(studyUID)))
            .thenReturn(mockInstances);

        // Act
        ResponseEntity<String> response = wadoRsApi.retrieveStudyMetadata(projectId, studyUID);

        // Assert
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());

        String responseBody = response.getBody();
        assertTrue("Response should start with '['", responseBody.startsWith("["));
        assertTrue("Response should end with ']'", responseBody.endsWith("]"));

        // Verify it contains multiple instances (should have multiple SOP Instance UIDs)
        int sopInstanceUIDCount = countOccurrences(responseBody, "\"00080018\"");
        assertEquals("Should contain 3 SOP Instance UIDs", 3, sopInstanceUIDCount);
    }

    @Test
    public void testRetrieveStudyMetadata_NotFound() {
        // Arrange
        String projectId = "TestProject";
        String studyUID = "1.2.3.4.5";

        when(mockDicomService.retrieveAllStudyInstanceMetadata(any(UserI.class), eq(projectId), eq(studyUID)))
            .thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<String> response = wadoRsApi.retrieveStudyMetadata(projectId, studyUID);

        // Assert
        assertEquals("Should return 404 Not Found for empty result", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRetrieveStudyMetadata_EachInstanceHasSOPInstanceUID() {
        // Arrange
        String projectId = "TestProject";
        String studyUID = "1.2.3.4.5";

        List<Attributes> mockInstances = createMockInstances(2);

        when(mockDicomService.retrieveAllStudyInstanceMetadata(any(UserI.class), eq(projectId), eq(studyUID)))
            .thenReturn(mockInstances);

        // Act
        ResponseEntity<String> response = wadoRsApi.retrieveStudyMetadata(projectId, studyUID);

        // Assert
        String responseBody = response.getBody();
        assertNotNull(responseBody);

        // Each instance should have both SOP Instance UID (00080018) and SOP Class UID (00080016)
        assertTrue("Should contain SOP Instance UID tag", responseBody.contains("\"00080018\""));
        assertTrue("Should contain SOP Class UID tag", responseBody.contains("\"00080016\""));
    }

    @Test
    public void testRetrieveAllStudyInstanceMetadata_NoNullPointerException() {
        // Regression test: verify that retrieveAllStudyInstanceMetadata doesn't throw NPE
        // when called with valid parameters (unlike searchInstances which required non-null seriesUID)
        String projectId = "TestProject";
        String studyUID = "1.2.3.4.5";

        List<Attributes> mockInstances = createMockInstances(5);

        when(mockDicomService.retrieveAllStudyInstanceMetadata(any(UserI.class), eq(projectId), eq(studyUID)))
            .thenReturn(mockInstances);

        // Act - should not throw NullPointerException
        ResponseEntity<String> response = wadoRsApi.retrieveStudyMetadata(projectId, studyUID);

        // Assert
        assertEquals("Should successfully return all instances without NPE", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response should contain data", response.getBody());

        // Verify all 5 instances are in the response
        int sopInstanceUIDCount = countOccurrences(response.getBody(), "\"00080018\"");
        assertEquals("Should return all 5 instances", 5, sopInstanceUIDCount);
    }

    // Helper methods

    private List<Attributes> createMockInstances(int count) {
        List<Attributes> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Attributes attrs = new Attributes();
            attrs.setString(Tag.SOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2"); // CT Image Storage
            attrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5.6." + i);
            attrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4.5");
            attrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4.5.100");
            attrs.setString(Tag.Modality, VR.CS, "CT");
            attrs.setInt(Tag.InstanceNumber, VR.IS, i + 1);
            instances.add(attrs);
        }
        return instances;
    }

    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
