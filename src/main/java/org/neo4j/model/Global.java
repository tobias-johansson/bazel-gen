package org.neo4j.model;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import org.neo4j.tools.Files;
import org.neo4j.tools.Locations;

public class Global
{
    public final MavenSession session;
    public final Locations locations;
    public final DependencyGraphBuilder dependencyGraphBuilder;
    public final Files files;
    public final Dependencies dependencies;

    public Global( MavenSession session, Files files, DependencyGraphBuilder dependencyGraphBuilder )
    {
        this.session = session;
        this.files = files;
        this.locations = new Locations( files );
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.dependencies = new Dependencies( locations );
    }
}
