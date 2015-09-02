package org.rdfm.merge.treemerge;

import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.ArrayList;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class TrunkMergeStatus {
    boolean ok;
    String error;
    Exception exception;
    MultiTreeMergeProject project;
    BranchHistoryMapping mapping;
    ArrayList<SVNLogEntry> pendingEntries;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public MultiTreeMergeProject getProject() {
        return project;
    }

    public void setProject(MultiTreeMergeProject project) {
        this.project = project;
    }

    public BranchHistoryMapping getMapping() {
        return mapping;
    }

    public void setMapping(BranchHistoryMapping mapping) {
        this.mapping = mapping;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<SVNLogEntry> getPendingEntries() {
        return pendingEntries;
    }

    public void setPendingEntries(ArrayList<SVNLogEntry> pendingEntries) {
        this.pendingEntries = pendingEntries;
    }
}
