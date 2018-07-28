/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.vcs.internal;

import org.gradle.api.Incubating;
import org.gradle.vcs.VersionControlSpec;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

/**
 * Allows the user to perform generic version control operations in ways
 * specified by the underlying implementations.
 *
 * @since 4.4
 */
@Incubating
public interface VersionControlSystem {
    /**
     * Returns a {@link Set} of {@link VersionRef}s representing
     * versions of a software package as they are known to the version
     * control system.
     */
    Set<VersionRef> getAvailableVersions(VersionControlSpec spec);

    /**
     * Populates a working directory with the given version fomr the version control repository from the {@code spec}.
     */
    void populate(File workingDir, VersionRef ref, VersionControlSpec spec);

    /**
     * Resets the given working directory to its state in the VCS.
     */
    void reset(File workingDir, VersionControlSpec spec);

    /**
     * Returns the default revision for this VCS.
     */
    VersionRef getDefaultBranch(VersionControlSpec spec);

    /**
     * Returns the given branch or null.
     */
    @Nullable
    VersionRef getBranch(VersionControlSpec spec, String branch);
}
