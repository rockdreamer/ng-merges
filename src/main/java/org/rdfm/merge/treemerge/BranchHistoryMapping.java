package org.rdfm.merge.treemerge;

import org.rdfm.merge.BadCommandException;
import org.rdfm.merge.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.beans.Transient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class BranchHistoryMapping {
    private static final Logger log = LoggerFactory.getLogger(BranchHistoryMapping.class);

    String name;
    String sourceRepositoryId;
    transient Repository sourceRepository;
    String targetRepositoryId;
    transient Repository targetRepository;
    String sourceRelativePath;
    transient SVNURL sourceUrl;
    String targetRelativePath;
    transient SVNURL targetUrl;
    String trunkHistoryMappingId;
    transient BranchHistoryMapping trunkHistoryMapping;
    SVNRevision startRevision;
    transient CommitMappingList commitMappingList;

    @Transient
    public Repository getSourceRepository() {
        return sourceRepository;
    }

    @Transient
    public void setSourceRepository(Repository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Transient
    public Repository getTargetRepository() {
        return targetRepository;
    }

    @Transient
    public void setTargetRepository(Repository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @Transient
    public BranchHistoryMapping getTrunkHistoryMapping() {
        return trunkHistoryMapping;
    }

    @Transient
    public void setTrunkHistoryMapping(BranchHistoryMapping trunkHistoryMapping) {
        this.trunkHistoryMapping = trunkHistoryMapping;
    }

    @Transient
    public CommitMappingList getCommitMappingList() {
        return commitMappingList;
    }

    @Transient
    public void setCommitMappingList(CommitMappingList commitMappingList) {
        this.commitMappingList = commitMappingList;
    }

    public void connectTo(MultiTreeMergeProject project) throws ConfigurationException {
        sourceRepository = project.getRepositoryMap().get(sourceRepositoryId);
        if (sourceRepository == null) {
            throw new ConfigurationException(
                    String.format("Cannot find source repository %s for branch history mapping %s", sourceRepositoryId, name));
        }
        targetRepository = project.getRepositoryMap().get(targetRepositoryId);
        if (targetRepository == null) {
            throw new ConfigurationException(
                    String.format("Cannot find target repository %s for branch history mapping %s", targetRepositoryId, name));
        }
        try {
            sourceUrl = sourceRepository.getBaseUrl().appendPath(sourceRelativePath, false);
        } catch (SVNException e) {
            throw new ConfigurationException(
                    String.format("Cannot create source repository url for branch history mapping %s", name), e);
        }
        try {
            targetUrl = targetRepository.getBaseUrl().appendPath(targetRelativePath, false);
        } catch (SVNException e) {
            throw new ConfigurationException(
                    String.format("Cannot create target repository url for branch history mapping %s", name), e);
        }
        if (trunkHistoryMappingId != null) {
            trunkHistoryMapping = project.getTrunkHistoryMappings().get(trunkHistoryMappingId);
            if (trunkHistoryMapping == null) {
                throw new ConfigurationException(
                        String.format("Cannot find parent history mapping %s for branch history mapping %s", trunkHistoryMappingId, name));
            }
        }
        commitMappingList = project.getCommitMappings().get(name);

    }

    public void doInitialImportOfTrunk() throws SVNException {

        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(targetRepository.getAuthManager());
        targetRepository.updateAndCleanWc();

        SVNRevision currentRevision = targetRepository.getCurrentWcRevision();
        log.info("Current WC revision: {}", currentRevision);

        File destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());
        ourClientManager.getUpdateClient().doExport(
                getSourceUrl(), destinationPath,
                getStartRevision(),
                getStartRevision(),
                null,
                true,
                SVNDepth.INFINITY
        );
        log.info("Exported {} on repo directory {}", getSourceUrl(), destinationPath);

        targetRepository.addAll();
        targetRepository.doCommit("Adding subproject " + getName() + " from original revision "
                + getSourceUrl().toString() + " r" +
                getStartRevision().toString());

    }

    @Transient
    public List<SVNLogEntry> getSourceSortedLogEntries(SVNRevision from, SVNRevision to) throws SVNException {
        ArrayList<SVNLogEntry> sortedLogEntries = new ArrayList<>();
        final ArrayList<SVNLogEntry> logEntries = new ArrayList<>();
        sourceRepository.getClientManager().getLogClient().doLog(
                getSourceUrl(),
                null,
                from, // peg
                from,
                to,
                false,
                true,
                true,
                0,
                null,
                svnLogEntry -> logEntries.add(svnLogEntry)
        );

        logEntries.stream().sorted((e1, e2) -> Long.compare(e1.getRevision(), e2.getRevision()))
                .forEach(e -> sortedLogEntries.add(e));
        log.info("Found {} commits in {}", sortedLogEntries.size(), targetRelativePath);
        return sortedLogEntries;

    }

    public void doAllUpdatesForParentless() throws BadCommandException, SVNException {
        if (hasParent()) {
            throw new BadCommandException("Cannot do AllUpdatesForParentless for project with parent...");
        }

        SVNRevision sourceFromRevision = startRevision;
        for (SingleCommitMapping mapping : commitMappingList.getCommitMappings()) {
            if (mapping.getSourceRevision().getNumber() > sourceFromRevision.getNumber()) {
                sourceFromRevision = mapping.getSourceRevision();
            }
        }
        log.info("Update of {} starts with remote revision {}", getTargetUrl(), sourceFromRevision);
        File destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());

        List<SVNLogEntry> sortedLogEntries = getSourceSortedLogEntries(sourceFromRevision, SVNRevision.HEAD);
        for (SVNLogEntry logEntry : sortedLogEntries) {
            targetRepository.getClientManager().getDiffClient().doMerge(
                    getSourceUrl(),
                    SVNRevision.create(logEntry.getRevision() - 1),
                    getSourceUrl(),
                    SVNRevision.create(logEntry.getRevision()),
                    destinationPath,
                    SVNDepth.INFINITY,
                    false, // no ancestry
                    false,
                    false,
                    false
            );
            log.info("Merged revision {} from {} to {}", logEntry.getRevision(), getSourceUrl(), getTargetRelativePath());

            for (Map.Entry<String, SVNLogEntryPath> logEntryPathEntry : logEntry.getChangedPaths().entrySet()) {
                if (logEntryPathEntry.getValue().getType() == SVNLogEntryPath.TYPE_DELETED) {
                    log.info("deleted {}", logEntryPathEntry.getKey());
                }
            }

            targetRepository.doCommit(logEntry.getMessage()
                    + "\n\nOriginal commit r" + logEntry.getRevision()
                    + " in " + getName()
                    + " by " + logEntry.getAuthor());
            log.info("Committed {} on repo", getTargetUrl());

            targetRepository.updateAndCleanWc();
            SVNRevision currentRevision = targetRepository.getCurrentWcRevision();

            log.info("Current WC revision: {}", currentRevision);
            SingleCommitMapping commitMapping = new SingleCommitMapping();
            commitMapping.setSourceRevision(SVNRevision.create(logEntry.getRevision()));
            commitMapping.setTargetRevision(currentRevision);
            commitMappingList.getCommitMappings().add(commitMapping);
        }

    }

    public boolean hasParent() {
        return trunkHistoryMappingId != null;
    }

    public String getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    public void setSourceRepositoryId(String sourceRepositoryId) {
        this.sourceRepositoryId = sourceRepositoryId;
    }

    public String getTargetRepositoryId() {
        return targetRepositoryId;
    }

    public void setTargetRepositoryId(String targetRepositoryId) {
        this.targetRepositoryId = targetRepositoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public SVNURL getSourceUrl() {
        return sourceUrl;
    }

    @Transient
    public void setSourceUrl(SVNURL sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Transient
    public SVNURL getTargetUrl() {
        return targetUrl;
    }

    @Transient
    public void setTargetUrl(SVNURL targetUrl) {
        this.targetUrl = targetUrl;
    }

    public SVNRevision getStartRevision() {
        return startRevision;
    }

    public void setStartRevision(SVNRevision startRevision) {
        this.startRevision = startRevision;
    }

    public String getTrunkHistoryMappingId() {
        return trunkHistoryMappingId;
    }

    public void setTrunkHistoryMappingId(String trunkHistoryMappingId) {
        this.trunkHistoryMappingId = trunkHistoryMappingId;
    }

    public String getSourceRelativePath() {
        return sourceRelativePath;
    }

    public void setSourceRelativePath(String sourceRelativePath) {
        this.sourceRelativePath = sourceRelativePath;
    }

    public String getTargetRelativePath() {
        return targetRelativePath;
    }

    public void setTargetRelativePath(String targetRelativePath) {
        this.targetRelativePath = targetRelativePath;
    }

}
