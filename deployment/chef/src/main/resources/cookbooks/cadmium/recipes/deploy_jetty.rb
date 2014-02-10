#
# Cookbook Name:: cadmium
# Recipe:: deploy_jetty
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
  
jettyAppPath = "#{node[:cadmium][:jetty_root]}/#{node[:cadmium][:domain]}"

if !File.exists?("#{jettyAppPath}")
  
  directory "#{jettyAppPath}" do
    
    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"
  	recursive true

  end

end

if !File.exists?("#{jettyAppPath}/bin")

  directory "#{jettyAppPath}/bin" do

    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"

  end

end

if !File.exists?("#{jettyAppPath}/log")

  directory "#{jettyAppPath}/log" do

    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"

  end

end

remote_directory "#{jettyAppPath}/conf" do

  source "conf"
  files_owner "#{node[:cadmium][:system_user]}"
  files_group "#{node[:cadmium][:system_group]}"
  purge true
  overwrite true

end

remote_directory "#{jettyAppPath}/lib" do

  source "lib"
  files_owner "#{node[:cadmium][:system_user]}"
  files_group "#{node[:cadmium][:system_group]}"
  purge true
  overwrite true

end

remote_file "#{jettyAppPath}/bin/#{node[:cadmium][:domain]}.war" do
  source "file://#{Chef::Config[:file_cache_path]}/#{node[:cadmium][:domain]}.war"
  owner "#{node[:cadmium][:system_user]}"
  group "#{node[:cadmium][:system_group]}"

  action :create
end

# setup jetty service

template "/etc/init/#{node[:cadmium][:domain]}.conf" do
  source "cadmium-jetty.conf.erb"
  owner "root"
  group "root"
  mode 0644

  variables({
     :cwd => "#{jettyAppPath}",
     :user => "#{node[:cadmium][:system_user]}",
     :group => "#{node[:cadmium][:system_group]}",
     :environment => "#{node[:cadmium][:environment]}",
     :content_root => "#{node[:cadmium][:shared_content_root]}",
     :war_name => "#{node[:cadmium][:domain]}.war"
  })

  action :create
end

service "#{node[:cadmium][:domain]}" do
  provider Chef::Provider::Service::Upstart
  action :start
end
