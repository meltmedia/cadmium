#
# Cookbook Name:: cadmium
# Role:: standalone
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

name "standalone"
description "Role to build a jetty instance."
run_list(%w{
        recipe[apt::default]
        recipe[java::default]
        recipe[maven::default]
        recipe[cadmium::init]
        recipe[cadmium::install_cli]
        recipe[cadmium::war]
        recipe[cadmium::ssh]
        recipe[cadmium::deploy_jetty]
})