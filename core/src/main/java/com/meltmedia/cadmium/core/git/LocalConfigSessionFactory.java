/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.git;

import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.meltmedia.cadmium.core.FileSystemManager;

public class LocalConfigSessionFactory extends JschConfigSessionFactory {
	
	private String privateKeyFile;
	private String knownHostsFile;
	private String sshDir;
	private boolean noPrompt = false;
	
	public LocalConfigSessionFactory( String sshDir, boolean noPrompt ) {
		this.privateKeyFile = sshDir+"/meltmedia-gene-deploy";
		this.knownHostsFile = sshDir+"/known_hosts";
		this.sshDir = sshDir;
		this.noPrompt = noPrompt;
	}
	
	@Override
	protected void configure(Host host, Session session) {
		session.setUserInfo(new UserInfo() {

			@Override
			public String getPassphrase() {
			  if(noPrompt) {
			    return "";
			  } else {
			    return new String(System.console().readPassword());
			  }
			}

			@Override
			public String getPassword() {
        if(noPrompt) {
          return "";
        } else {
          return new String(System.console().readPassword());
        }
			}

			@Override
			public boolean promptPassphrase(String arg0) {
			  if(!noPrompt) {
	        System.err.print("Enter "+arg0+": ");
			  }
				return true;
			}

			@Override
			public boolean promptPassword(String arg0) {
        if(!noPrompt) {
          System.err.print("Enter "+arg0+": ");
        }
				return true;
			}

			@Override
			public boolean promptYesNo(String arg0) {
				return false;
			}

			@Override
			public void showMessage(String arg0) {
				System.err.println("Password or passphrase needed:");
			}
			
		});
	}

	
	@Override
	public synchronized RemoteSession getSession(URIish arg0,
			CredentialsProvider arg1, FS arg2, int arg3)
			throws TransportException {
		return super.getSession(arg0, arg1, arg2, arg3);
	}

	protected com.jcraft.jsch.JSch getJSch(OpenSshConfig.Host hc, FS fs)
      throws com.jcraft.jsch.JSchException
      {
		JSch jsch = super.getJSch(hc, fs);
    JSch.setConfig("StrictHostKeyChecking", "no");
		  if(FileSystemManager.exists(privateKeyFile)) {
		    jsch.addIdentity(privateKeyFile);
		  } else if (FileSystemManager.exists(sshDir + "/id_rsa")) {
		    jsch.addIdentity(sshDir + "/id_rsa");
		  }
	    jsch.setKnownHosts(knownHostsFile);
		return jsch;
      }

}
