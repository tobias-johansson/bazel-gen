package org.neo4j;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(
        name = "module-deps",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ModuleDeps extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    Path root = Paths.get( "/home/tobias/Projects/neo4j" );

    public void execute() throws MojoExecutionException
    {
        printDeps();
    }

    private void printDeps()
    {
        List<String> external = project.getDependencies().stream()
                .filter( d -> !d.getVersion().equals( "4.0.0-SNAPSHOT" ) )
                .map( this::toPantsSpec )
                .collect( Collectors.toList() );

        File out = root.resolve( "all-deps.txt" ).toFile();
        try ( FileOutputStream os = new FileOutputStream( out, true ); PrintStream ps = new PrintStream( os ) )
        {
            external.forEach( ps::println );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private String toPantsSpec( Dependency d )
    {
        String artifactId = d.getArtifactId();
        if ( artifactId.endsWith( "_2.12" ) )
        {
            artifactId = artifactId.replaceAll( "_2.12$", "" );
            return "scala_jar('" + d.getGroupId() + "', '" + artifactId + "', '" + d.getVersion() + "'),";
        }
        else
        {
            return "jar('" + d.getGroupId() + "', '" + artifactId + "', '" + d.getVersion() + "'),";
        }
    }
}
