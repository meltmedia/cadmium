package com.meltmedia.cadmium.jgit;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestJGit {
	private Logger log = LoggerFactory.getLogger(getClass());

	
	private String basepath;
    private Repository repo, remoteRepo;
    private Git git;
 // private String name = "chrishaley42";
 // private String password = "April602";
    //TODO delete repo first
    
    
    @Before
    public void init() throws IOException {
    	basepath = "/Users/chaley/repos/";
        repo = new FileRepository(basepath + "mytestrepo/.git");
        remoteRepo = new FileRepository(basepath + "testrepo.git");
        git = new Git(repo);
       
    }
    
    @Test
    public void testCreate() throws IOException {
        try {
          remoteRepo.create();
        } catch(IllegalStateException e) {
        	log.info("Repo Already Exists");
        }
    }
    
    @Test    
    public void testClone() throws IOException, NoFilepatternException {    
    	try{
        Git.cloneRepository().setURI(basepath + "testrepo.git").setDirectory(new File(basepath + "mytestrepo")).call();  
    	} catch(JGitInternalException e) {
    		log.info("Repo Already Exists");
    	}
    	
    }
    	
    
    @Test
    public void testAdd() throws IOException, NoFilepatternException { 
        File myfile = new File(basepath + "mytestrepo/myfile");
        myfile.createNewFile();
        git.add().addFilepattern("myfile").call();
    }
    
    @Test
    public void testPush() throws IOException, JGitInternalException, InvalidRemoteException, NoHeadException, NoMessageException, ConcurrentRefUpdateException, WrongRepositoryStateException {
    	//CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(name, password); 
    	git.commit().setMessage("commit").call();
    	git.push().call();

    }    

    @Test    
    public void testTrackMaster() throws IOException, JGitInternalException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException {     
        git.branchCreate().setName("master").setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM).setStartPoint("origin/master").setForce(true).call();
    }

    @Test
    public void testPull() throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException {
        git.pull().call();
    }
}
