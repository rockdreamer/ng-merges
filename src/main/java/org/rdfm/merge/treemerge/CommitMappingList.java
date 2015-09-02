package org.rdfm.merge.treemerge;

import java.util.ArrayList;

/**
 * Created by bantaloukasc on 28/08/15.
 */
public class CommitMappingList {
    String branchHistoryMappingName;
    ArrayList<SingleCommitMapping> commitMappings = new ArrayList<>();

    public String getBranchHistoryMappingName() {
        return branchHistoryMappingName;
    }

    public void setBranchHistoryMappingName(String branchHistoryMappingName) {
        this.branchHistoryMappingName = branchHistoryMappingName;
    }

    public ArrayList<SingleCommitMapping> getCommitMappings() {
        return commitMappings;
    }

    public void setCommitMappings(ArrayList<SingleCommitMapping> commitMappings) {
        this.commitMappings = commitMappings;
    }

}
