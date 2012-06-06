package com.meltmedia.cadmium.cli;

import java.io.File;

import com.beust.jcommander.JCommander;
import com.meltmedia.cadmium.core.git.GitService;

public class CadmiumCli {
	
	public static JCommander jCommander = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		  setupSsh();
		  
		  jCommander = new JCommander();
		  
		  jCommander.setProgramName("cadmium");
		  
		  HelpCommand helpCommand = new HelpCommand();
		  jCommander.addCommand("help", helpCommand);
      
      NewBranchCommand newBranchCommand = new NewBranchCommand();
      jCommander.addCommand("new-branch", newBranchCommand);
      
      InitializeProjectCommand initProjectCommand = new InitializeProjectCommand();
      jCommander.addCommand("init-project", initProjectCommand);
		  
		  InitializeCommand initCommand = new InitializeCommand();
		  jCommander.addCommand("init-content", initCommand);
		  
		  InitializeWarCommand initWarCommand = new InitializeWarCommand();
		  jCommander.addCommand("init-war", initWarCommand);
      
      CloneCommand cloneCommand = new CloneCommand();
      jCommander.addCommand("clone", cloneCommand);
      
      RevertCommand revertCommand = new RevertCommand();
      jCommander.addCommand("revert", revertCommand);
      
      UpdateCommand updateCommand = new UpdateCommand();
      jCommander.addCommand("update", updateCommand);
      
      StatusCommand statusCommand = new StatusCommand();
      jCommander.addCommand("status", statusCommand);
		  
		  HistoryCommand historyCommand = new HistoryCommand();
		  jCommander.addCommand("history", historyCommand);
		  
		  CommitCommand commitCommand = new CommitCommand();
		  jCommander.addCommand("commit", commitCommand);
      
      MaintenanceCommand maintenanceCommand = new MaintenanceCommand();
      jCommander.addCommand("maint", maintenanceCommand);
		  
		  jCommander.parse(args);
		  
		  String commandName = jCommander.getParsedCommand();
		  if( commandName == null ) {
			  System.out.println("Use cadmium help for usage information.");
			  return;
		  }
		  
		  if( commandName.equals("help") ) {
			  if( helpCommand.subCommand.size()==0 ) {
				  jCommander.usage();
				  return;
			  }
			  else {
				 JCommander subCommander = jCommander.getCommands().get(helpCommand.subCommand.get(0));
				 if( subCommander == null ) {
					 System.out.println("Unknown sub command "+helpCommand.subCommand);
					 return;
				 }
				 subCommander.usage();
				 return;
			  }
		  }
		  else if( commandName.equals("update") ) {
			  updateCommand.execute();
		  }
		  else if( commandName.equals("init-content") ) {
		    initCommand.execute();
		  }
		  else if( commandName.equals("init-war") ) {
		    initWarCommand.execute();
		  }
		  else if( commandName.equals("history") ) {
		    historyCommand.execute();
		  }
		  else if ( commandName.equals("maint")) {
		  	maintenanceCommand.execute();
		  }
		  else if ( commandName.equals("new-branch")) {
		    newBranchCommand.execute();
		  } 
		  else if ( commandName.equals("init-project")) {
		  	initProjectCommand.execute();
		  }
		  else if ( commandName.equals("status")) {
			statusCommand.execute();
		  }
		  else if ( commandName.equals("clone")) {
		    cloneCommand.execute();
		  }
		  else if ( commandName.equals("commit")) {
		    commitCommand.execute();
		  }
		  else if ( commandName.equals("revert")) {
		    revertCommand.execute();
		  }
		
		}
		catch( Exception e ) {
			jCommander.usage();
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	
	private static void setupSsh() {
    File sshDir = new File(System.getProperty("user.home"), ".ssh");
    if(sshDir.exists()) {
      GitService.setupLocalSsh(sshDir.getAbsolutePath());
    }
	}

}
