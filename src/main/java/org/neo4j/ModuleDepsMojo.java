package org.neo4j;

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

import org.neo4j.model.Dependencies;
import org.neo4j.model.Global;
import org.neo4j.model.Module;
import org.neo4j.tools.Files;
import org.neo4j.tools.Locations;

@Mojo(
        name = "module-deps",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ModuleDepsMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    @Parameter( required = true, property = "rootDir" )
    String rootDir;

    public void execute() throws MojoExecutionException
    {
        Global global = new Global( new Files( rootDir ) );
        Module module = new Module( project, global );
        printDeps( global.locations.files.tmp.resolve( "all-deps.txt" ).toFile(), module );
    }

    private void printDeps( File out, Module module )
    {
        try ( FileOutputStream os = new FileOutputStream( out, true ); PrintStream ps = new PrintStream( os ) )
        {
            module.dependencies()
                  .filter( Dependencies.Dep::isExternal )
                  .map( Dependencies.Dep::coords )
                  .forEach( ps::println );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
