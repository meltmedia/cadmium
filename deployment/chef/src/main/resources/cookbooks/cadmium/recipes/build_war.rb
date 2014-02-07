#
# Cookbook Name:: cadmium
# Recipe:: build_war
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

load_recipe "maven::default"

maven "#{node[:cadmium][:war][:artifactId]}" do
  group_id "#{node[:cadmium][:war][:groupId]}"
  version "#{node[:cadmium][:war][:version]}"
  packaging "war"
  dest "#{Chef::Config[:file_cache_path]}"
  owner "#{user}"

  action :put
end

warArg = "--existingWar #{node[:cadmium][:war][:artifactId]}.war "
contentRepoArg = "--repo #{node[:cadmium][:content][:repo]} "
contentBranchArg = "--branch cd-#{node[:cadmium][:content][:branch]} "
contentArgs = "#{contentRepoArg}#{contentBranchArg} "
configRepoArg = "--configuration-repo #{node[:cadmium][:config][:repo]} "
configBranchArg = "--configuration-branch cfg-#{node[:cadmium][:config][:branch]} "
configArgs = "#{configRepoArg}#{configBranchArg} "
domainArg = "--domain #{node[:cadmium][:domain]} #{node[:cadmium][:domain]}.war "

execute "Initializing war" do
  command "cadmium init-war #{warArg}#{contentArgs}#{configArgs}#{domainArg}"
  cwd "#{Chef::Config[:file_cache_path]}"
  user "#{user}"
  group "#{node[:cadmium][:cadmium_group]}"

  action :run
end