package org.rdfm.merge.treemerge;

import org.tmatesoft.svn.core.wc.SVNRevision;

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

    public SVNRevision getTargetRevision(SVNRevision sourceRevision){
        SVNRevision lastTargetRevision=null;
        for(SingleCommitMapping s:commitMappings){
            if (s.getSourceRevision().getNumber()<= sourceRevision.getNumber()){
                lastTargetRevision=s.getTargetRevision();
            } else {
                break;
            }
        }
        return lastTargetRevision;
    }
    public SVNRevision getExactTargetRevision(SVNRevision sourceRevision){
        for(SingleCommitMapping s:commitMappings){
            if (s.getSourceRevision().getNumber()== sourceRevision.getNumber()){
                return s.getTargetRevision();
            }
        }
        return null;
    }

}
