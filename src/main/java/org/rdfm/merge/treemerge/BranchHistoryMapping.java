package org.rdfm.merge.treemerge;

import org.rdfm.merge.BadCommandException;
import org.rdfm.merge.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.beans.Transient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    transient CommitMappingList commitMappingList=new CommitMappingList();

    transient CommitMappings commitMappings;

    transient Pattern p1 = Pattern.compile("^merge r(\\d+) (\\S+)");
    transient Pattern p2 = Pattern.compile("^Merged revision\\(s\\) (\\d+)");

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
        if (commitMappingList==null){
            commitMappingList = new CommitMappingList();
            commitMappingList.setBranchHistoryMappingName(name);
            project.getCommitMappings().put(name,commitMappingList);
        }
        commitMappings = project.getCommitMappings();

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
                    + " by " + logEntry.getAuthor(),
                    logEntry.getAuthor());
            log.info("Committed {} on repo", getTargetUrl());

            targetRepository.updateWc();
            SVNRevision currentRevision = targetRepository.getCurrentWcRevision();

            log.info("Current WC revision: {}", currentRevision);
            SingleCommitMapping commitMapping = new SingleCommitMapping();
            commitMapping.setSourceRevision(SVNRevision.create(logEntry.getRevision()));
            commitMapping.setTargetRevision(currentRevision);
            commitMappingList.getCommitMappings().add(commitMapping);
        }

    }
    public void doBranchUpdate() throws SVNException, BadCommandException {
        if (!hasParent()) {
            throw new BadCommandException("Cannot do branch update for project without parent...");
        }
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(targetRepository.getAuthManager());
        targetRepository.updateAndCleanWc();

        SVNRevision currentRevision = targetRepository.getCurrentWcRevision();
        log.info("Current WC revision: {}", currentRevision);

        File destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());
        List<SVNLogEntry> sortedLogEntries = getBranchSortedLogEntries();
        log.debug("Source Branch entries: {}", sortedLogEntries);
        for (SVNLogEntry entry: sortedLogEntries){
            if (!destinationPath.exists()){
                setStartRevision(SVNRevision.create(entry.getRevision()));
                if (entry.getChangedPaths().size()!=1){
                    log.error("First branch is not copy!");
                    throw new BadCommandException("First branch commit is not copy");
                }
                SVNLogEntryPath logEntryPath = entry.getChangedPaths().values().iterator().next();
                SVNRevision copyRevision = SVNRevision.create(logEntryPath.getCopyRevision());
                SVNRevision newCopyRevision = getTrunkHistoryMapping().getCommitMappingList().getTargetRevision(copyRevision);

                String message = String.format("%s\n\n Original revision r%d copy of r%d from repository %s, path %s",
                        entry.getMessage(),
                        entry.getRevision(), logEntryPath.getCopyRevision(),
                        getTrunkHistoryMappingId(), logEntryPath.getCopyPath());
                ourClientManager.getCopyClient().doCopy(new SVNCopySource[]{
                        new SVNCopySource(newCopyRevision,newCopyRevision,getTrunkHistoryMapping().getTargetUrl())
                },
                        getTargetUrl(),
                        false,
                        true,
                        true,
                        message,
                        null);
                log.info("Copy revision is {} -> {}", copyRevision, newCopyRevision);
                targetRepository.updateWc();
                currentRevision = targetRepository.getCurrentWcRevision();

                log.info("Current WC revision: {}", currentRevision);
                SingleCommitMapping commitMapping = new SingleCommitMapping();
                commitMapping.setSourceRevision(SVNRevision.create(entry.getRevision()));
                commitMapping.setTargetRevision(currentRevision);
                commitMappingList.getCommitMappings().add(commitMapping);
            } else {
                if (commitMappingList.getExactTargetRevision(SVNRevision.create(entry.getRevision()))!=null){
                    log.info("Skipping altready merged {} revision {}", getSourceRepositoryId(), entry.getRevision());
                    continue;
                }
                currentRevision = targetRepository.getCurrentWcRevision();
                SVNRevision targetRevision = commitMappingList.getTargetRevision(SVNRevision.create(entry.getRevision()));
                log.info("Merging {} revision {} to {} source URL:{} destinationPath:{}", getSourceRepositoryId(), entry.getRevision(),
                        getTargetRepositoryId(), getSourceUrl(), destinationPath);
                targetRepository.getClientManager().getDiffClient().doMerge(
                        getSourceUrl(),
                        SVNRevision.create(entry.getRevision() - 1),
                        getSourceUrl(),
                        SVNRevision.create(entry.getRevision()),
                        destinationPath,
                        SVNDepth.INFINITY,
                        false, // no ancestry
                        false,
                        false,
                        false
                );
                log.info("Merged files of revision {} from {} to {}", entry.getRevision(), getSourceUrl(), getTargetRelativePath());

                for (Map.Entry<String, SVNLogEntryPath> logEntryPathEntry : entry.getChangedPaths().entrySet()) {
                    if (logEntryPathEntry.getValue().getType() == SVNLogEntryPath.TYPE_DELETED) {
                        log.info("deleted {}", logEntryPathEntry.getKey());
                    }
                }
                Matcher m1 = p1.matcher(entry.getMessage());
                Matcher m2 = p2.matcher(entry.getMessage());
                Long sourceTrunkRevision=null;
                String originalAuthor=null;
                if (m1.find()){
                    sourceTrunkRevision = Long.decode(m1.group(1));
                    originalAuthor = m1.group(2);
                }
                if (m2.find()){
                    sourceTrunkRevision = Long.decode(m2.group(1));
                }
                CommitMappingList trunkMappings= getTrunkHistoryMapping().getCommitMappingList();
                SVNRevision targetTrunkRevision= null;
                if (sourceTrunkRevision!=null){
                    targetTrunkRevision = trunkMappings.getExactTargetRevision(SVNRevision.create(sourceTrunkRevision));
                }

                log.info("merging ancestry: source trunk revision is {}, in target {}",sourceTrunkRevision, targetTrunkRevision);
                if (targetTrunkRevision!=null){
                    targetRepository.getClientManager().getDiffClient().doMerge(
                            getTrunkHistoryMapping().getTargetUrl(),
                            SVNRevision.create(targetTrunkRevision.getNumber() - 1),
                            getTrunkHistoryMapping().getTargetUrl(),
                            SVNRevision.create(targetTrunkRevision.getNumber()),
                            destinationPath,
                            SVNDepth.INFINITY,
                            true,
                            false,
                            false,
                            true
                    );
                }
                String message = entry.getMessage() +"\n\n"
                        + "Original commit r"+entry.getRevision()
                        + " in " + getName()
                        + " by " + entry.getAuthor();
                if (targetTrunkRevision!=null) {
                    message = message +"\n"
                            + "mapped to target trunk revision " + targetTrunkRevision.toString();
                }

                if (originalAuthor!=null) {
                    targetRepository.doCommit(message, originalAuthor);
                    log.info("Committed {} on repo as {}", getTargetUrl(), originalAuthor);
                } else {
                    targetRepository.doCommit(message, entry.getAuthor());
                    log.info("Committed {} on repo as {}", getTargetUrl(), entry.getAuthor());
                }

                targetRepository.updateWc();
                currentRevision = targetRepository.getCurrentWcRevision();

                log.info("Current WC revision: {}", currentRevision);
                SingleCommitMapping commitMapping = new SingleCommitMapping();
                commitMapping.setSourceRevision(SVNRevision.create(entry.getRevision()));
                commitMapping.setTargetRevision(currentRevision);
                commitMappingList.getCommitMappings().add(commitMapping);
            }
        }

    }

    public void something() throws SVNException, BadCommandException {
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(targetRepository.getAuthManager());
        targetRepository.updateAndCleanWc();
        SVNRevision currentRevision = targetRepository.getCurrentWcRevision();


        File destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());
        if (!destinationPath.exists()) {
            log.info("missing target directory {}", destinationPath);

        } else {
            log.info("{} already present on repo directory {}", getSourceUrl(), destinationPath);
            destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());
        }

        //


        SVNRevision sourceFromRevision = startRevision;
        for (SingleCommitMapping mapping : commitMappingList.getCommitMappings()) {
            if (mapping.getSourceRevision().getNumber() > sourceFromRevision.getNumber()) {
                sourceFromRevision = mapping.getSourceRevision();
            }
        }
        log.info("Update of {} starts with remote revision {}", getTargetUrl(), sourceFromRevision);
        destinationPath = new File(targetRepository.getWcPath(), getTargetRelativePath());

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
                            + " by " + logEntry.getAuthor(),
                    logEntry.getAuthor());
            log.info("Committed {} on repo", getTargetUrl());

            targetRepository.updateWc();
            currentRevision = targetRepository.getCurrentWcRevision();

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

    public List<SVNLogEntry> getBranchSortedLogEntries() throws SVNException {
        ArrayList < SVNLogEntry > sortedLogEntries = new ArrayList<>();
        final ArrayList<SVNLogEntry> logEntries = new ArrayList<>();
        sourceRepository.getClientManager().getLogClient().doLog(
                getSourceUrl(),
                new String[]{""},
                null, // no peg
                SVNRevision.create(0),
                SVNRevision.HEAD,
                true,
                true,
                0,
                svnLogEntry -> logEntries.add(svnLogEntry)
        );

        logEntries.stream().sorted((e1, e2) -> Long.compare(e1.getRevision(), e2.getRevision()))
                .forEach(e -> sortedLogEntries.add(e));
        log.info("Found {} commits in {}", sortedLogEntries.size(), targetRelativePath);
        return sortedLogEntries;

    }
}
