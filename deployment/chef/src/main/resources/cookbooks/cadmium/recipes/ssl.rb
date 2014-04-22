#
# Cookbook Name:: cadmium
# Recipe:: ssl
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

include_recipe "cadmium::init"

log "Setting up apache2 for ssl." do
  notifies :run, "bash[setup-apache2]", :delayed
end

bash "setup-apache2" do
  user "root"
  code <<-EOH
  a2enmod ssl
  EOH

  notifies :create, "cookbook_file[/etc/apache2/ssl/server.key]", :delayed
  notifies :create, "cookbook_file[/etc/apache2/ssl/server.crt]", :delayed
  notifies :restart, "service[apache2]", :delayed
  action :nothing
end

cookbook_file "/etc/apache2/ssl/server.key" do
  source "ssl/server.key"
  owner "www-data"
  group "www-data"
  mode "0600"

  action :nothing
end

cookbook_file "/etc/apache2/ssl/server.crt" do
  source "ssl/server.crt"
  owner "www-data"
  group "www-data"
  mode "0644"

  action :nothing
end