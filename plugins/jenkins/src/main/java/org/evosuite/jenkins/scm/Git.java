/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.jenkins.scm;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;

import org.eclipse.jgit.lib.ObjectId;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.PushCommand;

import java.io.IOException;
import java.util.Set;

import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitTool;
import hudson.tools.ToolInstallation;

public class Git implements SCM {

	private final GitSCM gitSCM;
	private final GitTool gitInstallation;
	private final StandardUsernameCredentials credentials;

	public Git(GitSCM gitSCM, AbstractMavenProject<?, ?> project) {
		this.gitSCM = gitSCM;
		this.gitInstallation = GitTool.getDefaultInstallation();
		this.credentials = (StandardUsernameCredentials) this.getCredentials(project);
	}

	@Override
	public ToolInstallation findInstallation() {
		return this.gitInstallation;
	}

	@Override
	public Credentials getCredentials(AbstractMavenProject<?, ?> project) {
		CredentialsMatcher credentials = GitClient.CREDENTIALS_MATCHER;
		// TODO get credentials (username-password, or ssh key) of a particular project
		return null;
	}

	@Override
	public boolean commit(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

		try {
			org.jenkinsci.plugins.gitclient.Git git = new org.jenkinsci.plugins.gitclient.Git(listener, null);
			git.using(this.gitInstallation.getGitExe());

			GitClient client = git.getClient();

			Set<Branch> branches = client.getBranches();
			Branch evo_branch = new Branch(SCM.EVOSUITE_BRANCH, ObjectId.zeroId());
			if (!branches.contains(evo_branch)) {
				// create a new branch called "evosuite-tests" to commit and push
				// the new generated test suites
				client.branch(SCM.EVOSUITE_BRANCH);
			}

			// TODO do we really need this?
			client.setAuthor("jenkins", "jenkins@localhost.com");
			client.setCommitter("jenkins", "jenkins@localhost.com");

			// parse list of new and modified files
			

			// commit
			String commit_msg = "EvoSuite Jenkins Plugin #" + "evosuite-" + build.getProject().getName().replace(" ", "_") + "-" + build.getNumber();
			client.commit(commit_msg);

		} catch (IOException | InterruptedException e) {
			// TODO reset repository/branch ?!
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean push(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		try {
			/*BranchToPush evo_branch = new BranchToPush("", SCM.EVOSUITE_BRANCH); // FIXME
			List<BranchToPush> branches = new ArrayList<BranchToPush>();
			branches.add(evo_branch);

			GitPublisher publisher = new GitPublisher(null, branches, null, true, false, true);
			publisher.perform(build, launcher, listener);*/
			
			org.jenkinsci.plugins.gitclient.Git git = new org.jenkinsci.plugins.gitclient.Git(listener, null);
			git.using(this.gitInstallation.getGitExe());

			GitClient client = git.getClient();

			// FIXME set or add?
			client.setCredentials(this.credentials);
			client.addDefaultCredentials(this.credentials);

			PushCommand p = client.push();
			p.force().execute(); // TODO force and execute OR execute and force?

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
