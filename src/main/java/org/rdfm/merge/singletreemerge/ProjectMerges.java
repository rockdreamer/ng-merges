package org.rdfm.merge.singletreemerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.ArrayList;

/**
 * Created by bantaloukasc on 05/08/15.
 */
public class ProjectMerges {
    private static Logger log = LoggerFactory.getLogger(ProjectMerges.class);
    ArrayList<Merge> merges;
    SingleTreeMergeProject singleTreeMergeProject;

    public ArrayList<Merge> getMerges() {
        return merges;
    }

    public void setMerges(ArrayList<Merge> merges) {
        this.merges = merges;
    }

    public SingleTreeMergeProject getSingleTreeMergeProject() {
        return singleTreeMergeProject;
    }

    public void setSingleTreeMergeProject(SingleTreeMergeProject singleTreeMergeProject) {
        this.singleTreeMergeProject = singleTreeMergeProject;
    }

    public void update(ISVNAuthenticationManager authManager) {
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(authManager);
        try {
        /*
        doGetLogEligibleMergeInfo(SVNURL url,
                                      SVNRevision pegRevision,
                                      SVNURL mergeSrcURL,
                                      SVNRevision srcPegRevision,
                                      boolean discoverChangedPaths,
                                      java.lang.String[] revisionProperties,
                                      ISVNLogEntryHandler handler)
         */
            if (merges == null)
                merges = new ArrayList<>();
            merges.clear();
            ourClientManager.getDiffClient().doGetLogEligibleMergeInfo(
                    singleTreeMergeProject.getBranchUrl(),
                    SVNRevision.HEAD,
                    singleTreeMergeProject.getTrunkUrl(),
                    SVNRevision.HEAD,
                    false,
                    null,
                    new ISVNLogEntryHandler() {
                        @Override
                        public void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException {
                            log.info("author:{} rev:{}", svnLogEntry.getAuthor(), svnLogEntry.getRevision());
                            Merge m = new Merge();
                            m.setAuthor(svnLogEntry.getAuthor());
                            m.setFullLogMessage(svnLogEntry.getMessage());
                            m.setRevision(Long.toString(svnLogEntry.getRevision(), 10));
                            String msg = svnLogEntry.getMessage();
                            msg = msg.replaceAll("[\n\t]", " ");
                            m.setBriefLogMessage(msg);
                            merges.add(m);
                        }
                    }
            );
        } catch (SVNException e) {
            log.error("error updating singleTreeMergeProject {}", singleTreeMergeProject.getName(), e);
        }


    }
}
