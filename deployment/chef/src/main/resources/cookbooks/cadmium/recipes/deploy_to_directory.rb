#
# Cookbook Name:: cadmium
# Recipe:: deploy_to_directory
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


unless node[:cadmium][:deploy_directory].nil?

    warLocation = "#{Chef::Config[:file_cache_path]}/#{node[:cadmium][:domain]}.war"
    deployDir = "#{node[:cadmium][:deploy_directory]}"
    if Dir.exists?("#{deployDir}") && File.exists?("#{warLocation}")
	    remote_file "Deploy war to jboss 5" do
            path "#{deployDir}/#{node[:cadmium][:domain]}.war"
            source "file://#{warLocation}"
            owner "#{node[:cadmium][:system_user]}"
            group "#{node[:cadmium][:system_group]}"
            mode 0644
	    end  
    end

end