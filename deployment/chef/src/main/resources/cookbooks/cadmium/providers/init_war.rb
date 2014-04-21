#
# Cookbook Name:: cadmium
# Provider:: init_war
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

action :fetch do

  log "Queuing war download #{new_resource.artifact_id}" do
    notifies :put, "maven[#{new_resource.artifact_id}]", :delayed
  end

  maven "#{new_resource.artifact_id}" do
    group_id "#{new_resource.group_id}"
    version "#{new_resource.version}"
    packaging "war"
    dest "#{Chef::Config[:file_cache_path]}"
    owner "#{new_resource.owner}"

    action :nothing
  end

  new_resource.updated_by_last_action(true)

end

action :init do

  log "Queuing war initialization #{new_resource.war_name}" do
    notifies :run, "execute[Initializing-war-#{new_resource.war_name}]", :delayed
  end

  warArg = "--existingWar #{new_resource.existing_war} "
  contentRepoArg = "--repo #{new_resource.content_repo} "
  contentBranchArg = "--branch cd-#{new_resource.content_branch} "
  contentArgs = "#{contentRepoArg}#{contentBranchArg} "
  configRepoArg = "--configuration-repo #{new_resource.config_repo} "
  configBranchArg = "--configuration-branch cfg-#{new_resource.config_branch} "
  configArgs = "#{configRepoArg}#{configBranchArg} "
  domainArg = "--domain #{new_resource.domain} #{new_resource.war_name} "

  execute "Initializing-war-#{new_resource.war_name}" do
    command "cadmium init-war #{warArg}#{contentArgs}#{configArgs}#{domainArg}"
    cwd "#{Chef::Config[:file_cache_path]}"
    user "#{new_resource.owner}"
    group "#{new_resource.group}"

    action :nothing
  end

  new_resource.updated_by_last_action(true)
end