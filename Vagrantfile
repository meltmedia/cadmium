# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "precise64"

  config.vm.box_url = "http://files.vagrantup.com/precise64.box"

  config.vm.network :forwarded_port, guest: 80, host: 8080, auto_correct: true
  config.vm.network :forwarded_port, guest: 443, host: 8443, auto_correct: true

  config.vm.synced_folder "~/.m2", "/home/vagrant/.m2"
  config.vm.synced_folder "~/.m2", "/root/.m2"
  config.vm.synced_folder "~/.ssh", "/home/cadmium/.ssh"
  config.vm.synced_folder "~/.cadmium", "/home/vagrant/.cadmium"

  config.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "128"]
  end

  config.vm.provision :chef_solo do |chef|
    chef.provisioning_path = "/tmp/vagrant-chef-solo"
    chef.file_cache_path = chef.provisioning_path
    chef.cookbooks_path = "deployment/chef/target/filtered-resources/cookbooks"
    chef.roles_path = "deployment/chef/target/filtered-resources/roles"
    chef.add_role "cadmium_vagrant"
  end
end
