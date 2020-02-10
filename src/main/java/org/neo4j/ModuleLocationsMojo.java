package org.neo4j;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.neo4j.tools.Files;
import org.neo4j.tools.Locations;

@Mojo(
        name = "module-locations",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ModuleLocationsMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    @Parameter( required = true, property = "rootDir" )
    String rootDir;

    public void execute() throws MojoExecutionException
    {
        Locations locations = new Locations( new Files( rootDir ) );
        locations.add( project );
    }
}
