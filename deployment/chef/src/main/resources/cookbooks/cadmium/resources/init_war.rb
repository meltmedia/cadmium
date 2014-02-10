#
# Cookbook Name:: cadmium
# Resource:: init_war
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

actions :create

attribute :artifact_id,    :kind_of => String
attribute :existing_war,   :kind_of => String
attribute :group_id,       :kind_of => String
attribute :version,        :kind_of => String
attribute :domain,         :kind_of => String
attribute :war_name,       :kind_of => String
attribute :content_repo,   :kind_of => String
attribute :content_branch, :kind_of => String
attribute :config_repo,    :kind_of => String
attribute :config_branch,  :kind_of => String
attribute :owner,          :kind_of => String 
attribute :group,          :kind_of => String 
attribute :dest,           :kind_of => String

def initialize(*args)
  super
  # we can't use the node properties when initially specifying the resource
  @dest ||= @name
  @group_id ||= node[:cadmium][:war][:groupId]
  @artifact_id ||= node[:cadmium][:war][:artifactId]
  @version ||= node[:cadmium][:war][:version]
  @existing_war ||= "#{@artifact_id}.war"
  @domain ||= node[:cadmium][:domain]
  @content_repo ||= node[:cadmium][:content][:repo]
  @content_branch ||= node[:cadmium][:content][:branch]
  @config_repo ||= node[:cadmium][:config][:repo]
  @config_branch ||= node[:cadmium][:config][:branch]
  @war_name ||= "#{@domain}.war"
  @owner ||= node[:cadmium][:cadmium_user]
  @group ||= node[:cadmium][:cadmium_group]
  @action = :create
end