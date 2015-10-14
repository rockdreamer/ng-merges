package org.rdfm.merge; /**
 * Created by bantaloukasc on 05/08/15.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.rdfm.merge.treemerge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.SparkBase.staticFileLocation;

public class MultiTreeMerges {
    static final Logger log = LoggerFactory.getLogger(MultiTreeMerges.class);

    public static void main(String[] args) {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .registerTypeHierarchyAdapter(SVNURL.class, new SVNUrlSerializer())
                .registerTypeHierarchyAdapter(SVNRevision.class, new SVNRevisionSerializer())
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .create();

        final Repositories repositories = new Repositories();
        try {
            repositories.loadAllFromJson();
        } catch (IOException e) {
            log.error("Cannot read repositories", e);
            return;
        }

        CommitMappings commitMappings = new CommitMappings();
        try {
            commitMappings.loadAllFromJson();
        } catch (IOException e) {
            log.error("Cannot read commit mappings", e);
            return;
        }

        final MultiTreeMergeProjects multiTreeMergeProjects = new MultiTreeMergeProjects();
        try {
            multiTreeMergeProjects.loadAllFromJson();
        } catch (IOException e) {
            log.error("Cannot read multi-tree projects", e);
            return;
        }

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


        staticFileLocation("/public"); // Static files

        get("/repositories/", (request, response) -> {
            //for development only
            response.header("Access-Control-Allow-Origin", "*");
            response.type("text/javascript");
            return repositories;
        }, gson::toJson);

        get("/repository/:name", (request, response) -> {
            response.type("text/javascript");
            return repositories.get(request.params(":name"));
        }, gson::toJson);

        get("/multi-tree/:name", (request, response) -> {
            response.type("text/javascript");
            MultiTreeMergeProject multiTreeMergeProject = multiTreeMergeProjects.get(request.params(":name"));
            return multiTreeMergeProject;
        }, gson::toJson);

        get("/multi-tree/:name/importTrunks", (request, response) -> {
            response.type("text/javascript");
            MultiTreeMergeProject multiTreeMergeProject = multiTreeMergeProjects.get(request.params(":name"));
            return importTrunks(multiTreeMergeProject);
        }, gson::toJson);

        get("/multi-tree/:name/updateTrunks", (request, response) -> {
            response.type("text/javascript");
            MultiTreeMergeProject multiTreeMergeProject = multiTreeMergeProjects.get(request.params(":name"));
            multiTreeMergeProject.setCommitMappings(commitMappings);
            multiTreeMergeProject.setRepositoryMap(repositories);
            return updateTrunk(multiTreeMergeProject);
        }, gson::toJson);

    }

    public static ArrayList<TrunkMergeStatus> updateTrunk(MultiTreeMergeProject project) {
        ArrayList<TrunkMergeStatus> statuses = new ArrayList<>();
        Repository targetRepository = project.getRepositoryMap().get(project.getTargetRepositoryId());
        if (targetRepository == null) {
            return null;
        }

        targetRepository.deleteWc();

        try {
            targetRepository.checkOutWc();
        } catch (SVNException e) {
            log.error("Error checking out wc {}", project.getName(), e);
            TrunkMergeStatus status = new TrunkMergeStatus();
            status.setProject(project);
            status.setOk(false);
            status.setException(e);
            status.setError("Checkout Error");
            statuses.add(status);
            return statuses;
        }

        for (Map.Entry<String, BranchHistoryMapping> subProject : project.getTrunkHistoryMappings().entrySet()) {
            statuses.add(updateSubProjectTrunk(project, subProject.getValue()));
        }

        return statuses;
    }

    private static TrunkMergeStatus updateSubProjectTrunk(MultiTreeMergeProject project, BranchHistoryMapping subProjectTrunk) {
        TrunkMergeStatus status = new TrunkMergeStatus();
        status.setProject(project);
        status.setMapping(subProjectTrunk);
        status.setOk(true);

        try {
            subProjectTrunk.doAllUpdatesForParentless();
        } catch (BadCommandException | SVNException e) {
            status.setOk(false);
            status.setError(e.getMessage());
            status.setException(e);
            return status;
        }

        return status;
    }

    public static ArrayList<TrunkMergeStatus> importTrunks(MultiTreeMergeProject project) {
        ArrayList<TrunkMergeStatus> statuses = new ArrayList<>();
        Repository targetRepository = project.getRepositoryMap().get(project.getTargetRepositoryId());
        if (targetRepository == null) {
            return null;
        }

        targetRepository.deleteWc();

        try {
            targetRepository.checkOutWc();
        } catch (SVNException e) {
            log.error("Error checking out wc {}", project.getName(), e);
            TrunkMergeStatus status = new TrunkMergeStatus();
            status.setProject(project);
            status.setOk(false);
            status.setException(e);
            status.setError("Checkout Error");
            statuses.add(status);
            return statuses;
        }


        for (Map.Entry<String, BranchHistoryMapping> subProject : project.getTrunkHistoryMappings().entrySet()) {
            statuses.add(importSubProjectTrunk(project, subProject.getValue()));
        }

        return statuses;
    }

    private static TrunkMergeStatus importSubProjectTrunk(MultiTreeMergeProject project, BranchHistoryMapping subProjectTrunk) {
        TrunkMergeStatus status = new TrunkMergeStatus();
        status.setProject(project);
        status.setMapping(subProjectTrunk);
        status.setOk(true);

        try {
            project.doInitialImportOfTrunk(subProjectTrunk.getName());
        } catch (SVNException e) {
            log.error("Error doing initial import of {}", subProjectTrunk.getName(), e);
            status.setOk(false);
            status.setException(e);
            status.setError("Initial Import Error");
            return status;
        }
        return status;
    }

    public static ArrayList<TrunkMergeStatus> mergeBranches(MultiTreeMergeProject project) {
        ArrayList<TrunkMergeStatus> statuses = new ArrayList<>();
        Repository targetRepository = project.getRepositoryMap().get(project.getTargetRepositoryId());
        if (targetRepository == null) {
            return null;
        }
        statuses.addAll(project.getBranchHistoryMappings().entrySet().stream().map(
                subProject -> importSubProjectBranch(project, subProject.getValue()))
                .collect(Collectors.toList()));

        return statuses;
    }

    private static TrunkMergeStatus importSubProjectBranch(MultiTreeMergeProject project, BranchHistoryMapping subProjectTrunk) {
        TrunkMergeStatus status = new TrunkMergeStatus();
        status.setProject(project);
        status.setMapping(subProjectTrunk);
        status.setOk(true);

        try {
            project.doImportOfBranch(subProjectTrunk.getName());
        } catch (SVNException | BadCommandException e) {
            log.error("Error doing import of {}", subProjectTrunk.getName(), e);
            status.setOk(false);
            status.setException(e);
            status.setError("Branch Import Error");
            return status;
        }
        return status;
    }
}
