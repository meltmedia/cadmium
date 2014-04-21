#
# Cookbook Name:: cadmium
# Recipe:: ssh
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

sshDir = "#{node[:cadmium][:shared_content_root]}/#{node[:cadmium][:ssh_dir]}"
sshKey = "#{sshDir}/meltmedia-gene-deploy"

if !File.exists?("#{sshDir}")
  directory "#{sshDir}" do
    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"
    mode 0755

    action :create
  end
end

if !node[:cadmium][:ssh_key_priv].nil?
  privUrl = "#{node[:cadmium][:ssh_key_priv]}"

  remote_file "#{sshKey}" do
  	source "#{privUrl}"
    owner "#{node[:cadmium][:system_user]}"
    group "#{node[:cadmium][:system_group]}"

    mode 0600

    action :create_if_missing
  end

elsif File.exists?("/home/cadmium/.ssh/id_rsa")
  ruby_block "Moving vagrant ssh key" do
    block do
      require 'fileutils'
      FileUtils.cp "/home/cadmium/.ssh/id_rsa", "#{sshKey}"
      FileUtils.chmod 0600, "#{sshKey}"
      FileUtils.chown "#{node[:cadmium][:system_user]}", "#{node[:cadmium][:system_group]}", "#{sshKey}"
    end
  end
end