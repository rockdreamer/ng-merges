package org.rdfm.merge; /**
 * Created by bantaloukasc on 05/08/15.
 */

import com.google.gson.Gson;
import org.rdfm.merge.singletreemerge.ProjectMerges;
import org.rdfm.merge.singletreemerge.SingleTreeMergeProject;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import spark.ModelAndView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.SparkBase.staticFileLocation;

public class Merges {
    public static void main(String[] args) {

        staticFileLocation("/public"); // Static files
        Gson gson = new Gson();

        ISVNAuthenticationManager authManager =
                SVNWCUtil.createDefaultAuthenticationManager("bantaloukasc", "tff-build".toCharArray());

        SingleTreeMergeProject[] singleTreeMergeProjects = gson.fromJson(new BufferedReader(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("projects.json"))), SingleTreeMergeProject[].class);

        get("/singleTreeMergeProjects", (request, response) -> {
            return singleTreeMergeProjects;
        }, gson::toJson);

        get("/project-merges/:name", (request, response) -> {
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
