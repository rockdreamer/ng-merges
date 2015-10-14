package org.rdfm.merge;
import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.rdfm.merge.treemerge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by rdfm on 12/10/15.
 */
public class MultiTreeMergeTest {
    static final Logger log = LoggerFactory.getLogger(MultiTreeMerges.class);

    @Test
    public void doall() throws SVNException, IOException{
        mergeAllExNovo();
        mergeBranches();
    }

    @Test
    public void mergeAllExNovo() throws SVNException, IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .registerTypeHierarchyAdapter(SVNURL.class, new SVNUrlSerializer())
                .registerTypeHierarchyAdapter(SVNRevision.class, new SVNRevisionSerializer())
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .create();

        final Repositories repositories = new Repositories();
            repositories.loadAllFromJson();

        CommitMappings commitMappings = new CommitMappings();
        commitMappings.loadAllFromJson();

        final MultiTreeMergeProjects multiTreeMergeProjects = new MultiTreeMergeProjects();
        multiTreeMergeProjects.loadAllFromJson();

        try {
            for (MultiTreeMergeProject multiTreeMergeProject : multiTreeMergeProjects.values()) {
                multiTreeMergeProject.setCommitMappings(commitMappings);
                multiTreeMergeProject.setRepositoryMap(repositories);
                for (BranchHistoryMapping mapping : multiTreeMergeProject.getTrunkHistoryMappings().values()) {
                    mapping.connectTo(multiTreeMergeProject);
                }
                for (BranchHistoryMapping mapping : multiTreeMergeProject.getBranchHistoryMappings().values()) {
                    mapping.connectTo(multiTreeMergeProject);
                }
            }
        } catch (ConfigurationException e) {
            log.error("Cannot read multi-tree projects", e);
            return;
        }

        MultiTreeMergeProject multiTreeMergeProject = multiTreeMergeProjects.get("tfj-all");
        multiTreeMergeProject.setCommitMappings(commitMappings);
        multiTreeMergeProject.setRepositoryMap(repositories);
        repositories.get("test-tfj-target").deleteWc();
        repositories.get("test-tfj-target").recreateEmptyLocalRepository();
        SVNClientManager cm = repositories.get("test-tfj-target").getClientManager();

        File base = new File("newbase");
        base.mkdirs();
        File other = new File(base,"tfj-all/trunk");
        other.mkdirs();
        other = new File(base,"tfj-all/branches");
        other.mkdirs();
        other = new File(base,"tfj-all/tags");
        other.mkdirs();


            cm.getCommitClient().doImport(
                    base,
                    repositories.get("test-tfj-target").getBaseUrl(),
                    "<structure import>", null,
                    false, true, SVNDepth.fromRecurse(true));
        ArrayList<TrunkMergeStatus> trunkMergeStatuses = MultiTreeMerges.importTrunks(multiTreeMergeProject);
        for (TrunkMergeStatus status: trunkMergeStatuses) {
            Assert.assertEquals(true, status.isOk());
        }
        log.info("Output:{}", gson.toJson(trunkMergeStatuses));

        ArrayList<TrunkMergeStatus> trunkUpdateMergeStatuses = MultiTreeMerges.updateTrunk(multiTreeMergeProject);
        for (TrunkMergeStatus status: trunkUpdateMergeStatuses) {
            Assert.assertEquals(true, status.isOk());
        }
        log.info("Output:{}", gson.toJson(trunkUpdateMergeStatuses));
        commitMappings.saveAllToJson();

    }
    @Test
    public void mergeBranches() throws SVNException, IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .registerTypeHierarchyAdapter(SVNURL.class, new SVNUrlSerializer())
                .registerTypeHierarchyAdapter(SVNRevision.class, new SVNRevisionSerializer())
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .create();

        final Repositories repositories = new Repositories();
        repositories.loadAllFromJson();

        CommitMappings commitMappings = new CommitMappings();
        commitMappings.loadAllFromJson();

        final MultiTreeMergeProjects multiTreeMergeProjects = new MultiTreeMergeProjects();
        multiTreeMergeProjects.loadAllFromJson();

        try {
            for (MultiTreeMergeProject multiTreeMergeProject : multiTreeMergeProjects.values()) {
                multiTreeMergeProject.setCommitMappings(commitMappings);
                multiTreeMergeProject.setRepositoryMap(repositories);
                for (BranchHistoryMapping mapping : multiTreeMergeProject.getTrunkHistoryMappings().values()) {
                    mapping.connectTo(multiTreeMergeProject);
                }
                for (BranchHistoryMapping mapping : multiTreeMergeProject.getBranchHistoryMappings().values()) {
                    mapping.connectTo(multiTreeMergeProject);
                }
            }
        } catch (ConfigurationException e) {
            log.error("Cannot read multi-tree projects", e);
            return;
        }

        MultiTreeMergeProject multiTreeMergeProject = multiTreeMergeProjects.get("tfj-all");
        multiTreeMergeProject.setCommitMappings(commitMappings);
        multiTreeMergeProject.setRepositoryMap(repositories);
        SVNClientManager cm = repositories.get("test-tfj-target").getClientManager();

        ArrayList<TrunkMergeStatus> trunkMergeStatuses = MultiTreeMerges.mergeBranches(multiTreeMergeProject);
        for (TrunkMergeStatus status: trunkMergeStatuses) {
            Assert.assertEquals(true, status.isOk());
        }
        log.info("Output:{}", gson.toJson(trunkMergeStatuses));
        commitMappings.saveAllToJson();
    }
}
