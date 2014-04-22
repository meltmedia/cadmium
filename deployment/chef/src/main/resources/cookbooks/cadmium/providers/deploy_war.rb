#
# Cookbook Name:: cadmium
# Provider:: deploy_war
#
# Copyright 2014, Meltmedia
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

action :jetty do

  jettyAppPath = "#{new_resource.app_path}"
  warFinalLocation = "#{jettyAppPath}/bin/#{new_resource.war_name}"
  
  directory "#{jettyAppPath}" do
    
    owner "#{new_resource.owner}"
    group "#{new_resource.group}"
  	recursive true

  end

  directory "#{jettyAppPath}/bin" do

    owner "#{new_resource.owner}"
    group "#{new_resource.group}"

  end

  directory "#{jettyAppPath}/log" do

    owner "#{new_resource.owner}"
    group "#{new_resource.group}"

  end

  remote_directory "#{jettyAppPath}/conf" do

    source "conf"
    files_owner "#{new_resource.owner}"
    files_group "#{new_resource.group}"
    purge true
    overwrite true

  end

  remote_directory "#{jettyAppPath}/lib" do

    source "lib"
    files_owner "#{new_resource.owner}"
    files_group "#{new_resource.group}"
    purge true
    overwrite true

  end

  log "Installing war #{new_resource.war_name}" do
    notifies :create, "ruby_block[copy-war-#{new_resource.war_name}]", :delayed
  end

  ruby_block "copy-war-#{new_resource.war_name}" do
    block do
      require 'fileutils'
      FileUtils.cp "#{Chef::Config[:file_cache_path]}/#{new_resource.war_name}", "#{warFinalLocation}"
      FileUtils.chmod 0600, "#{warFinalLocation}"
      FileUtils.chown "#{new_resource.owner}", "#{new_resource.group}", "#{warFinalLocation}"
    end

    notifies :create, "template[/etc/init/#{new_resource.war_name}.conf]", :delayed

    action :nothing
  end

  # setup jetty service

  template "/etc/init/#{new_resource.war_name}.conf" do
    source "cadmium-jetty.conf.erb"
    owner "root"
    group "root"
    mode 0644

    variables({
      :cwd => "#{jettyAppPath}",
      :user => "#{new_resource.owner}",
      :group => "#{new_resource.group}",
      :environment => "#{node[:cadmium][:environment]}",
      :content_root => "#{node[:cadmium][:shared_content_root]}",
      :war_name => "#{new_resource.war_name}",
      :port => "#{new_resource.port}"
    })

    notifies :start, "service[#{new_resource.war_name}]", :delayed
    notifies :create, "template[/etc/apache2/sites-available/#{new_resource.war_name}.conf]", :delayed

    action :nothing
  end

  service "#{new_resource.war_name}" do
    provider Chef::Provider::Service::Upstart
    action :nothing
  end

  template "/etc/apache2/sites-available/#{new_resource.war_name}.conf" do
    source "apache-site.conf.erb"
    owner "www-data"
    group "www-data"
    mode "0644"

    variables({
      :war_name => "#{new_resource.war_name}.conf",
      :port => "#{new_resource.port}",
      :http_port => "#{node[:cadmium][:external_http_port]}",
      :https_port => "#{node[:cadmium][:external_https_port]}"
    })

    only_if { ::File.directory?("/etc/apache2") }

    notifies :run, "bash[enable-site-#{new_resource.war_name}.conf]", :delayed

    action :nothing
  end

  bash "enable-site-#{new_resource.war_name}.conf" do
    user "root"
    code <<-EOF
    a2ensite #{new_resource.war_name}.conf
    EOF

    notifies :restart, "service[apache2]", :delayed

    action :nothing
  end

service "apache2" do

  supports :restart => true, :reload => true, :status => true
  action :nothing
end
  
  new_resource.updated_by_last_action(true)
end