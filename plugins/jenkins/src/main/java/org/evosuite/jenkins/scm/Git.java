/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.jenkins.scm;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import org.eclipse.jgit.transport.URIish;
import org.evosuite.jenkins.recorder.EvoSuiteRecorder;
import org.jenkinsci.plugins.gitclient.CliGitAPIImpl;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.PushCommand;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.security.ACL;
import jenkins.plugins.git.GitSCMSource;

/**
 * Git wrapper to handle git commands, such commit and push.
 *
 * @author José Campos
 */
public class Git implements SCM {

    private GitClient gitClient;
    private final String remote;

    public Git(GitSCM gitSCM, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {

        String gitExe = gitSCM.getGitExe(build.getBuiltOn(), listener);
        EnvVars environment = build.getEnvironment(listener);

        this.gitClient = org.jenkinsci.plugins.gitclient.Git.with(listener, environment).in(build.getWorkspace())
                .using(gitExe) // only if you want to use Git CLI
                .getClient();

        // get remote configurations, e.g., URL
        List<UserRemoteConfig> remotes = gitSCM.getUserRemoteConfigs();
        UserRemoteConfig remoteConfig = remotes.get(0);
        this.remote = remoteConfig.getUrl();
        listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Remote config " + remoteConfig.toString());

        // get key's (i.e., username-password, ssh key, etc) hash
        String credentialID = remoteConfig.getCredentialsId();
        if (credentialID == null || credentialID.equals("null")) {
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "No credentials defined.");
            // TODO: should we throw an exception and make the build fail?
        } else {
            // get key (i.e., username-password or ssh key / passphrase)
            StandardUsernameCredentials credentials = this.getCredentials(credentialID);
            this.gitClient.setCredentials(credentials);
            this.gitClient.addDefaultCredentials(credentials);
        }
    }

    @Override
    public int commit(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build,
                      BuildListener listener, String branchName, String ctgBestsDir) {
        try {
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Commiting new test cases");

            Set<String> branches = this.getBranches();
            if (!branches.contains(branchName)) {
                // create a new branch called "evosuite-tests" to commit and
                // push the new generated test suites
                listener.getLogger()
                        .println(EvoSuiteRecorder.LOG_PREFIX + "There is no branch called " + branchName);
                this.gitClient.branch(branchName);
            }

            this.gitClient.setAuthor("jenkins", "jenkins@localhost.com");
            this.gitClient.setCommitter("jenkins", "jenkins@localhost.com");
            this.gitClient.checkoutBranch(branchName, "HEAD");

            EnvVars env = build.getEnvironment(listener);
            env.overrideAll(build.getBuildVariables());

            int number_of_files_committed = 0;
            try {
                MavenModuleSet prj = (MavenModuleSet) project;

                // parse list of new and modified files per module
                StringBuilder filesToBeCommitted = new StringBuilder();
                for (MavenModule module : prj.getModules()) {
                    String status = ((CliGitAPIImpl) this.gitClient).launchCommand("ls-files", "--deleted", "--modified",
                            "--others", (module.getRelativePath().isEmpty() ? "" : module.getRelativePath() + File.separator) +
                                    ctgBestsDir);
                    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Status (" + status.length() + "):\n" + status);
                    filesToBeCommitted.append(status);
                }

                String s_filesToBeCommitted = filesToBeCommitted.toString();
                if (s_filesToBeCommitted.isEmpty()) {
                    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Nothing to commit");
                    return 0;
                }

                for (String toCommit : s_filesToBeCommitted.split("\\R")) {
                    String filePath = build.getWorkspace().getRemote() + File.separator + toCommit;
                    if (new File(filePath).exists()) {
                        listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "adding: " + filePath);
                        this.gitClient.add(filePath);

                        number_of_files_committed++;
                    } else {
                        listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "File '" + filePath + "' reported by git status command does not exist");
                    }
                }
            } catch (ClassCastException e) {
                // FIXME when building a project remotely, we just have access to a GitClient of type
                // RemoteGitImpl, which cannot be cast to CliGitAPIImpl. and therefore, we cannot use
                // launchCommand method. as a workaround, we can simple add all files under .evosuite/best-tests
                // and hopefully git will take care of the rest. GitClient already supports the creation
                // of a new branch, checkout some branch, add files to be committed, commmit, push, etc.
                // there must be a way of getting the list of modified / new / deleted files just using
                // GitClient, however we still do not know how to get that.
                listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + e.getMessage() + "\nTrying a different approach!");
                FilePath[] filesToCommit = build.getWorkspace().list(build.getEnvironment(listener).expand(
                        "**" + File.separator + ctgBestsDir + File.separator + "**" + File.separator + "*"));

                if (filesToCommit.length == 0) {
                    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Nothing to commit");
                    return number_of_files_committed;
                }

                number_of_files_committed = filesToCommit.length;
                for (FilePath fileToCommit : filesToCommit) {
                    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "adding: " + fileToCommit.getRemote());
                    this.gitClient.add(fileToCommit.getRemote());
                }
            }

            // commit
            String commit_msg = SCM.COMMIT_MSG_PREFIX + build.getProject().getName().replace(" ", "_") + "-" + build.getNumber();
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + commit_msg);
            this.gitClient.commit(commit_msg);

            return number_of_files_committed;

        } catch (InterruptedException | IOException e) {
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Commit failed " + e.getMessage());
            e.printStackTrace();
            this.rollback(build, listener);
            return -1;
        }
    }

    @Override
    public boolean push(AbstractBuild<?, ?> build, BuildListener listener, String branchName) {
        try {
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Pushing new test cases");

            PushCommand p = this.gitClient.push();
            p.ref(branchName);
            p.to(new URIish("origin"));
            p.force().execute();

        } catch (InterruptedException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void rollback(AbstractBuild<?, ?> build, BuildListener listener) {
        try {
            listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Rollback, cleaning up workspace");
            this.gitClient.clean();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private StandardUsernameCredentials getCredentials(String credentialsID) {
        GitSCMSource source = new GitSCMSource("id", this.remote, credentialsID, "*", "", false);

        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, source.getOwner(), ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(source.getRemoteName()).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsID), GitClient.CREDENTIALS_MATCHER));
    }

    private Set<String> getBranches() throws InterruptedException {
        Set<String> branches = new LinkedHashSet<String>();
        for (Branch branch : this.gitClient.getBranches()) {
            String[] parts = branch.getName().split("/");
            branches.add(parts[parts.length - 1]);
        }
        return branches;
    }
}
