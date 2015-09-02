package org.rdfm.merge.treemerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.beans.Transient;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by bantaloukasc on 28/08/15.
 */
public class Repository {
    static final Logger log = LoggerFactory.getLogger(Repository.class);

    String id;
    transient ISVNAuthenticationManager authManager;
    String username;
    String password;
    SVNURL baseUrl;
    File wcPath;
    transient SVNClientManager clientManager;

    public static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public void updateAndCleanWc() throws SVNException {
        getClientManager().getUpdateClient().doUpdate(
                getWcPath(),
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false,
                true);
        getClientManager().getWCClient().doCleanup(getWcPath(), true);
    }

    public SVNRevision getCurrentWcRevision() {
        try {
            return getClientManager().getWCClient().doInfo(getWcPath(), SVNRevision.HEAD).getRevision();
        } catch (SVNException e) {
            return null;
        }
    }

    public void deleteWc() {
        try {
            log.info("Deleting wc {}", getWcPath());
            deleteRecursive(getWcPath());
            log.info("Deleted wc {}", getWcPath());
        } catch (FileNotFoundException e) {
            log.info("wc {} is not around, good", getWcPath());
        }
    }

    public void checkOutWc() throws SVNException {

        SVNUpdateClient updateClient = getClientManager().getUpdateClient();
        updateClient.doCheckout(
                getBaseUrl(),
                getWcPath(),
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false);
        log.info("Checked out {}", getWcPath());
    }

    public void addAll() throws SVNException {
        getClientManager().getWCClient().doAdd(
                new File[]{getWcPath()},
                true,
                true,
                true,
                SVNDepth.INFINITY,
                false,
                true,
                true
        );
        log.info("Added all files for commit in {}", getWcPath());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    public ISVNAuthenticationManager getAuthManager() {
        if (authManager == null) {
            addAuthenticationInfo(username, password);
        }
        return authManager;
    }

    @Transient
    public void setAuthManager(ISVNAuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void addAuthenticationInfo(String username, String password) {
        authManager =
                SVNWCUtil.createDefaultAuthenticationManager(username, password.toCharArray());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SVNURL getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(SVNURL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public File getWcPath() {
        return wcPath;
    }

    public void setWcPath(File wcPath) {
        this.wcPath = wcPath;
    }

    public void doCommit(String message) throws SVNException {
        getClientManager().getCommitClient().doCommit(
                new File[]{getWcPath()},
                false,
                message,
                null,
                null,
                false,
                false,
                SVNDepth.INFINITY
        );
        log.info("Committed on repo {}", getId());

    }

    @Transient
    public SVNClientManager getClientManager() {
        if (clientManager != null) {
            return clientManager;
        }
        clientManager = SVNClientManager.newInstance();
        clientManager.setAuthenticationManager(getAuthManager());
        return clientManager;
    }
}
