package com.meltmedia.cadmium.jgit.impl;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;

public class CoordinatedWorkerImpl implements CoordinatedWorker {
	
	private String lastUpdatedDir = "";
	private CoordinatedWorkerListener listener;
	private boolean kill = false;
	private boolean running = false;
	
	@Override
	public void beginPullUpdates(final Map<String, String> properties) {
		
		if(!running) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					running = true;
					try {
						
						Repository repo = new FileRepository(properties.get("repo"));
						Git git = new Git(repo);
						git.pull();
						// TODO clone to new directory and delete .git folder.
						if(!kill) {
							listener.workDone(lastUpdatedDir);
						}
					} catch (IOException e) {
						if(!kill) {
							listener.workFailed();
						}
						//TODO add log statement
						e.printStackTrace();
					}
					finally {
						running = false;
					}
				}
			}).start();
			
		}
		
	}

	@Override
	public void killUpdate() {
		kill = true;
		
	}

	@Override
	public void setListener(CoordinatedWorkerListener listener) {
		this.listener = listener;
		
	}

}
