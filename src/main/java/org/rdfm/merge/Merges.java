package org.rdfm.merge; /**
 * Created by bantaloukasc on 05/08/15.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.rdfm.merge.singletreemerge.ProjectMerges;
import org.rdfm.merge.singletreemerge.SingleTreeMergeProject;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import spark.ModelAndView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.SparkBase.staticFileLocation;

public class Merges {
    public static void main(String[] args) {

        staticFileLocation("/public"); // Static files
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .registerTypeHierarchyAdapter(SVNURL.class, new SVNUrlSerializer())
                .registerTypeHierarchyAdapter(SVNRevision.class, new SVNRevisionSerializer())
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .create();

        ISVNAuthenticationManager authManager =
                SVNWCUtil.createDefaultAuthenticationManager("bantaloukasc", "tff-build".toCharArray());

        SingleTreeMergeProject[] singleTreeMergeProjects = gson.fromJson(new BufferedReader(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("projects.json"))), SingleTreeMergeProject[].class);

        get("/singleTreeMergeProjects", (request, response) -> {
            response.type("text/javascript");
            return singleTreeMergeProjects;
        }, gson::toJson);

        get("/project-merges/:name", (request, response) -> {
            response.type("text/javascript");
            for (SingleTreeMergeProject p : singleTreeMergeProjects) {
                if (p.getName().equals(request.params(":name"))) {
                    ProjectMerges projectMerge = new ProjectMerges();
                    projectMerge.setSingleTreeMergeProject(p);
                    projectMerge.update(authManager);
                    return projectMerge;
                }
            }
            return null;
        }, gson::toJson);

        get("/project-merges", (request, response) -> {
            response.type("text/javascript");
            ArrayList<ProjectMerges> projectMerges = new ArrayList<ProjectMerges>();
            for (SingleTreeMergeProject p : singleTreeMergeProjects) {
                ProjectMerges projectMerge = new ProjectMerges();
                projectMerge.setSingleTreeMergeProject(p);
                projectMerge.update(authManager);
                projectMerges.add(projectMerge);
            }
            return projectMerges;
        }, gson::toJson);

        get("/available-merges", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("websiteTitle", "Available merges!");
            ArrayList<ProjectMerges> projectMerges = new ArrayList<ProjectMerges>();
            for (SingleTreeMergeProject p : singleTreeMergeProjects) {
                ProjectMerges projectMerge = new ProjectMerges();
                projectMerge.setSingleTreeMergeProject(p);
                projectMerge.update(authManager);
                projectMerges.add(projectMerge);
            }
            attributes.put("projectMerges", projectMerges);
            return new ModelAndView(attributes, "available-merges.pebble");
        }, new PebbleTemplateEngine());
    }
}
