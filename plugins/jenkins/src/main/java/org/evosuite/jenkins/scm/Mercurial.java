/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import org.evosuite.jenkins.recorder.EvoSuiteRecorder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.mercurial.HgExe;
import hudson.plugins.mercurial.MercurialInstallation;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.security.ACL;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;

/**
 * Mercurial wrapper to handle hg commands, such commit and push.
 * 
 * @author José Campos
 */
public class Mercurial implements SCM {

	private final HgExe hgClient;

	public Mercurial(MercurialSCM mercurialSCM, AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws IOException, InterruptedException {

		// TODO check whether there is an issue between "Default" and "(Default)"
		MercurialInstallation mercurialInstallation = null;
		for (MercurialInstallation inst : MercurialInstallation.allInstallations()) {
			if (inst.getName().equals(mercurialSCM.getInstallation())) {
				mercurialInstallation = inst;
				break;
			}
		}
		assert mercurialInstallation != null;

		// get credentials (username-password, ssh key-passphrase
		StandardUsernameCredentials credentials = this.getCredentials(mercurialSCM, project);
		listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Credentials " + credentials.getDescription());

		// get a MercurialClient to handle hg commands
		this.hgClient = new HgExe(mercurialInstallation, credentials, launcher, Jenkins.getInstance(), listener, build.getEnvironment(listener));
	}

	@Override
    public int commit(AbstractMavenProject<?, ?> project, AbstractBuild<?, ?> build,
        BuildListener listener, String branchName, String ctgBestsDir) {
		try {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Commiting new test cases");

			Set<String> branches = this.getBranches(build.getWorkspace(), listener);
			if (!branches.contains(branchName)) {
				// create a new branch called "evosuite-tests" to commit and
				// push the new generated test suites
				listener.getLogger()
						.println(EvoSuiteRecorder.LOG_PREFIX + "There is no branch called " + branchName);
				if (this.hgClient.run("branch", branchName).pwd(build.getWorkspace()).join() != 0) {
					listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Unable to create a new branch called " + branchName);
					return -1;
				}
			}

			// switch to EVOSUITE_BRANCH
			if (this.hgClient.run("update", branchName).pwd(build.getWorkspace()).join() != 0) {
				listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Unable to switch to branch " + branchName);
				return -1;
			}

			// start adding all removed files to commit
			if (this.hgClient.run("remove", "--after").pwd(build.getWorkspace()).join() != 0) {
				this.rollback(build, listener);
				return -1;
			}

			MavenModuleSet prj = (MavenModuleSet) project;

			// parse list of new and modified files
			int number_of_files_committed = 0;
			for (MavenModule module : prj.getModules()) {
			    for (String file : this.parseStatus(this.hgClient.popen(build.getWorkspace(), listener,
			        true, new ArgumentListBuilder("status")),
			        (module.getRelativePath().isEmpty() ? "" : module.getRelativePath() + File.separator) + ctgBestsDir)) {

			      if (this.hgClient.run("add", file).pwd(build.getWorkspace()).join() != 0) {
                      this.rollback(build, listener);
                      return -1;
			      }

                  number_of_files_committed++;
			    }
			}

			if (number_of_files_committed == 0) {
			    listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Nothing to commit");
                return 0;
			}

			// commit
			String commit_msg = SCM.COMMIT_MSG_PREFIX + build.getProject().getName().replace(" ", "_") + "-" + build.getNumber();
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + commit_msg);

			if (this.hgClient.run("commit", "--message", commit_msg).pwd(build.getWorkspace()).join() != 0) {
				this.rollback(build, listener);
				return -1;
			}

			return number_of_files_committed++;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public boolean push(AbstractBuild<?, ?> build, BuildListener listener, String branchName) {
		try {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Pushing new test cases");

			if (this.hgClient.run("push").pwd(build.getWorkspace()).join() != 0) {
				return false;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
    public void rollback(AbstractBuild<?, ?> build, BuildListener listener) {
		listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Rollback, cleaning up workspace");
		// TODO
	}

	private Set<String> parseStatus(String status, String ctgBestsDir) {
		Set<String> result = new LinkedHashSet<String>();
		Matcher m = Pattern.compile("[?AMR!]\\s(.*" + ctgBestsDir + ".*)").matcher(status);
		while (m.find()) {
			result.add(m.group(1));
		}
		return result;
	}

	private StandardUsernameCredentials getCredentials(MercurialSCM mercurialSCM, AbstractMavenProject<?, ?> project) {
		return CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, project, ACL.SYSTEM,
						URIRequirementBuilder.fromUri(mercurialSCM.getSource()).build()).get(0);
	}

	private Set<String> getBranches(FilePath workspace, BuildListener listener) throws InterruptedException, IOException {
        String rawBranches = this.hgClient.popen(workspace, listener, true, new ArgumentListBuilder("branches"));
        Set<String> branches = new LinkedHashSet<String>();
        for (String line: rawBranches.split("\n")) {
            // line should contain: <branchName>                 <revision>:<hash>
            String[] seperatedByWhitespace = line.split("\\s+");
            String branchName = seperatedByWhitespace[0];
            branches.add(branchName);
        }
        return branches;
    }
}
