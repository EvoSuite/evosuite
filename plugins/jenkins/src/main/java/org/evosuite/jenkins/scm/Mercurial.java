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

import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.plugins.mercurial.HgExe;
import hudson.plugins.mercurial.MercurialInstallation;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

public class Mercurial implements SCM {

	private final MercurialSCM mercurialSCM;
	private final MercurialInstallation mercurialInstallation;
	private final StandardUsernameCredentials credentials;

	public Mercurial(MercurialSCM mercurialSCM, AbstractMavenProject<?, ?> project) {
		this.mercurialSCM = mercurialSCM;

		this.mercurialInstallation = (MercurialInstallation) this.findInstallation();
		this.credentials = (StandardUsernameCredentials) this.getCredentials(project);
	}

	@Override
	public ToolInstallation findInstallation() {
		// FIXME issue between "Default" and "(Default)"
		for (MercurialInstallation inst : MercurialInstallation.allInstallations()) {
			//if (inst.getName().equals(mercurialSCM.getInstallation())) {
				return inst;
			//}
		}

		return null;
	}

	@Override
	public Credentials getCredentials(AbstractMavenProject<?, ?> project) {
		// FIXME add support to private keys
		for (StandardUsernameCredentials c : CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, project, null, URIRequirementBuilder.fromUri(this.mercurialSCM.getSource()).build()) ) {
			if (c.getId().equals(this.mercurialSCM.getCredentialsId())) {
				return c;
			}
		}

		return null;
	}

	@Override
	public boolean commit(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		// FIXME to keep it simple, just commit all new files at .../evosuite-tests

		try {
			HgExe hg = new HgExe(this.mercurialInstallation, this.credentials, launcher, /*this.workspaceToNode(build.getWorkspace())*/Jenkins.getInstance(), listener, build.getEnvironment(listener));

			// start adding all removed files to commit
			if (hg.run("remove", "--after").pwd(build.getWorkspace()).join() != 0) {
				// TODO reset repository
				return false;
			}

			// parse list of new and modified files
			Set<String> setOfFiles = this.parseStatus(hg.popen(build.getWorkspace(), listener, false, new ArgumentListBuilder("status")));
			for (String file : setOfFiles) {
				if (hg.run("add", file).pwd(build.getWorkspace()).join() != 0) {
					// TODO reset repository
					return false;
				}
			}

			// commit
			String commit_msg = "EvoSuite Jenkins Plugin #" + "evosuite-" + build.getProject().getName().replace(" ", "_") + "-" + build.getNumber();

			if (hg.run("commit", "-m", commit_msg).pwd(build.getWorkspace()).join() != 0) {
				// TODO reset repository
				return false;
			}

			hg.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}

	@Override
	public boolean push(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		try {
			HgExe hg = new HgExe(this.mercurialInstallation, this.credentials, launcher, /*this.workspaceToNode(build.getWorkspace())*/Jenkins.getInstance(), listener, build.getEnvironment(listener));
			if (hg.run("push").pwd(build.getWorkspace()).join() != 0) {
				return false;
			}
			hg.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/*private Node workspaceToNode(FilePath workspace) {
		Jenkins j = Jenkins.getInstance();
		if (workspace.isRemote()) {
			for (Computer c : j.getComputers()) {
				if (c.getChannel() == workspace.getChannel()) {
					Node n = c.getNode();
					if (n != null) {
						return n;
					}
				}
			}
		}

		return j;
	}*/

	private Set<String> parseStatus(String status) {
		Set<String> result = new LinkedHashSet<String>();
		Matcher m = Pattern.compile("[?AMR!]\\s(.*" + SCM.TESTS_DIR_TO_COMMIT + ".*)").matcher(status);
		while (m.find()) {
			result.add(m.group(1));
		}
		return result;
	}
}
