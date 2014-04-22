#
# Cookbook Name:: cadmium
# Recipe:: init
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

group "#{node[:cadmium][:system_group]}" do
  system true

  action :create
end

user "#{node[:cadmium][:system_user]}" do
  system true
  gid "#{node[:cadmium][:system_group]}"
  home "#{node[:cadmium][:shared_content_root]}"
  shell "/bin/false"

  action :create
end

if !File.exists?("#{node[:cadmium][:shared_content_root]}")
  directory "#{node[:cadmium][:shared_content_root]}" do
    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"
    mode 0755

    action :create
  end
end

if !node[:cadmium][:github_teams].nil?
  template "#{node[:cadmium][:shared_content_root]}/team.properties" do
    source "team.properties.erb"
    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"

    mode 0644
  end
end

log "Installing apache2." do
  notifies :install, "package[apache2]", :immediate
  notifies :create, "directory[/etc/apache2/ssl]", :delayed
  notifies :run, "bash[install-apache2]", :delayed
  notifies :start, "service[apache2]", :delayed
end

package "apache2" do
  action :nothing
end

directory "/etc/apache2/ssl" do
  owner "www-data"
  group "www-data"
  mode "0755"
  recursive true

  action :nothing
end

bash "install-apache2" do
  user "root"
  code <<-EOH
  a2dissite default
  a2enmod proxy
  a2enmod proxy_http
  a2enmod headers
  EOH

  action :nothing
end

service "apache2" do

  supports :restart => true, :reload => true, :status => true
  action :nothing
end