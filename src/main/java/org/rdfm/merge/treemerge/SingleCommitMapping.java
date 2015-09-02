package org.rdfm.merge.treemerge;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class SingleCommitMapping {
    SVNRevision sourceRevision;
    SVNRevision targetRevision;

    public SVNRevision getTargetRevision() {
        return targetRevision;
    }

    public void setTargetRevision(SVNRevision targetRevision) {
        this.targetRevision = targetRevision;
    }

    public SVNRevision getSourceRevision() {
        return sourceRevision;
    }

    public void setSourceRevision(SVNRevision sourceRevision) {
        this.sourceRevision = sourceRevision;
    }
}
