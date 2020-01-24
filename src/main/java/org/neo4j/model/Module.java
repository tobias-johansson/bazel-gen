package org.neo4j.model;

import org.apache.maven.project.MavenProject;

import java.util.stream.Stream;

public class Module
{
    private final MavenProject project;
    private final Global global;

    public Module( MavenProject project, Global global )
    {
        this.project = project;
        this.global = global;
    }

    public Stream<Dependencies.Dep> dependencies()
    {
        return project.getDependencies().stream()
                      .map( global.dependencies::create );
    }

    public boolean buildAsJava()
    {
        return hasMainJava() && !hasMainScala();
    }

    public boolean buildAsScala()
    {
        return hasMainScala();
    }

    public boolean hasMainJava()
    {
        return project.getBasedir().toPath().resolve( "src/main/java" ).toFile().exists();
    }

    public boolean hasMainScala()
    {
        return project.getBasedir().toPath().resolve( "src/main/scala" ).toFile().exists();
    }

    public boolean hasTestJava()
    {
        return project.getBasedir().toPath().resolve( "src/test/java" ).toFile().exists();
    }


}
