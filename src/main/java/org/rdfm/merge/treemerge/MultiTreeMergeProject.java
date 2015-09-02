package org.rdfm.merge.treemerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import java.beans.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bantaloukasc on 05/08/15.
 */
public class MultiTreeMergeProject {
    private static final Logger log = LoggerFactory.getLogger(MultiTreeMergeProject.class);
    String name;
    String targetRepositoryId;
    Map<String, BranchHistoryMapping> trunkHistoryMappings;
    Map<String, BranchHistoryMapping> branchHistoryMappings;
    List<BranchHistoryMappingTagDefinition> branchHistoryMappingTagDefinitions;
    String targetRelativePath;
    transient CommitMappings commitMappings;
    transient Map<String, Repository> repositoryMap;

    public String getTargetRepositoryId() {
        return targetRepositoryId;
    }

    public void setTargetRepositoryId(String targetRepositoryId) {
        this.targetRepositoryId = targetRepositoryId;
    }

    public String getTargetRelativePath() {
        return targetRelativePath;
    }

    public void setTargetRelativePath(String targetRelativePath) {
        this.targetRelativePath = targetRelativePath;
    }

    public void doInitialImportOfTrunk(String branchHistoryMappingName) throws SVNException {
        BranchHistoryMapping branchHistoryMapping = trunkHistoryMappings.get(branchHistoryMappingName);
        branchHistoryMapping.doInitialImportOfTrunk();
    }

    @Transient
    public CommitMappings getCommitMappings() {
        return commitMappings;
    }

    @Transient
    public void setCommitMappings(CommitMappings commitMappings) {
        this.commitMappings = commitMappings;
    }

    @Transient
    public Map<String, Repository> getRepositoryMap() {
        return repositoryMap;
    }

    @Transient
    public void setRepositoryMap(Map<String, Repository> repositoryMap) {
        this.repositoryMap = repositoryMap;
    }

    public List<BranchHistoryMappingTagDefinition> getBranchHistoryMappingTagDefinitions() {
        return branchHistoryMappingTagDefinitions;
    }

    public void setBranchHistoryMappingTagDefinitions(List<BranchHistoryMappingTagDefinition> branchHistoryMappingTagDefinitions) {
        this.branchHistoryMappingTagDefinitions = branchHistoryMappingTagDefinitions;
    }


    public Map<String, BranchHistoryMapping> getBranchHistoryMappings() {
        if (branchHistoryMappings == null) {
            branchHistoryMappings = new HashMap<>();
        }
        return branchHistoryMappings;
    }

    public void setBranchHistoryMappings(Map<String, BranchHistoryMapping> branchHistoryMappings) {
        this.branchHistoryMappings = branchHistoryMappings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, BranchHistoryMapping> getTrunkHistoryMappings() {
        return trunkHistoryMappings;
    }

    public void setTrunkHistoryMappings(Map<String, BranchHistoryMapping> trunkHistoryMappings) {
        this.trunkHistoryMappings = trunkHistoryMappings;
    }


}
