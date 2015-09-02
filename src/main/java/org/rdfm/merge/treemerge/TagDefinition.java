package org.rdfm.merge.treemerge;

import java.util.List;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class TagDefinition {
    String branchName;
    List<BranchHistoryMappingTagDefinition> branchLimits;

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public List<BranchHistoryMappingTagDefinition> getBranchLimits() {
        return branchLimits;
    }

    public void setBranchLimits(List<BranchHistoryMappingTagDefinition> branchLimits) {
        this.branchLimits = branchLimits;
    }
}
