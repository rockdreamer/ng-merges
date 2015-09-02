package org.rdfm.merge.singletreemerge;

/**
 * Created by bantaloukasc on 05/08/15.
 */
public class Merge {
    String revision;
    String author;
    String fullLogMessage;
    String briefLogMessage;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFullLogMessage() {
        return fullLogMessage;
    }

    public void setFullLogMessage(String fullLogMessage) {
        this.fullLogMessage = fullLogMessage;
    }

    public String getBriefLogMessage() {
        return briefLogMessage;
    }

    public void setBriefLogMessage(String briefLogMessage) {
        this.briefLogMessage = briefLogMessage;
    }

}
