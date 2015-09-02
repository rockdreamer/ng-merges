package org.rdfm.merge.treemerge;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class BranchHistoryMappingTagDefinition {
    String branchHistoryMappingName;
    SVNRevision sourceTagRevision;

    public String getBranchHistoryMappingName() {
        return branchHistoryMappingName;
    }

    public void setBranchHistoryMappingName(String branchHistoryMappingName) {
        this.branchHistoryMappingName = branchHistoryMappingName;
    }

    public SVNRevision getSourceTagRevision() {
        return sourceTagRevision;
    }

    public void setSourceTagRevision(SVNRevision sourceTagRevision) {
        this.sourceTagRevision = sourceTagRevision;
    }
}
