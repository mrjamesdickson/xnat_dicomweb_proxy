package org.nrg.xnat.dicomweb.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.nrg.xft.security.UserI;

import java.io.InputStream;
import java.util.List;

/**
 * Service interface for accessing DICOM data from XNAT
 */
public interface XnatDicomService {

    /**
     * Search for studies in a project
     */
    List<Attributes> searchStudies(UserI user, String projectId, Attributes queryAttributes);

    /**
     * Search for series within a study
     */
    List<Attributes> searchSeries(UserI user, String projectId, String studyInstanceUID, Attributes queryAttributes);

    /**
     * Search for instances within a series
     */
    List<Attributes> searchInstances(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID, Attributes queryAttributes);

    /**
     * Retrieve a DICOM instance
     */
    InputStream retrieveInstance(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID);

    /**
     * Retrieve metadata for an instance
     */
    Attributes retrieveMetadata(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID);

    /**
     * Retrieve study-level metadata
     */
    Attributes retrieveStudyMetadata(UserI user, String projectId, String studyInstanceUID);

    /**
     * Retrieve metadata for all instances in a study
     */
    List<Attributes> retrieveAllStudyInstanceMetadata(UserI user, String projectId, String studyInstanceUID);

    /**
     * Retrieve all instances in a study
     */
    List<InputStream> retrieveStudy(UserI user, String projectId, String studyInstanceUID);

    /**
     * Retrieve all instances in a series
     */
    List<InputStream> retrieveSeries(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID);

    /**
     * Retrieve a rendered instance as JPEG
     */
    byte[] retrieveRenderedInstance(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID);
}
