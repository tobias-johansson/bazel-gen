package org.neo4j;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.model.Dependencies;
import org.neo4j.model.Global;
import org.neo4j.model.Module;
import org.neo4j.tools.Files;
import org.neo4j.tools.XmlTools;

@Mojo( name = "bazel-generate", requiresDependencyCollection = ResolutionScope.TEST, threadSafe = true )
public class BazelGenerateMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    @Parameter( required = true, property = "rootDir" )
    String rootDir;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Component( hint = "default" )
    private DependencyGraphBuilder dependencyGraphBuilder;

    public void execute() throws MojoExecutionException
    {

        Global global = new Global( session, new Files( rootDir ), dependencyGraphBuilder );
        Module module = new Module( project, reactorProjects, global );

        Comparator<Dependencies.Dep> labelSort = Comparator.comparing( Dependencies.Dep::isExternal )
                                                           .thenComparing( Dependencies.Dep::label );

        boolean transitiveMainDeps = module.buildAsScala();
        List<Dependencies.Dep> compileDeps = module.dependencies( transitiveMainDeps )
                                                   .filter( Dependencies.Dep::isJarOrTestJar )
                                                   .sorted( labelSort )
                                                   .filter( Dependencies.Dep::isCompile )
                                                   .collect( Collectors.toList() );

        boolean transitiveTestDeps = module.testAsScala();
        List<Dependencies.Dep> testDeps = module.dependencies( transitiveTestDeps )
                                                .filter( Dependencies.Dep::isJarOrTestJar )
                                                .sorted( labelSort )
                                                .filter( Dependencies.Dep::isTest )
                                                .collect( Collectors.toList() );

        JtwigModel model = JtwigModel.newModel()
                                     .with( "module", module )
                                     .with( "compile", compileDeps.stream()
                                                                  .map( Dependencies.Dep::label )
                                                                  .collect( Collectors.toList() ) )
                                     .with( "test", testDeps.stream()
                                                            .map( Dependencies.Dep::label )
                                                            .collect( Collectors.toList() ) )
                                     .with( "javacopts", XmlTools
                                             .start( () -> (Xpp3Dom) project
                                                     .getPlugin( "org.apache.maven.plugins:maven-compiler-plugin" )
                                                     .getConfiguration() )
                                             .child( "compilerArgs" )
                                             .children().value().collect( Collectors.toList() ) )
                                     .with( "scalacopts", XmlTools
                                             .start( () -> (Xpp3Dom) project
                                                     .getPlugin( "net.alchim31.maven:scala-maven-plugin" )
                                                     .getConfiguration()
                                             )
                                             .child( "args" )
                                             .children().value().collect( Collectors.toList() )
                                     )
                                     .with( "scalac_jvm_flags", XmlTools
                                             .start( () -> (Xpp3Dom) project
                                                     .getPlugin( "net.alchim31.maven:scala-maven-plugin" )
                                                     .getConfiguration()
                                             )
                                             .child( "jvmArgs" )
                                             .children().value().collect( Collectors.toList() )
                                     );

        Path file = Paths.get( project.getBasedir().getAbsolutePath() ).resolve( "BUILD" );
        try ( FileOutputStream out = new FileOutputStream( file.toFile() ) )
        {
            JtwigTemplate.classpathTemplate( "templates/BUILD.twig" )
                         .render( model, out );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    void println( Dependency d )
    {
        System.out.println( d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion() + " @ " + d.getScope() );
    }

    Dependency log( Dependency d )
    {
        println( d );
        return d;
    }

    <T> T log( T t )
    {
        System.out.println( t );
        return t;
    }
}
