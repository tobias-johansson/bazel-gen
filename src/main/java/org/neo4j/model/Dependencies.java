package org.neo4j.model;

import org.apache.maven.artifact.Artifact;

import org.neo4j.tools.Locations;

public class Dependencies
{
    private final Locations locations;

    public Dependencies( Locations locations )
    {
        this.locations = locations;
    }

    public Dep create( Artifact artifact )
    {
        return new Dep( artifact );
    }

    public class Dep
    {
        private final Artifact artifact;

        private Dep( Artifact artifact )
        {
            this.artifact = artifact;
        }

        public String id()
        {
            return artifact.getGroupId() + ":" + artifact.getArtifactId();
        }

        public String coords()
        {
            return id() + ":" + artifact.getVersion();
        }

        public boolean isTest()
        {
            return "test".equals( artifact.getScope() );
        }

        public boolean isJarOrTestJar()
        {
            return "jar".equals( artifact.getType() ) || "test-jar".equals( artifact.getType() );
        }

        public boolean isCompile()
        {
            return !isTest();
        }

        public boolean isInternal()
        {
            return "4.0.1-SNAPSHOT".equals( artifact.getVersion() );
        }

        public boolean isExternal()
        {
            return !isInternal();
        }

        public String label()
        {
            return isInternal() ? internalTarget() : externalTarget();
        }

        private String internalTarget()
        {
            String loc = locations.map.get( id() );
            return "//" + loc + ":" + artifact.getType();
        }

        private String externalTarget()
        {
            return "@maven//:" + id().replaceAll( "[^a-zA-Z0-9]", "_" );
        }
    }
}
