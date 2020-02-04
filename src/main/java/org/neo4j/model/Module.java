package org.neo4j.model;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Module
{
    private final MavenProject project;
    private final List<MavenProject> reactorProjects;
    private final Global global;

    public Module( MavenProject project, List<MavenProject> reactorProjects, Global global )
    {
        this.project = project;
        this.reactorProjects = reactorProjects;
        this.global = global;
    }

    private List<Artifact> dependencyArtifacts( boolean transitive )
    {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest( global.session.getProjectBuildingRequest() );
        buildingRequest.setProject( project );

        LinkedList<Artifact> artifacts = new LinkedList<>();
        try
        {
            DependencyNode root = global.dependencyGraphBuilder.buildDependencyGraph( buildingRequest, null, reactorProjects );
            root.accept( new DependencyNodeVisitor()
            {
                @Override
                public boolean visit( DependencyNode dependencyNode )
                {

                    if ( dependencyNode == root )
                    {
                        return true;
                    }
                    else
                    {
                        artifacts.add( dependencyNode.getArtifact() );
                        return transitive;
                    }
                }

                @Override
                public boolean endVisit( DependencyNode dependencyNode )
                {
                    return true;
                }
            } );
        }
        catch ( DependencyGraphBuilderException e )
        {
            e.printStackTrace();
        }
        return artifacts;
    }

    public Stream<Dependencies.Dep> dependencies()
    {
        return dependencies( false );
    }

    public Stream<Dependencies.Dep> dependencies( boolean transitive )
    {
        return dependencyArtifacts( transitive ).stream()
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

    public boolean testAsJava()
    {
        return hasTestJava() && !hasTestScala();
    }

    public boolean testAsScala()
    {
        return hasTestScala();
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

    public boolean hasTestScala()
    {
        return project.getBasedir().toPath().resolve( "src/test/scala" ).toFile().exists();
    }
}
