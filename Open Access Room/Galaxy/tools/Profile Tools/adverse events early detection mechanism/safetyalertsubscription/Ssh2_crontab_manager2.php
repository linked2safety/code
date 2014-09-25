<?php
/* 
  Ssh2_crontab_manager2.php - defines the Ssh2_crontab_manager2 class

   Copyright 2014 The University of Manchester tiand@cs.man.ac.uk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  
   The source files are available at: https://github.com/linked2safety/code
*/
Class Ssh2_crontab_manager2 {

	private $connection;
	private $path;
	private $handle;
	public  $cron_file;

	function __construct($host=NULL, $port=NULL, $username=NULL, $password=NULL)
	{		
		$path_length	 = strrpos(__FILE__, "/");
		$this->path 	 = substr(__FILE__, 0, $path_length) . '/';
		//generate a random file name to avoid file name conflict at the code execution time
 		$rand_num = strval(rand(2,9999999)*rand(3,9999999)); 	
		$this->handle	 = 'crontab'.$rand_num;
		$this->cron_file = "{$this->path}{$this->handle}";
		
		if (is_null($host) || is_null($port) || is_null($username) || is_null($password)) throw new Exception("The host, port, username and password arguments must be specified!");
		
		$this->connection = @ssh2_connect($host, $port);			
		if ( ! $this->connection) throw new Exception("The SSH2 connection could not be established.");

		$authentication = @ssh2_auth_password($this->connection, $username, $password);
		if ( ! $authentication) throw new Exception("Could not authenticate '{$username}' using pasword: '{$password}'.");
		
	}

	public function exec()
	{
		$argument_count = func_num_args();

		try
		{
			if ( ! $argument_count) throw new Exception("There is nothing to exececute, no arguments specified.");

			$arguments = func_get_args();
			
			$command_string = ($argument_count > 1) ? implode(" && ", $arguments) : $arguments[0];
			
			$stream = @ssh2_exec($this->connection, $command_string);
			if ( ! $stream) throw new Exception("Unable to execute the specified commands: <br />{$command_string}");
		}
		catch (Exception $e)
		{
			$this->error_message($e->getMessage());
		}

		return $this;
	}

	public function write_to_file($path=NULL, $handle=NULL)
	{
		if ( ! $this->crontab_file_exists())
		{		
			$this->handle = (is_null($handle)) ? $this->handle : $handle;
			$this->path   = (is_null($path))   ? $this->path   : $path;			
			$this->cron_file = "{$this->path}{$this->handle}";
			
			$init_cron = "crontab -l > {$this->cron_file} && [ -f {$this->cron_file} ] || > {$this->cron_file}";
			
			$this->exec($init_cron);		
		}
	
		return $this;	
	}
	
	public function remove_file()
	{		
		if ($this->crontab_file_exists()) $this->exec("rm {$this->cron_file}");		
		return $this;
	}
	
	public function append_cronjob($cron_jobs=NULL)
	{
		if (is_null($cron_jobs)) $this->error_message("Nothing to append!  Please specify a cron job or an array of cron jobs.");
		
		$append_cronfile = "echo '";		
		
		$append_cronfile .= (is_array($cron_jobs)) ? implode("\n", $cron_jobs) : $cron_jobs;
		
		$append_cronfile .= "'  >> {$this->cron_file}";
		
		$install_cron = "crontab {$this->cron_file}";

		$this->write_to_file()->exec($append_cronfile, $install_cron)->remove_file();
		
		return $this;		
	}
	
	public function remove_cronjob($cron_jobs=NULL)
	{	
		if (is_null($cron_jobs)) $this->error_message("Nothing to remove!  Please specify a cron job or an array of cron jobs.");
		
		$this->write_to_file();
	
		$cron_array = file($this->cron_file, FILE_IGNORE_NEW_LINES);
		
		if (empty($cron_array))
		{
		  exit;	
		}
		
		$original_count = count($cron_array);
		
		if (is_array($cron_jobs))
		{
			foreach ($cron_jobs as $cron_regex) $cron_array = preg_grep($cron_regex, $cron_array, PREG_GREP_INVERT);
		}
		else
		{
			$cron_array = preg_grep($cron_jobs, $cron_array, PREG_GREP_INVERT);
		}
		
		return ($original_count === count($cron_array)) ? $this->remove_file() : $this->remove_crontab()->append_cronjob($cron_array);
	}

	public function remove_crontab()
	{
		$this->remove_file()->exec("crontab -r");		
		return $this;
	}

	private function crontab_file_exists()
	{
		return file_exists($this->cron_file);
	}
	
	private function error_message($error)
	{
		die("<pre style='color:#EE2711'>ERROR: {$error}</pre>");
	}
	
}
