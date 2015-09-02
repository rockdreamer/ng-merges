package org.rdfm.merge.singletreemerge;

import org.tmatesoft.svn.core.SVNURL;

/**
 * Created by bantaloukasc on 05/08/15.
 */
public class SingleTreeMergeProject {
    String name;
    String mergeCommand;
    SVNURL trunkUrl;
    SVNURL branchUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMergeCommand() {
        return mergeCommand;
    }

    public void setMergeCommand(String mergeCommand) {
        this.mergeCommand = mergeCommand;
    }

    public SVNURL getTrunkUrl() {
        return trunkUrl;
    }

    public void setTrunkUrl(SVNURL trunkUrl) {
        this.trunkUrl = trunkUrl;
    }

    public SVNURL getBranchUrl() {
        return branchUrl;
    }

    public void setBranchUrl(SVNURL branchUrl) {
        this.branchUrl = branchUrl;
    }

}
