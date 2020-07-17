package org.hitchain.exporer.solidity;

import org.ethereum.solidity.compiler.SourceArtifact;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.solidity.compiler.ContractException.assembleError;

public class Sources {

    private final Map<String, org.ethereum.solidity.compiler.SourceArtifact> artifacts = new HashMap<>();
    private String targetArtifact;

    public Sources(File[] files) {
        for (File file : files) {
            artifacts.put(file.getName(), new org.ethereum.solidity.compiler.SourceArtifact(file));
        }
    }

    public void resolveDependencies() {
        for (String srcName : artifacts.keySet()) {
            org.ethereum.solidity.compiler.SourceArtifact src = artifacts.get(srcName);
            for (String dep : src.getDependencies()) {
                org.ethereum.solidity.compiler.SourceArtifact depArtifact = artifacts.get(dep);
                if (depArtifact == null) {
                    throw assembleError("can't resolve dependency: dependency '%s' not found.", dep);
                }
                src.injectDependency(depArtifact);
            };
        }

        for (SourceArtifact artifact : artifacts.values()) {
            if (!artifact.hasDependentArtifacts()) {
                targetArtifact = artifact.getName();
            }
        }
    }
    
    public String plainSource() {
        return artifacts.get(targetArtifact).plainSource();
    }
}
